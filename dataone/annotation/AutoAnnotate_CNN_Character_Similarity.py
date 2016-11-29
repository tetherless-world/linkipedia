
msg = 'Do you want to retrain the model?'
ans = raw_input("%s (y/N) " % msg).lower() == 'y'

import json
with open('data.json') as f:
    manu_annotation = json.load(f)
print 'Size of manual annotation: ', len(manu_annotation)

from rdflib import URIRef, Namespace, ConjunctiveGraph, OWL, RDFS
MeasurementType = URIRef('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#MeasurementType')
Entity = URIRef('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Entity')
Characteristic = URIRef('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Characteristic')
SKOS = Namespace('http://www.w3.org/2004/02/skos/core#')
def get_all_measurement_types(ontology_file):
    graph = ConjunctiveGraph()
    graph.load(ontology_file, format="n3")
    query_str = '''SELECT DISTINCT ?mt ?label
        WHERE {
          ?mt rdfs:label ?label .
          ?mt rdfs:subClassOf <%s> .
          ?mt rdfs:subClassOf ?r1 .
          ?r1 owl:onProperty oboe:measuresEntity ; owl:someValuesFrom ?ent .
          ?mt rdfs:subClassOf ?r2 .
          ?r2 owl:onProperty oboe:measuresCharacteristic ; owl:someValuesFrom ?char .
          OPTIONAL { ?mt rdfs:comment ?comment }
          OPTIONAL { ?mt skos:definition ?defn }
        }''' % (MeasurementType)
    qres = list(graph.query(query_str, initNs=dict(oboe=URIRef("http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#"),
                                                   owl=OWL,rdfs=RDFS,skos=SKOS)))
    if len(qres) > 0:
        qres.sort(key=lambda x: x[0], reverse=True)
        result = dict()
        i = 0
        for row in qres:
            result[i] = (row[0], row[1])
            i = i + 1
        print "Sparql query finished!"
        return result
    return None

nt_file = '/Users/jason/EclipseWorkspace/linkipedia/dataone-index/NTriple/merged.nt'
mt_dict = get_all_measurement_types(nt_file)
num_classes = len(mt_dict)
print 'Number of measurement types: ', num_classes

def get_samples(manu_anno, indices):
    result = []
    for i in indices:
        result.append(manu_anno[i])
    return result

import random
indices_for_sample = range(len(manu_annotation))
random.shuffle(indices_for_sample)
nSample = 8000
training_manu_anno = get_samples(manu_annotation, indices_for_sample[0:nSample])
testing_manu_anno = get_samples(manu_annotation, indices_for_sample[nSample:])

from pretrain_embedding import PreTrainEmbedding
embedding_size = 300
#we = PreTrainEmbedding('/Users/jason/EclipseWorkspace/GoogleNews-vectors-negative300.bin.gz', embedding_size)

def get_train_pair(ontology, manu_anno, negative_label_size):
    mt_indices = []
    labels = []
    pairs = []
    for d in manu_anno:
        correct_idx = 0
        for idx in ontology:
            if ontology[idx][0] == URIRef(d['mt']):
                labels.append(1)
                mt_indices.append(idx)
                correct_idx = idx
                pairs.append(URIRef(ontology[idx][0], URIRef(d['mt'])))
                break
        if negative_label_size > 0:
            shuffled_idx = range(len(ontology.keys()))
            random.shuffle(shuffled_idx)
            num_neg_label = 0
            for idx in shuffled_idx:
                if not idx == correct_idx:
                    labels.append(0)
                    mt_indices.append(-1)
                    pairs.append((ontology[idx][0], URIRef(d['mt'])))
                    num_neg_label += 1
                    if num_neg_label == negative_label_size:
                        break
    return pairs, labels, mt_indices

def get_test_pair_label(target_dict, manu_anno):
    mt_indices = []
    labels = []
    pairs = []
    for d in manu_anno:
        for idx in target_dict:
            if target_dict[idx][0] == URIRef(d['mt']):
                labels.append(1)
                mt_indices.append(idx)
                pairs.append((URIRef(d['mt']), URIRef(d['mt'])))
                break
    return pairs, labels, mt_indices

train_pairs, train_labels, train_mt_indices = get_train_pair(mt_dict, training_manu_anno, 3)
test_pairs, test_labels, test_mt_indice = get_test_pair_label(mt_dict, testing_manu_anno)
print 'Training label length: ', len(train_labels)
print 'Testing label length: ', len(test_labels)

# Network Parameters
learning_rate = 0.001
training_iters = 200000
default_batch_size = 50
display_step = 100
n_input = embedding_size

label_len = 10
defn_len = 20
context_len = 50
dropout = 0.75 # Dropout, probability to keep units

import tensorflow as tf
slabel = tf.placeholder("float", [None, label_len, n_input])
tlabel = tf.placeholder("float", [None, label_len, n_input])
sdefn = tf.placeholder("float", [None, defn_len, n_input])
tdefn = tf.placeholder("float", [None, defn_len, n_input])
scontext = tf.placeholder("float", [None, context_len, n_input])
tcontext = tf.placeholder("float", [None, context_len, n_input])
y = tf.placeholder("float", [None, num_classes])
keep_prob = tf.placeholder(tf.float32)

from conv_net import ConvNet
src_label_net = ConvNet(slabel)
tar_label_net = ConvNet(tlabel)
src_defn_net = ConvNet(sdefn)
tar_defn_net = ConvNet(tdefn)
src_cont_net = ConvNet(scontext)
tar_cont_net = ConvNet(tcontext)

print src_label_net.output.get_shape()

