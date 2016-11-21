#!/usr/bin/env python
# coding: utf-8

topHits = 3
context_steps = 0
maxDistance = 0.9
useLSA = False
minNgrams = 1
maxNgrams = 1
char_ngram_size = -1

weighted_kmeans_clustering_passes = 0

from rdflib import *
import rdflib.resource
import os, re, sys
from multiprocessing import Manager, Process, Pool, Queue, Event, JoinableQueue, cpu_count, Value, Lock
from Queue import Empty
import xml.etree.ElementTree as ET
from rdflib.extras.infixowl import Class
import numpy as np

query_file = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/queries/uc52_queries_all.csv'
ground_truth_file = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/ground_truth/test_corpus_f_groundtruth_carbon_flux_queries.csv'

manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_F_dev/manual_annotations.tsv.txt'
manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/manual_annotation/joined_annotations.csv'
data_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_E_id_list.txt'
dataset_service_url = 'https://cn.dataone.org/cn/v1/query/solr/?wt=json&fl=title,abstract,attributeName,attributeDescription&q=identifier:"%s"'
service_url = 'http://localhost:8080/annotate/annotate/'
entity_url = 'http://localhost:8080/annotate/vlinking3/'

oboe = Namespace('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#')

# measurement = URIRef('http://purl.dataone.org/odo/ECSO_00000039')
measurement = oboe.MeasurementType
# entity = URIRef('http://purl.dataone.org/odo/ECSO_00000525')
Entity = oboe.Entity
Characteristic = oboe.Characteristic
unit = URIRef('http://purl.obolibrary.org/obo/UO_0000000')
quality = URIRef('http://purl.obolibrary.org/obo/PATO_0000001')

oboe_char = Namespace('http://ecoinformatics.org/oboe/oboe.1.2/oboe-characteristics.owl#')
cmo = Namespace('http://purl.org/twc/ontologies/cmo.owl#')
skos = Namespace('http://www.w3.org/2004/02/skos/core#')
_prefix = Namespace('http://purl.dataone.org/odo/ECSTRA_')

eml=Namespace('https://cn.dataone.org/cn/v2/object/')

class Counter(object):
    def __init__(self, initval=0):
        self.val = Value('i', initval)
        self.lock = Lock()

    def increment(self):
        with self.lock:
            self.val.value += 1

    def value(self):
        with self.lock:
            return self.val.value


import csv, urllib, json, urllib2
import pandas as pd
import collections
import json
import base64
import random
import datetime
import requests
from math import log10
from rdflib.compare import to_isomorphic
from io import StringIO

from sklearn.feature_extraction import DictVectorizer
from sklearn.decomposition import TruncatedSVD

import nltk
nltk.download(['punkt','wordnet'])
from nltk.stem import *
from nltk.tokenize import WordPunctTokenizer
from nltk.corpus import wordnet as wn

stemmer = PorterStemmer()

from eml2owl import create_ontology as get_eml

nt_file = '../dataone/dataone-index/NTriple/merged.nt'
nt_file = 'dataone-index/NTriple/d1-ESCO-imported-2.nt'
#nt_file = 'ecso-old-labels.nt'
graph = ConjunctiveGraph()
graph.load(nt_file, format="n3")

def expand_labels(graph):
    for c, label in graph.query('select ?c ?label where { ?c rdfs:subClassOf+ [rdfs:label ?label] } '):
        graph.add((c, skos.hiddenLabel, label))

expand_labels(graph)

stopwords = set([ ',', '.', ';', ':', '?', '!', '-' ])

groupings = {
    URIRef('http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym') : "label",
    URIRef('http://www.geneontology.org/formats/oboInOwl#hasExactSynonym'): "label",
    URIRef('http://www.w3.org/2000/01/rdf-schema#label'): "label",
    URIRef('http://www.w3.org/2004/02/skos/core#altLabel'): "label",
    URIRef('http://www.w3.org/2004/02/skos/core#prefLabel'): "label",
    URIRef('http://www.w3.org/2004/02/skos/core#hiddenLabel'): "label",
    URIRef('http://tool.eal.org/urlName'): "label",
    URIRef('http://purl.org/dc/elements/1.1/title'): "label",
    URIRef('http://purl.org/dc/terms/title'): "label",
    URIRef('http://purl.org/dc/elements/1.1/description'): "description",
    URIRef('http://purl.org/dc/terms/description'): "description",
    URIRef('http://www.w3.org/2000/01/rdf-schema#comment'): "description",
    URIRef('http://www.w3.org/2004/02/skos/core#definition'): "description",
}

weights = {
    'label': 1,
    'description': 0.5,
    'context': 0.25,
    RDFS.subClassOf: 0.75,
    RDF.type: 0.5
}
distance_weight = 0.25

