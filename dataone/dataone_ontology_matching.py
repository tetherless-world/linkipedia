#!/usr/bin/env python
# coding: utf-8

# In[1]:

from rdflib import *
import os, re
from multiprocessing import Manager, Process, Pool, Queue, Event, JoinableQueue, cpu_count, Value, Lock
from Queue import Empty
import xml.etree.ElementTree as ET

manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_F_dev/manual_annotations.tsv.txt'
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

from nltk.stem import *
from nltk.tokenize import WordPunctTokenizer
stemmer = PorterStemmer()

datasets = urllib2.urlopen(data_url).read().split("\n")[1:]

from eml2owl import create_ontology as get_eml

nt_file = '../dataone-index/NTriple/merged.nt'

graph = ConjunctiveGraph()
graph.load(nt_file, format="n3")

stopwords = set([ ',', '.', ';', ':', '?', '!', '-' ])

labels = [
    URIRef('http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym'),
    URIRef('http://www.geneontology.org/formats/oboInOwl#hasExactSynonym'),
    URIRef('http://www.w3.org/2000/01/rdf-schema#label'),
    URIRef('http://www.w3.org/2004/02/skos/core#altLabel'),
    URIRef('http://www.w3.org/2004/02/skos/core#prefLabel'),
    URIRef('http://tool.eal.org/urlName'),
    URIRef('http://purl.org/dc/elements/1.1/title'),
    URIRef('http://purl.org/dc/terms/title')
]

def compute_term_vector(resource):
    terms = collections.defaultdict(float)
    try:
        qname = resource.graph.compute_qname(resource.identifier, generate=True)
        qname = qname.split(":")[-1]
        terms.append(qname.lower())
    except:
        pass
    for label in labels:
        for value in resource[label]:
            value = value.value.replace('_'," ")
            for term in [t for t in WordPunctTokenizer().tokenize(value) if t not in stopwords]:
                terms[term.lower()] += 1
    tf = collections.defaultdict(float)
    term_counts = terms.values()
    if len(term_counts) > 0:
        max_f = max(term_counts)
        for url, count in terms.items():
            tf[url] += 0.5 + 0.5 * count / max_f
    return tf

class Node:
    pass

def vectorize_ontology(graph, idf = None):
    classes = []
    for c in graph.subjects(RDF.type, OWL.Class):
        if isinstance(c, BNode):
            continue
        r = graph.resource(c)
        node = Node()
        node.resource = r
        node.identifier = c
        node.tf = compute_term_vector(r)
        classes.append(node)

    if idf == None:
        idf = compute_idf(classes)

    for node in classes:
        node.concept_vector = concept_vector(node, idf)
        node.magnitude = vector_magnitude(node.concept_vector)
    return classes, idf
        
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

def cosine_distance(a, b):
    ''' See https://en.wikipedia.org/wiki/Cosine_similarity for definition and formula'''
    a_iter = iter(a.concept_vector)
    b_iter = iter(b.concept_vector)
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

target_classes, idf = vectorize_ontology(graph)
subtree = set(graph.transitive_subjects(RDFS.subClassOf, oboe.MeasurementType))
target_class_subtree = [x for x in target_classes if x.identifier in subtree and x.identifier != oboe.MeasurementType]
targets = dict([(x.identifier, x) for x in target_class_subtree])


def get_manual_annotations(nSize=-1):
    resp = requests.get(manual_annotation_url)
    annotations = [x for x in csv.DictReader(StringIO(resp.text,newline=None), delimiter="\t")]
    resp.close()
    result = collections.defaultdict(set)
    for annotation in annotations:
        if len(annotation['class_id_int'].strip()) == 0:
            continue
        package = annotation['pkg_id']
        #if package != 'johnwu01.3.18':
        #    continue
        uri = 'http://purl.dataone.org/odo/ECSO_%08d'%int(annotation['class_id_int'].strip())
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

def work(id, jobs, result, processed_count):
    while True:
        try:
            dataset = jobs.get(timeout=10)
        except Empty:
            print "Nothing left in job queue!"
            processed.set()
            break
        try:
            r = set()
            print dataset
            source_graph = get_eml(dataset)
            source_classes, local_idf = vectorize_ontology(source_graph, idf)
            source_subtree = set(source_graph.transitive_subjects(RDFS.subClassOf, oboe.MeasurementType))
            source_class_subtree = [x for x in source_classes if x.identifier in source_subtree and x.identifier != oboe.MeasurementType]
            sources = dict([(x.identifier, x) for x in source_class_subtree])

            distances = pairwise_sparsedist(source_class_subtree, target_class_subtree)
            #print distances
            for c, dist in distances.items():
                target, score = min(dist.items(), key=lambda x: x[1])
                #print sources[c].concept_vector, targets[target].concept_vector
                if score < 1:
                    r.add(target)
            
            processed_count.increment()
            print dataset, processed_count.value(), r
            result.put((dataset, r))
        except Exception as e:
            print "Error processing dataset:", dataset, e
        finally:
            jobs.task_done()
            
            
def main():
    jobs = JoinableQueue()
    result = JoinableQueue()


    print len(datasets)
    numToProcess = -1
    scores = pd.DataFrame(columns=['precision','recall','fmeasure',
                                   'numResult','minScore','topHits',
                                   'contentWeight','relationWeight'])
    manual_annotations = get_manual_annotations(numToProcess)
    manual_tuples = get_ir_tuples(manual_annotations)

    for key in manual_annotations.keys():
        jobs.put(key)

    processed_count = Counter()
        
    for i in xrange(NUMBER_OF_PROCESSES):
        p = Process(target=work, args=(i, jobs, result, processed_count))
        p.daemon = True
        p.start()

    #work(1, jobs, result, processed_count)

    automated_annotations = {}

    jobs.join()

    while not result.empty():
        dataset, classes = result.get()
        automated_annotations[dataset] = classes
        result.task_done()

    automated_tuples = get_ir_tuples(automated_annotations)
    hits = manual_tuples & automated_tuples
    misses = manual_tuples - automated_tuples
    
    precision = float(len(hits)) / len(automated_tuples)
    recall = float(len(hits)) / len(manual_tuples)
    fmeasure = 2 * (precision * recall) / (precision + recall)
    # print '\t'.join([str(x) for x in [precision, recall, fmeasure,
    #                              numResult, minScore, topHits]])
    scores = scores.append(dict(precision=precision, recall=recall, fmeasure=fmeasure),
                        ignore_index=True)

    print scores

if __name__ == '__main__':
    main()