with tf.name_scope("dropout"):
    src_label_rep = tf.nn.dropout(src_label_net.output, keep_prob)
    tar_label_rep = tf.nn.dropout(tar_label_net.output, keep_prob)
    src_defn_rep = tf.nn.dropout(src_defn_net.output, keep_prob)
    tar_defn_rep = tf.nn.dropout(tar_defn_net.output, keep_prob)
    src_cont_rep = tf.nn.dropout(src_cont_net.output, keep_prob)
    tar_cont_rep = tf.nn.dropout(tar_cont_net.output, keep_prob)

print src_label_rep
S = [src_label_rep, src_defn_rep, src_cont_rep]
T = [tar_label_rep, tar_defn_rep, tar_cont_rep]

sim_matrix = []
for s in S:
    for t in T:
        sim = tf.contrib.losses.cosine_distance(s, t, 0)
        sim_matrix.append(sim)
print sim_matrix
f = tf.pack(sim_matrix)
        
W_out = tf.get_variable("W_out", shape=[len(S) * len(T), 1],
                        initializer=tf.contrib.layers.xavier_initializer())
b_out = tf.Variable(tf.constant(0.1), name="b")

print f
print W_out
print b_out
#pred_sim = tf.nn.xw_plus_b(tf.transpose(f), W_out, b_out)
pred_sim = tf.matmul(tf.transpose(f), W_out) + b_out

# CalculateMean cross-entropy loss
l2_reg_lambda = 0.0
# Keeping track of l2 regularization loss (optional)
l2_loss = tf.constant(0.0)
with tf.name_scope("loss"):
    losses = tf.contrib.losses.sum_of_squares(pred_sim, y)
    loss = tf.reduce_mean(losses) + l2_reg_lambda * l2_loss
    optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(loss)

# Initializing the variables
init = tf.initialize_all_variables()

# Add ops to save and restore all the variables.
saver = tf.train.Saver()

print 'Model is defined...'

# Launch the graph
with tf.Session() as sess:
    if ans:
        sess.run(init)
        step = 1
        n_processed = 0
        n_processed_total = 0
        shuffled_idx = range(len(train_labels))
        random.shuffle(shuffled_idx)
        
        while n_processed_total < training_iters:
            if n_processed >= len(train_labels):
                n_processed = 0
                shuffled_idx = range(len(train_labels))
                random.shuffle(shuffled_idx)
            selected_idx = shuffled_idx[n_processed:min(n_processed + default_batch_size, len(train_labels))]
            n_processed += len(selected_idx)
            n_processed_total += len(selected_idx)
            batch_size = len(selected_idx)
            batch_xs = np.array([create_sentence_embeddings_char_plus_word(train_pairs[idx], MAX_DOCUMENT_LENGTH) for idx in selected_idx])
            batch_ys = np.array([train_labels[idx] for idx in selected_idx])
            
            batch_xs = batch_xs.reshape((batch_size, n_steps, n_input))
            batch_ys = batch_ys.reshape((batch_size, 1))
            fdict = {x: batch_xs, y: batch_ys, keep_prob: dropout}
            sess.run(optimizer, feed_dict=fdict)
            
            if step % display_step == 0:
                # Calculate batch accuracy
                acc = sess.run(accuracy, feed_dict={x: batch_xs, y: batch_ys, keep_prob: 1.})
                # Calculate batch loss
                loss_val = sess.run(loss, feed_dict={x: batch_xs, y: batch_ys, keep_prob: 1.})
                print "Iter " + str(n_processed_total) + ", Minibatch Loss= " + "{:.6f}".format(loss_val) + \
                      ", Training Accuracy= " + "{:.5f}".format(acc)
            step += 1
        print "Optimization Finished!"
        save_path = saver.save(sess, "cnn_model_character_cos_sim.ckpt")
        print("Model saved in file: %s" % save_path)
    else:
        # Restore variables from disk.
        saver.restore(sess, "cnn_model_character_cos_sim.ckpt")
        print("Model restored.")
    
    print 'Automatic Test Phase: '
    print 'Size: ', len(test_labels)
    matching = []
    for i in range(len(test_labels)):
        is_hit = False
        sim_values = []
        batch_xs = np.zeros((num_classes, n_steps, n_input))
        for j in range(num_classes):
            pair = ' '.join([test_pairs[i][0], mt_dict[j][1].encode("utf-8")])
            batch_xs[j] = create_sentence_embeddings_char_plus_word(pair, MAX_DOCUMENT_LENGTH)
        batch_xs = batch_xs.reshape((num_classes, n_steps, n_input))
        batch_ys = np.ones(num_classes)
        batch_ys = batch_ys.reshape((num_classes, 1))
        fdict = {x: batch_xs, y: batch_ys, keep_prob: 1.}
        sim_prob_val = sess.run(predictions, feed_dict=fdict)
        sim_values = [ val[0] for val in sim_prob_val]
        predict_idx = np.array(sim_values).argmax()
        correct_idx = test_mt_indice[i]
        top_three = np.array(sim_values).argsort()[-3:][::-1]
        print top_three
        for hit in top_three:
            if hit == correct_idx:
                matching.append(1)
                is_hit = True
                break
        if not is_hit:
            matching.append(0)
        print i, ' / ', j , ' ', is_hit, ' description: ', test_pairs[i][0], ' pred: ', mt_dict[predict_idx][1], ' true: ', mt_dict[correct_idx][1], predict_idx, correct_idx                
    precision = np.mean(np.array(matching))
    print "Test Accuracy by three hits = " + "{:.5f}".format(precision)