def ngrams(terms, min_n=minNgrams, max_n=maxNgrams):
    for i in range(len(terms)):
        for j in range(i+min_n, i+max_n+1):
            gram = terms[i:j]
            if len(gram) == j - i:
                yield ' '.join(gram)
                
def char_ngrams(term, char_ngram_size=-1):
    if char_ngram_size == -1:
        return [term]
    else:
        return nltk.ngrams(term, char_ngram_size)
    
def compute_term_vector(resource):
    terms = collections.defaultdict(float)
    try:
        qname = resource.graph.compute_qname(resource.identifier, generate=True)
        qname = qname.split(":")[-1]
        terms.append(qname.lower())
    except:
        pass
    for p, value in resource.predicate_objects():
        p = p.identifier
        if p in groupings:
            p = groupings[p]
        weighting = weights['context']
        if p in weights:
            weighting = weights[p]
        if isinstance(value, rdflib.resource.Resource) and isinstance(value.identifier, URIRef):
            terms[(p, value.identifier)] += weighting
        elif isinstance(value, Literal):
            value = unicode(value.value).replace('_'," ")
            for term in ngrams([t for t in WordPunctTokenizer().tokenize(value) if t not in stopwords]):
                for char_ngram in char_ngrams(term.lower(), char_ngram_size):
                    terms[('context',char_ngram)] = weights['context']
                    terms[(p, char_ngram)] += weighting
    tf = collections.defaultdict(float)
    term_counts = terms.values()
    if len(term_counts) > 0:
        max_f = max(term_counts)
        for url, count in terms.items():
            tf[url] += 0.5 + 0.5 * count / max_f
    return tf

class Node:
    pass

def vectorize_ontology(graph, idf = None, lsa = None):
    classes = []
    class_map = {}
    for c in list(graph.subjects(RDF.type, OWL.Class)) + list(graph.subjects(RDF.type, OWL.Restriction)):
        if isinstance(c, BNode):
            continue
        r = graph.resource(c)
        node = Node()
        node.resource = r
        node.identifier = c
        node.tf = compute_term_vector(r)
        classes.append(node)
        class_map[c] = node

    for i in range(context_steps):
        for node in classes:
            for p, value in node.resource.predicate_objects():
                p = p.identifier
                if p in groupings:
                    p = groupings[p]
                if isinstance(value, rdflib.resource.Resource) and value.identifier in class_map:
                    other_node = class_map[value.identifier]
                    weight = distance_weight
                    if isinstance(value.identifier, BNode): # bnodes are essentially local.
                        weight = 1
                    for key, value in other_node.tf.items():
                        if len(key) == i + 2:
                            node.tf[('related',key[:-1])] += value * distance_weight
        
    if idf == None:
        idf = compute_idf(classes)

    for node in classes:
        node.concept_vector = concept_vector(node, idf)
        node.magnitude = vector_magnitude(node.concept_vector)
    if useLSA:
        nodes, lsa = compute_lsa(classes, lsa)
        for node in classes:
            node.magnitude = vector_magnitude(node.lsa_vector)
    return classes, idf, lsa

def compute_lsa(nodes, model=None):
    fit = False
    matrix = None
    if model is None:
        fit = True
        model = TruncatedSVD(n_components=100)
        model.vectorizer = DictVectorizer(sparse=True)
        matrix = model.vectorizer.fit_transform([dict(node.concept_vector) for node in nodes])
        model.fit(matrix)
    else:
        matrix = model.vectorizer.transform([dict(node.concept_vector) for node in nodes])
    result = model.transform(matrix)
    for i, node in enumerate(nodes):
        node.lsa_vector = [x for x in enumerate(result[i])]
    return nodes, model
        
def compute_idf(nodes):
    counts = collections.defaultdict(float)
    for node in nodes:
        for concept in node.tf.keys():
            counts[concept] += 1
    result = collections.defaultdict(float)
    result.update(dict([(concept, log10(len(nodes)/f)) for concept, f in counts.items()]))
    return result

def concept_vector(node, idf):
    return sorted([(concept, f * idf[concept]) for concept, f in node.tf.items()], key=lambda x: x[0])

def vector_magnitude(vector):
    return sum([x**2 for concept, x in vector]) ** 0.5

def get_vector(x):
    if useLSA:
        return x.lsa_vector
    else:
        return x.concept_vector

