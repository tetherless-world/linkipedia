
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
    query_str = '''SELECT DISTINCT ?mt ?label ?comment ?defn
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
            result[i] = {'uri' : row[0], 'label' : row[1], 'comment' : row[2], 'defn' : row[3]}
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
embeddings = PreTrainEmbedding('/Users/jason/EclipseWorkspace/GoogleNews-vectors-negative300.bin.gz', embedding_size)

from data_helper import DataHelper
helper = DataHelper()
def get_train_pair(ontology, manu_anno, negative_label_size):
    mt_indices = []
    labels = []
    samples = []
    for d in manu_anno:
        correct_idx = 0
        for idx in ontology:
            if ontology[idx]['uri'] == URIRef(d['mt']):
                labels.append(1)
                mt_indices.append(idx)
                correct_idx = idx
                samples.append({'slabel':ontology[idx]['label'], 'tlabel':helper.get_label(d), 
                    'sdefn':ontology[idx]['comment'], 'tdefn':helper.get_defn(d),
                    'scontext':ontology[idx]['defn'], 'tcontext':helper.get_context(d)})
                break
        if negative_label_size > 0:
            shuffled_idx = range(len(ontology.keys()))
            random.shuffle(shuffled_idx)
            num_neg_label = 0
            for idx in shuffled_idx:
                if not idx == correct_idx:
                    labels.append(0)
                    mt_indices.append(-1)
                    samples.append({'slabel':ontology[idx]['label'], 'tlabel':helper.get_label(d), 
                    'sdefn':ontology[idx]['comment'], 'tdefn':helper.get_defn(d),
                    'scontext':ontology[idx]['defn'], 'tcontext':helper.get_context(d)})
                    num_neg_label += 1
                    if num_neg_label == negative_label_size:
                        break
    return samples, labels, mt_indices

def get_test_pair_label(ontology, manu_anno):
    mt_indices = []
    labels = []
    samples = []
    for d in manu_anno:
        for idx in ontology:
            if ontology[idx]['uri'] == URIRef(d['mt']):
                labels.append(1)
                mt_indices.append(idx)
                samples.append({'slabel':ontology[idx]['label'], 'tlabel':helper.get_label(d), 
                    'sdefn':ontology[idx]['comment'], 'tdefn':helper.get_defn(d),
                    'scontext':ontology[idx]['defn'], 'tcontext':helper.get_context(d)})
                break
    return samples, labels, mt_indices

train_samples, train_labels, train_mt_indices = get_train_pair(mt_dict, training_manu_anno, 3)
test_samples, test_labels, test_mt_indice = get_test_pair_label(mt_dict, testing_manu_anno)
print 'Training label length: ', len(train_labels)
print 'Testing label length: ', len(test_labels)

# Network Parameters
learning_rate = 0.001
training_iters = 20000
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
y = tf.placeholder("float", [None, 1])
keep_prob = tf.placeholder(tf.float32)

from conv_net import ConvNet
cn = ConvNet()
src_label_net = cn.conv_net(slabel)
tar_label_net = cn.conv_net(tlabel)
src_defn_net = cn.conv_net(sdefn)
tar_defn_net = cn.conv_net(tdefn)
src_cont_net = cn.conv_net(scontext)
tar_cont_net = cn.conv_net(tcontext)

print src_label_net.get_shape()

with tf.name_scope("dropout"):
    src_label_rep = tf.nn.dropout(src_label_net, keep_prob)
    tar_label_rep = tf.nn.dropout(tar_label_net, keep_prob)
    src_defn_rep = tf.nn.dropout(src_defn_net, keep_prob)
    tar_defn_rep = tf.nn.dropout(tar_defn_net, keep_prob)
    src_cont_rep = tf.nn.dropout(src_cont_net, keep_prob)
    tar_cont_rep = tf.nn.dropout(tar_cont_net, keep_prob)

print src_label_rep
S = [src_label_rep, src_defn_rep, src_cont_rep]
T = [tar_label_rep, tar_defn_rep, tar_cont_rep]

sim_matrix = []
for s in S:
    for t in T:
        # Compute the cosine similarity between minibatch examples and all embeddings.
        s_norm = tf.sqrt(tf.reduce_sum(tf.square(s), 1, keep_dims=True))
        normalized_s = s
        t_norm = tf.sqrt(tf.reduce_sum(tf.square(t), 1, keep_dims=True))
        normalized_t = t
        sim = tf.diag_part(tf.matmul(normalized_s, normalized_t, transpose_b=True))
        sim_matrix.append(sim)
print sim_matrix
sim = tf.concat(0, sim_matrix)
print sim
sim_vec = tf.reshape(sim, [-1, len(S) * len(T)])
        
W_out = tf.get_variable("W_out", shape=[len(S) * len(T), 1],
                        initializer=tf.contrib.layers.xavier_initializer())
b_out = tf.Variable(tf.constant(0.1), name="b")

#pred_sim = tf.nn.xw_plus_b(tf.transpose(f), W_out, b_out)
pred_sim = tf.matmul(sim_vec, W_out) + b_out

# CalculateMean cross-entropy loss
l2_reg_lambda = 0.0
# Keeping track of l2 regularization loss (optional)
l2_loss = tf.constant(0.0)
with tf.name_scope("loss"):
    losses = tf.square(y - pred_sim)
    loss = tf.reduce_sum(tf.reduce_mean(losses, 1)) + l2_reg_lambda * l2_loss
    optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(loss)

# Initializing the variables
init = tf.initialize_all_variables()

# Add ops to save and restore all the variables.
saver = tf.train.Saver()

print 'Model is defined...'

import numpy as np

# Launch the graph
with tf.Session() as sess:
    if ans:
        sess.run(init)
        step = 1
        n_processed = 0
        n_processed_total = 0
        shuffled_idx = range(len(train_labels))
        random.shuffle(shuffled_idx)
        
        while step < training_iters:
            if n_processed >= len(train_labels):
                n_processed = 0
                shuffled_idx = range(len(train_labels))
                random.shuffle(shuffled_idx)
            selected_idx = shuffled_idx[n_processed:min(n_processed + default_batch_size, len(train_labels))]
            n_processed += len(selected_idx)
            n_processed_total += len(selected_idx)
            batch_size = len(selected_idx)

            batch_slabel = np.array([embeddings.create_word_level_embeddings(train_samples[idx]['slabel'], label_len) for idx in selected_idx])
            batch_tlabel = np.array([embeddings.create_word_level_embeddings(train_samples[idx]['tlabel'], label_len) for idx in selected_idx])
            batch_sdefn = np.array([embeddings.create_word_level_embeddings(train_samples[idx]['sdefn'], defn_len) for idx in selected_idx])
            batch_tdefn = np.array([embeddings.create_word_level_embeddings(train_samples[idx]['tdefn'], defn_len) for idx in selected_idx])
            batch_scontext = np.array([embeddings.create_word_level_embeddings(train_samples[idx]['scontext'], context_len) for idx in selected_idx])
            batch_tcontext = np.array([embeddings.create_word_level_embeddings(train_samples[idx]['tcontext'], context_len) for idx in selected_idx])
            batch_ys = np.array([train_labels[idx] for idx in selected_idx])
            
            batch_slabel = batch_slabel.reshape((batch_size, label_len, n_input))
            batch_tlabel = batch_tlabel.reshape((batch_size, label_len, n_input))
            batch_sdefn = batch_sdefn.reshape((batch_size, defn_len, n_input))
            batch_tdefn = batch_tdefn.reshape((batch_size, defn_len, n_input))
            batch_scontext = batch_scontext.reshape((batch_size, context_len, n_input))
            batch_tcontext = batch_tcontext.reshape((batch_size, context_len, n_input))
            batch_ys = batch_ys.reshape((batch_size, 1))
            fdict = {slabel: batch_slabel, 
                    tlabel: batch_tlabel, 
                    sdefn: batch_tdefn,
                    tdefn: batch_tdefn,
                    scontext: batch_scontext,
                    tcontext: batch_tcontext,
                    y: batch_ys, 
                    keep_prob: dropout}
            sess.run(optimizer, feed_dict=fdict)
            
            if step % display_step == 0:
                # Calculate batch accuracy
                # Calculate batch loss
                fdict[keep_prob] = 1.0
                loss_val = sess.run(loss, feed_dict=fdict)
                print "Iter " + str(step) + ", Minibatch Loss= " + "{:.6f}".format(loss_val)
            step += 1
        print "Optimization Finished!"
        save_path = saver.save(sess, "cnn_model_character_sim.ckpt")
        print("Model saved in file: %s" % save_path)
    else:
        # Restore variables from disk.
        saver.restore(sess, "cnn_model_character_sim.ckpt")
        print("Model restored.")
    
    print 'Automatic Test Phase: '
    print 'Size: ', len(test_labels)
    matching = []
    for i in range(len(test_labels)):
        is_hit = False
        sim_values = []
        batch_size = 1
        batch_slabel = np.array([embeddings.create_word_level_embeddings(test_samples[i]['slabel'], label_len)])
        batch_tlabel = np.array([embeddings.create_word_level_embeddings(test_samples[i]['tlabel'], label_len)])
        batch_sdefn = np.array([embeddings.create_word_level_embeddings(test_samples[i]['sdefn'], defn_len)])
        batch_tdefn = np.array([embeddings.create_word_level_embeddings(test_samples[i]['tdefn'], defn_len)])
        batch_scontext = np.array([embeddings.create_word_level_embeddings(test_samples[i]['scontext'], context_len)])
        batch_tcontext = np.array([embeddings.create_word_level_embeddings(test_samples[i]['tcontext'], context_len)])
        batch_ys = np.array([train_labels[i]])
        
        batch_slabel = batch_slabel.reshape((batch_size, label_len, n_input))
        batch_tlabel = batch_tlabel.reshape((batch_size, label_len, n_input))
        batch_sdefn = batch_sdefn.reshape((batch_size, defn_len, n_input))
        batch_tdefn = batch_tdefn.reshape((batch_size, defn_len, n_input))
        batch_scontext = batch_scontext.reshape((batch_size, context_len, n_input))
        batch_tcontext = batch_tcontext.reshape((batch_size, context_len, n_input))
        batch_ys = batch_ys.reshape((batch_size, 1))
        fdict = {slabel: batch_slabel, 
                tlabel: batch_tlabel, 
                sdefn: batch_tdefn,
                tdefn: batch_tdefn,
                scontext: batch_scontext,
                tcontext: batch_tcontext,
                y: batch_ys, 
                keep_prob: 1.0}

        pred_sim_val = sess.run(pred_sim, feed_dict=fdict)
        print i
        print 'pred_sim_val: ', pred_sim_val
        print 'true_sim_val: ', test_labels[i]
        #sim_values = [ val[0] for val in pred_sim_val]
        #predict_idx = np.array(sim_values).argmax()
        #correct_idx = test_mt_indice[i]
        #top_three = np.array(sim_values).argsort()[-3:][::-1]
        #print top_three
        #for hit in top_three:
        #    if hit == correct_idx:
        #        matching.append(1)
        #        is_hit = True
        #        break
        #if not is_hit:
        #    matching.append(0)
        #print i, ' / ', j , ' ', is_hit, ' description: ', test_samples[i][0], ' pred: ', mt_dict[predict_idx][1], ' true: ', mt_dict[correct_idx][1], predict_idx, correct_idx                
    #precision = np.mean(np.array(matching))
    #print "Test Accuracy by three hits = " + "{:.5f}".format(precision)


