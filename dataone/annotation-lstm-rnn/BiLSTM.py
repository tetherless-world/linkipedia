import tensorflow as tf
import numpy as np
from pretrain_embedding import PreTrainEmbedding
import random
from data_helper import DataHelper
from rdflib import *


class BiLSTM:
    def __init__(self, retrain, train_data, test_data, mt_table, epochs,
                 embedding_size=100, max_doc_length=10, n_hidden=64):
        self.retrain = retrain
        self.embedding_size = embedding_size
        self.n_hidden = n_hidden
        self.n_steps = max_doc_length
        self.n_input = embedding_size
        self.n_class = 2
        self.learning_rate = 0.001
        self.default_batch_size = 50
        self.epochs = epochs
        self.display_step = 10

        self.sess = None
        self.train_data = train_data
        self.test_data = test_data
        self.mt_table = mt_table

        self.abstract = None
        self.x1_label = None
        self.x1_defn = None
        self.x1_unit = None
        self.x2_label = None
        self.x2_defn = None
        self.x2_unit = None
        self.y_mt = None
        self.y_ent = None
        self.y_char = None
        self.keep_prob = None
        self.weights = None
        self.biases = None

        self.sequence_length = None
        self.outputs = None
        self.relevant_output = None

        self.rep_abstract = None
        self.rep_x1_label = None
        self.rep_x1_defn = None
        self.rep_x1_unit = None
        self.rep_x2_label = None
        self.rep_x2_defn = None
        self.rep_x2_unit = None

        self.seq_len_abstract = None
        self.seq_len_x1_label = None
        self.seq_len_x2_label = None
        self.seq_len_x1_defn = None
        self.seq_len_x2_defn = None
        self.seq_len_x1_unit = None
        self.seq_len_x2_unit = None

        self.joined_vec = None
        self.sim_score_label = None
        self.sim_score_defn = None
        self.sim_score_unit = None
        self.w_sim = None
        self.w_out_mt = None
        self.b_out_mt = None
        self.w_out_ent = None
        self.b_out_ent = None
        self.w_out_char = None
        self.b_out_char = None
        self.pred_mt = None
        self.pred_ent = None
        self.pred_char = None

        self.pred_softmax_mt = None
        self.pred_softmax_ent = None
        self.pred_softmax_char = None

        self.loss_orig = None
        self.loss = None
        self.optimizer = None
        self.correct_pred = None
        self.accuracy = None
        self.init = None
        self.saver = None

        self.embeddings = PreTrainEmbedding('/Users/jason/Software/PubMed/wikipedia-pubmed-and-PMC-w2v.bin',
                                            embedding_size=embedding_size, binary=True)

    def define(self):
        self.abstract = tf.placeholder("float", [None, self.n_steps, self.n_input])
        self.x1_label = tf.placeholder("float", [None, self.n_steps, self.n_input])
        self.x2_label = tf.placeholder("float", [None, self.n_steps, self.n_input])
        self.x1_defn = tf.placeholder("float", [None, self.n_steps, self.n_input])
        self.x2_defn = tf.placeholder("float", [None, self.n_steps, self.n_input])
        self.x1_unit = tf.placeholder("float", [None, self.n_steps, self.n_input])
        self.x2_unit = tf.placeholder("float", [None, self.n_steps, self.n_input])
        self.y_mt = tf.placeholder("float", [None, self.n_class])
        self.y_ent = tf.placeholder("float", [None, self.n_class])
        self.y_char = tf.placeholder("float", [None, self.n_class])
        self.keep_prob = tf.placeholder(tf.float32)

        with tf.variable_scope("BiLSTM_Abstract"):
            self.rep_abstract, self.seq_len_abstract = self.bidirectional_rnn(self.abstract)

        with tf.variable_scope("BiLSTM_Label") as scope:
            self.rep_x1_label, self.seq_len_x1_label = self.bidirectional_rnn(self.x1_label)
            scope.reuse_variables()
            self.rep_x2_label, self.seq_len_x2_label= self.bidirectional_rnn(self.x2_label)

        with tf.variable_scope("BiLSTM_Defn") as scope:
            self.rep_x1_defn, self.seq_len_x1_defn = self.bidirectional_rnn(self.x1_defn)
            scope.reuse_variables()
            self.rep_x2_defn, self.seq_len_x2_defn = self.bidirectional_rnn(self.x2_defn)

        with tf.variable_scope("BiLSTM_Unit") as scope:
            self.rep_x1_unit, self.seq_len_x1_unit = self.bidirectional_rnn(self.x1_unit)
            scope.reuse_variables()
            self.rep_x2_unit, self.seq_len_x2_unit = self.bidirectional_rnn(self.x2_unit)

        self.w_sim = tf.get_variable("w_sim", shape=[self.n_hidden, self.n_hidden],
                                     initializer=tf.contrib.layers.xavier_initializer())

        self.sim_score_label = tf.diag_part(tf.matmul(tf.matmul(self.rep_x1_label, self.w_sim),
                                                      tf.transpose(self.rep_x2_label)))
        self.sim_score_label = tf.expand_dims(self.sim_score_label, 1)

        self.sim_score_defn = tf.diag_part(tf.matmul(tf.matmul(self.rep_x1_defn, self.w_sim),
                                                     tf.transpose(self.rep_x2_defn)))
        self.sim_score_defn = tf.expand_dims(self.sim_score_defn, 1)

        self.sim_score_unit = tf.diag_part(tf.matmul(tf.matmul(self.rep_x1_unit, self.w_sim),
                                                     tf.transpose(self.rep_x2_unit)))
        self.sim_score_unit = tf.expand_dims(self.sim_score_unit, 1)

        self.joined_vec = tf.concat([self.rep_abstract, self.rep_x1_label, self.rep_x1_defn, self.rep_x1_unit,
                                     self.sim_score_label, self.sim_score_defn, self.sim_score_unit,
                                     self.rep_x2_label, self.rep_x2_defn, self.rep_x2_unit], 1)

        self.w_out_mt = tf.get_variable("w_out_mt", shape=[7 * self.n_hidden + 3, self.n_class],
                                        initializer=tf.contrib.layers.xavier_initializer())
        self.b_out_mt = tf.get_variable("b_out_mt", [self.n_class],
                                        initializer=tf.constant_initializer(0.0))

        self.w_out_ent = tf.get_variable("w_out_ent", shape=[7 * self.n_hidden + 3, self.n_class],
                                         initializer=tf.contrib.layers.xavier_initializer())
        self.b_out_ent = tf.get_variable("b_out_ent", [self.n_class],
                                         initializer=tf.constant_initializer(0.0))

        self.w_out_char = tf.get_variable("w_out_char", shape=[7 * self.n_hidden + 3, self.n_class],
                                          initializer=tf.contrib.layers.xavier_initializer())
        self.b_out_char = tf.get_variable("b_out_char", [self.n_class],
                                          initializer=tf.constant_initializer(0.0))

        self.pred_mt = tf.matmul(tf.nn.dropout(self.joined_vec, self.keep_prob), self.w_out_mt) + self.b_out_mt
        self.pred_softmax_mt = tf.nn.softmax(self.pred_mt)

        self.pred_ent = tf.matmul(tf.nn.dropout(self.joined_vec, self.keep_prob), self.w_out_ent) + self.b_out_ent
        self.pred_softmax_ent = tf.nn.softmax(self.pred_ent)

        self.pred_char = tf.matmul(tf.nn.dropout(self.joined_vec, self.keep_prob), self.w_out_char) + self.b_out_char
        self.pred_softmax_char = tf.nn.softmax(self.pred_char)

        self.loss_orig = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=self.pred_mt, labels=self.y_mt)) \
                         + tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=self.pred_ent, labels=self.y_ent)) \
                         + tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=self.pred_char, labels=self.y_char))

        l2_loss = tf.nn.l2_loss(self.w_out_mt) + tf.nn.l2_loss(self.b_out_mt) \
                  + tf.nn.l2_loss(self.w_out_ent) + tf.nn.l2_loss(self.b_out_ent) \
                  + tf.nn.l2_loss(self.w_out_char) + tf.nn.l2_loss(self.b_out_char)
        l2_reg_lambda = 0.5
        self.loss = self.loss_orig + l2_reg_lambda * l2_loss
        self.optimizer = tf.train.AdamOptimizer(learning_rate=self.learning_rate).minimize(self.loss)

        self.init = tf.global_variables_initializer()
        self.saver = tf.train.Saver()

    def bidirectional_rnn(self, x):
        sequence_length = self.retrieve_seq_length_op(x)

        # Unstack to get a list of 'n_steps' tensors of shape (batch_size, n_input)
        x = tf.unstack(x, self.n_steps, 1)

        with tf.variable_scope('forward'):
            lstm_fw_cell = tf.contrib.rnn.BasicLSTMCell(self.n_hidden, forget_bias=1.0)

        outputs, _ = tf.contrib.rnn.static_rnn(
            inputs=x,
            cell=lstm_fw_cell,
            initial_state=lstm_fw_cell.zero_state(tf.shape(x[0])[0], dtype=tf.float32),
            sequence_length=sequence_length)

        relevant_output = self.last_relevant(outputs, sequence_length, self.n_hidden)

        return relevant_output, sequence_length

    @staticmethod
    def last_relevant(outputs, sequence_length, output_size):
        batch_size = tf.shape(tf.stack(outputs))[1]
        index = tf.range(0, batch_size) + tf.nn.relu((sequence_length - 1)) * batch_size
        flat = tf.reshape(tf.stack(outputs), [-1, output_size])
        relevant = tf.gather(flat, index)
        return relevant

    @staticmethod
    def retrieve_seq_length_op(data):
        """ An op to compute the length of a sequence. 0 are masked. """
        used = tf.sign(tf.reduce_max(tf.abs(data), reduction_indices=2))
        length = tf.reduce_sum(used, reduction_indices=1)
        length = tf.cast(length, tf.int32)
        return length

    def construct_batch(self, data, indices):
        abstract = np.array([self.embeddings.create_word_level_embeddings(
            data[idx]['datasetAbstract'], max_len=self.n_steps) for idx in indices])

        x1_label = np.array([self.embeddings.create_word_level_embeddings(
            data[idx]['datasetLabel'], max_len=self.n_steps) for idx in indices])
        x1_defn = np.array([self.embeddings.create_word_level_embeddings(
            data[idx]['datasetDefn'], max_len=self.n_steps) for idx in indices])
        x1_unit = np.array([self.embeddings.create_word_level_embeddings(
            data[idx]['datasetUnit'], max_len=self.n_steps) for idx in indices])

        x2_label = np.array([self.embeddings.create_word_level_embeddings(
            data[idx]['ontoLabel'], max_len=self.n_steps) for idx in indices])
        x2_defn = np.array([self.embeddings.create_word_level_embeddings(
            data[idx]['ontoDefn'], max_len=self.n_steps) for idx in indices])
        x2_unit = np.array([self.embeddings.create_word_level_embeddings(
            data[idx]['ontoUnit'], max_len=self.n_steps) for idx in indices])

        y_mt = np.array([data[idx]['similarity_mt'] for idx in indices])
        y_ent = np.array([data[idx]['similarity_ent'] for idx in indices])
        y_char = np.array([data[idx]['similarity_char'] for idx in indices])

        return abstract, x1_label, x1_defn, x1_unit, x2_label, x2_defn, x2_unit, y_mt, y_ent, y_char

    def train(self):
        with tf.Session() as self.sess:
            if self.retrain:
                print 'Starting training!'
                self.sess.run(self.init)
                step = 1
                n_processed = 0
                cur_epochs = 0
                shuffled_idx = range(len(self.train_data))
                random.shuffle(shuffled_idx)

                while step <= 3000:
                    if n_processed >= len(self.train_data):
                        n_processed = 0
                        cur_epochs += 1
                        shuffled_idx = range(len(self.train_data))
                        random.shuffle(shuffled_idx)
                    selected_idx = shuffled_idx[n_processed:min(n_processed + self.default_batch_size,
                                                                len(self.train_data))]
                    n_processed += len(selected_idx)
                    batch_size = len(selected_idx)

                    abstract, x1_label, x1_defn, x1_unit, x2_label, x2_defn, x2_unit, y_mt, y_ent, y_char = \
                        self.construct_batch(self.train_data, selected_idx)

                    batch_abstract = abstract.reshape((batch_size, self.n_steps, self.n_input))
                    batch_x1_label = x1_label.reshape((batch_size, self.n_steps, self.n_input))
                    batch_x1_defn = x1_defn.reshape((batch_size, self.n_steps, self.n_input))
                    batch_x1_unit = x1_unit.reshape((batch_size, self.n_steps, self.n_input))
                    batch_x2_label = x2_label.reshape((batch_size, self.n_steps, self.n_input))
                    batch_x2_defn = x2_defn.reshape((batch_size, self.n_steps, self.n_input))
                    batch_x2_unit = x2_unit.reshape((batch_size, self.n_steps, self.n_input))
                    batch_y_mt = y_mt.reshape((batch_size, self.n_class))
                    batch_y_ent = y_ent.reshape((batch_size, self.n_class))
                    batch_y_char = y_char.reshape((batch_size, self.n_class))

                    feed_dict = {self.abstract: batch_abstract,
                                 self.x1_label: batch_x1_label,
                                 self.x1_defn: batch_x1_defn,
                                 self.x1_unit: batch_x1_unit,
                                 self.x2_label: batch_x2_label,
                                 self.x2_defn: batch_x2_defn,
                                 self.x2_unit: batch_x2_unit,
                                 self.y_mt: batch_y_mt,
                                 self.y_ent: batch_y_ent,
                                 self.y_char: batch_y_char,
                                 self.keep_prob: 0.7}

                    self.sess.run(self.optimizer, feed_dict=feed_dict)
                    if step % self.display_step == 0:
                        [sequence_len_abstract_val,
                         sequence_len_x1_label_val,
                         sequence_len_x2_label_val,
                         sequence_len_x1_defn_val,
                         sequence_len_x2_defn_val,
                         sequence_len_x1_unit_val,
                         sequence_len_x2_unit_val,
                         loss_val, loss_orig_val] = self.sess.run(
                            [self.seq_len_abstract,
                             self.seq_len_x1_label,
                             self.seq_len_x2_label,
                             self.seq_len_x1_defn,
                             self.seq_len_x2_defn,
                             self.seq_len_x1_unit,
                             self.seq_len_x2_unit,
                             self.loss, self.loss_orig], feed_dict=feed_dict)
                        print ''
                        print 'sequence_len_abstract_val: ', sequence_len_abstract_val
                        print 'sequence_len_x1_label_val: ', sequence_len_x1_label_val
                        print 'sequence_len_x2_label_val: ', sequence_len_x2_label_val
                        print 'sequence_len_x1_defn_val: ', sequence_len_x1_defn_val
                        print 'sequence_len_x2_defn_val: ', sequence_len_x2_defn_val
                        print 'sequence_len_x1_unit_val: ', sequence_len_x1_unit_val
                        print 'sequence_len_x2_unit_val: ', sequence_len_x2_unit_val
                        print "Iter " + str(step) + ", Minibatch Loss= " + str(loss_val) + \
                              ", Minibatch Original Loss= " + str(loss_orig_val)
                    step += 1
                print "Optimization Finished!"
                save_path = self.saver.save(self.sess, "bilstm_model.ckpt")
                print("Model saved in file: %s" % save_path)
            else:
                self.saver.restore(self.sess, "./bilstm_model.ckpt")
                print("Model restored.")
            self.test()

    def test(self):
        print 'Starting testing!'
        print 'Number:', len(self.test_data)
        matching = []
        total = len(self.test_data)
        i = 0
        for anno in self.test_data:
            batch = DataHelper.get_test_pairs(anno, self.mt_table)
            batch_size = len(batch)

            abstract, x1_label, x1_defn, x1_unit, x2_label, x2_defn, x2_unit, y_mt, y_ent, y_char = \
                self.construct_batch(batch, range(len(batch)))

            batch_abstract = abstract.reshape((batch_size, self.n_steps, self.n_input))
            batch_x1_label = x1_label.reshape((batch_size, self.n_steps, self.n_input))
            batch_x1_defn = x1_defn.reshape((batch_size, self.n_steps, self.n_input))
            batch_x1_unit = x1_unit.reshape((batch_size, self.n_steps, self.n_input))
            batch_x2_label = x2_label.reshape((batch_size, self.n_steps, self.n_input))
            batch_x2_defn = x2_defn.reshape((batch_size, self.n_steps, self.n_input))
            batch_x2_unit = x2_unit.reshape((batch_size, self.n_steps, self.n_input))
            batch_y_mt = y_mt.reshape((batch_size, self.n_class))
            batch_y_ent = y_ent.reshape((batch_size, self.n_class))
            batch_y_char = y_char.reshape((batch_size, self.n_class))

            feed_dict = {self.abstract: batch_abstract,
                         self.x1_label: batch_x1_label,
                         self.x1_defn: batch_x1_defn,
                         self.x1_unit: batch_x1_unit,
                         self.x2_label: batch_x2_label,
                         self.x2_defn: batch_x2_defn,
                         self.x2_unit: batch_x2_unit,
                         self.y_mt: batch_y_mt,
                         self.y_ent: batch_y_ent,
                         self.y_char: batch_y_char,
                         self.keep_prob: 1.0}

            [pred_val_mt, pred_val_ent, pred_val_char] = \
                self.sess.run([self.pred_softmax_mt, self.pred_softmax_ent, self.pred_softmax_char],
                              feed_dict=feed_dict)

            sim_values_mt = [(val[0] - val[1]) for val in pred_val_mt]
            sim_values_ent = [(val[0] - val[1]) for val in pred_val_ent]
            sim_values_char = [(val[0] - val[1]) for val in pred_val_char]
            sim_values = [sum(x) for x in zip(sim_values_mt, sim_values_ent, sim_values_char)]
            correct_mt = URIRef(anno['mtUri'])

            # top_three_indices = np.array([(sim[0] - sim[1]) for sim in sim_values]).argsort()[-3:][::-1]
            top_three_indices = np.array(sim_values).argsort()[-3:][::-1]
            top_three_mt = [self.mt_table[i]['uri'] for i in top_three_indices]
            top_three_prob = [sim_values[i] for i in top_three_indices]

            if correct_mt in top_three_mt:
                matching.append(1)
            else:
                matching.append(0)

            print 'target annotation:', anno
            print 'top three:', top_three_mt
            print 'top three probs:', top_three_prob
            i += 1
            print i, '/', total
            print ''
        precision = np.mean(np.array(matching))
        print "Test Accuracy by three hits = " + "{:.5f}".format(precision)