def cosine_distance(a, b, key=get_vector):
    ''' See https://en.wikipedia.org/wiki/Cosine_similarity for definition and formula'''
    a_iter = iter(key(a))
    b_iter = iter(key(b))
    product = float(0)
    try:
        a_current = next(a_iter)
        b_current = next(b_iter)
        while True:
            if a_current[0] > b_current[0]:
                b_current = next(b_iter)
            elif a_current[0] < b_current[0]:
                a_current = next(a_iter)
            else:
                product += a_current[1] * b_current[1]
                a_current = next(a_iter)
                b_current = next(b_iter)
    except StopIteration:
        # one of the iterators ran out of items.
        pass
    if a.magnitude == 0 or b.magnitude == 0:
        return 1.0
    result = 1.0 - product / (a.magnitude * b.magnitude)
    return result

def sparsedist(vectors, ident=None, metric=cosine_distance):
    if ident is None:
        ident = lambda x: x.identifier
    vector_ids = sorted([ident(v) for v in vectors])
    result = pd.DataFrame(columns=vector_ids, index=vector_ids)
    for i in vectors:
        i_id = ident(i)
        for j in vectors:
            j_id = ident(j)
            if np.isnan(result.at[i_id, j_id]):
                if i_id == j_id:
                    distance = 0
                else:
                    distance = metric(i, j)
                result.at[i_id, j_id] = distance
                result.at[j_id, i_id] = distance
    return result

def pairwise_sparsedist(source_vectors, target_vectors, ident=None, metric=cosine_distance):
    if ident is None:
        ident = lambda x: x.identifier
    result = {}
    for sv in source_vectors:
        dists = {}
        result[ident(sv)] = dists
        for tv in target_vectors:
            distance = 0
            if ident(sv) == ident(tv):
                distance = 0
            else:
                distance = metric(sv, tv)
            dists[ident(tv)] = distance
    return result

target_classes, idf, lsa_model = vectorize_ontology(graph)
subtree = set(graph.transitive_subjects(RDFS.subClassOf, oboe.MeasurementType))
target_class_subtree = [x for x in target_classes if x.identifier in subtree and x.identifier != oboe.MeasurementType]
targets = dict([(x.identifier, x) for x in target_class_subtree])

def get_queries():
    resp = requests.get(query_file)
    queries = dict([(x['Query_ID'], URIRef(x['Query_Frag'].split('"')[1]))
                    for x in csv.DictReader(StringIO(resp.text,newline=None), delimiter=",")
                    if x['SOLR_Index_Type'] == 'esor_cosine'])
    return queries

queries = get_queries()

def get_ground_truth():
    resp = requests.get(ground_truth_file)
    datasets = []
    queries = collections.defaultdict(set)
    for dataset in csv.DictReader(StringIO(resp.text,newline=None), delimiter=","):
        dataset_id = dataset['Dataset_ID']
        datasets.append(dataset_id)
        for i in range(1,11):
            query_id = 'q%s'%i
            if dataset[query_id] == '1':
                queries[query_id].add(dataset_id)
    return datasets, queries

datasets, ground_truth = get_ground_truth()
    
def get_manual_annotations(nSize=-1):
    resp = requests.get(manual_annotation_url)
    annotations = [x for x in csv.DictReader(StringIO(resp.text,newline=None), delimiter=",")]
    resp.close()
    result = collections.defaultdict(set)
    for annotation in annotations:
        if len(annotation['class_uri'].strip()) == 0:
            continue
        package = annotation['pkg_id']
        #if package != 'johnwu01.3.18':
        #    continue
        uri = annotation['class_uri'].strip() #'http://purl.dataone.org/odo/ECSO_%08d'%int(annotation['class_id_int'].strip())
        result[package].add(URIRef(uri))
        if nSize > 0 and len(result) >= nSize:
            print 'Size of the manual annotations is %d' % nSize 
            break
    return result

def get_ir_tuples(annotations):
    result = set()
    for dataset, classes in annotations.items():
        for c in classes:
            result.add((dataset, c))
    return result

NUMBER_OF_PROCESSES = 6

processed = Event()

dataset_cache = {}

def compute_dataset(dataset):
    if dataset not in dataset_cache:
        g = get_eml(dataset)
        g_classes, local_idf, lsa = vectorize_ontology(g, idf, lsa_model)
        g.classes = g_classes
        dataset_cache[dataset] = g
    return dataset_cache[dataset]

def train_lsa(datasets):
    nodes = []
    nodes.extend(target_classes)
    for i, dataset in enumerate(datasets):
        g = compute_dataset(dataset)
        nodes.extend(g.classes)
        sys.stdout.write('\r')
        sys.stdout.write(str(i+1))
        sys.stdout.flush()

    n, model = compute_lsa(nodes)
    return model

def train_kmeans(datasets, target_classses):
    vectors = dict([(c.identifier, c) for c in target_class_subtree])
    for c in target_classes:
        c.members = []
    for i, dataset in enumerate(datasets):
        source_graph = compute_dataset(dataset)
        source_subtree = set(source_graph.transitive_subjects(RDFS.subClassOf, oboe.MeasurementType))
        source_class_subtree = [x for x in source_graph.classes if x.identifier in source_subtree and x.identifier != oboe.MeasurementType]
        sources = dict([(x.identifier, x) for x in source_class_subtree])

        distances = pairwise_sparsedist(source_class_subtree, target_class_subtree)
        for c, dist in distances.items():
            for target, score in sorted(dist.items(), key=lambda x: x[1])[:topHits]:
                if score < maxDistance:
                    target_class = vectors[target]
                    target_class.members.append((sources[c], score))
        sys.stdout.write('\r')
        sys.stdout.write(str(i+1))
        sys.stdout.flush()
    print "\nMerging..." 
    for c in target_class_subtree:
        if len(c.members) > 0:
            new_vector = collections.defaultdict(float)
            for source_class, dist in c.members:
                for key, value in source_class.concept_vector:
                    new_vector[key] += value * (1- dist)
            for key in new_vector.keys():
                new_vector[key] = new_vector[key] / len(c.members)
            for key, value in c.concept_vector:
                new_vector[key] += value
            for key in new_vector.keys():
                new_vector[key] = new_vector[key] / 2
            c.concept_vector = sorted([(key, value) for key, value in new_vector.items()], key=lambda x: x[0])
            c.magnitude = vector_magnitude(c.concept_vector)
            
def work(id, jobs, result, processed_count):
    while True:
        try:
            dataset = jobs.get(timeout=10)
        except Empty:
            print "Nothing left in job queue!"
            processed.set()
            break
        try:
            r = dict()
            #print dataset
            source_graph = compute_dataset(dataset)
            source_classes = source_graph.classes
            source_subtree = set(source_graph.transitive_subjects(RDFS.subClassOf, oboe.MeasurementType))
            source_class_subtree = [x for x in source_classes if x.identifier in source_subtree and x.identifier != oboe.MeasurementType]
            sources = dict([(x.identifier, x) for x in source_class_subtree])

            distances = pairwise_sparsedist(source_class_subtree, target_class_subtree)
            #print distances
            for c, dist in distances.items():
                hits = 0
                for target, score in sorted(dist.items(), key=lambda x: x[1]):
                #target, score = min(dist.items(), key=lambda x: x[1])
                #print sources[c].concept_vector, targets[target].concept_vector
                    if score < maxDistance and (target not in r or r[target] < score):
                        r[target] = score
                        hits += 1
                        if hits >= topHits: break
            
            processed_count.increment()
            sys.stdout.write('\r')
            sys.stdout.write(str(processed_count.value()))
            sys.stdout.flush()
            result.put((dataset, r))
        except Exception as e:
            print "Error processing dataset:", dataset, e
        finally:
            jobs.task_done()
            
            
def main():
    jobs = JoinableQueue()
    result = JoinableQueue()


    numToProcess = -1
    scores = pd.DataFrame(columns=['query','fmeasure','precision','recall',
                                   'size','maxDistance','topHits',"contextSteps"])

    print len(datasets)

    for key in datasets:
        jobs.put(key)

    processed_count = Counter()
        
    for i in xrange(NUMBER_OF_PROCESSES):
        p = Process(target=work, args=(i, jobs, result, processed_count))
        p.daemon = True
        p.start()

    #work(1, jobs, result, processed_count)

    automated_annotations = {}
    distances = {}

    jobs.join()

    dataset_index = collections.defaultdict(set)
    annotated_datasets = set()
    while not result.empty():
        dataset, classes = result.get()
        if len(classes) == 0:
            annotated_datasets.add(dataset)
        for c in classes.keys():
            dataset_index[c].add(dataset)
            owl_class = Class(c, graph=graph)
            for parent in owl_class.parents:
                dataset_index[parent.identifier].add(dataset)
        result.task_done()

    print '\n'
    
    for query, c in queries.items():
        manual = ground_truth[query]
        automated = dataset_index[c]
        hits = manual & automated
        misses = manual - automated
        precision = np.nan if len(automated) == 0 else float(len(hits)) / len(automated)
        recall = np.nan if len(manual) == 0 else float(len(hits)) / len(manual)
        if precision != 0 or recall != 0:
            fmeasure = 0 if np.isnan(precision) or np.isnan(recall) else 2 * (precision * recall) / (precision + recall)
        else:
            fmeasure = 0
        scores = scores.append(dict(query=query, size=len(manual), precision=precision, recall=recall, fmeasure=fmeasure,topHits=topHits, maxDistance=maxDistance, contextSteps = context_steps),
                        ignore_index=True)
        print "Hits for", query, c
        print '\n'.join(sorted(hits))
    print scores
    print "Annotated", len(annotated_datasets), "datasets."

if __name__ == '__main__':
    main()
