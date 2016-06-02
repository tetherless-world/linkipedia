# coding: utf-8

# In[1]:

from rdflib import *
from numpy import character
from jupyter_client.consoleapp import classes
import os, re
from multiprocessing import Manager, Process, Pool, Queue, Event, JoinableQueue, cpu_count, Value, Lock
from Queue import Empty
import xml.etree.ElementTree as ET

manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_F_dev/manual_annotations.tsv.txt'
data_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_E_id_list.txt'
dataset_service_url = 'https://cn.dataone.org/cn/v1/query/solr/?wt=json&fl=title,abstract,attributeName,attributeDescription&q=identifier:"%s"'
service_url = 'http://localhost:8080/annotate/annotate/'
entity_url = 'http://localhost:8080/annotate/vlinking3/'

oboe = Namespace('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#')

# measurement = URIRef('http://purl.dataone.org/odo/ECSO_00000039')
measurement = URIRef('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#MeasurementType')
# entity = URIRef('http://purl.dataone.org/odo/ECSO_00000525')
Entity = oboe.Entity
Characteristic = oboe.Characteristic
unit = URIRef('http://purl.obolibrary.org/obo/UO_0000000')
quality = URIRef('http://purl.obolibrary.org/obo/PATO_0000001')

oboe_char = Namespace('http://ecoinformatics.org/oboe/oboe.1.1/oboe-characteristics.owl#')
cmo = Namespace('http://purl.org/twc/ontologies/cmo.owl#')
skos = Namespace('http://www.w3.org/2004/02/skos/core#')
_prefix = Namespace('http://purl.dataone.org/odo/ECSTRA_')

eml=Namespace('https://cn.dataone.org/cn/v2/object/')

# In[2]:

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
from rdflib.extras.infixowl import *
import collections
import json
import base64
import random
import datetime
import requests
from rdflib.compare import to_isomorphic
from io import StringIO

datasets = urllib2.urlopen(data_url).read().split("\n")[1:]

def get_eml(identifier):
    r = requests.get(eml[identifier])
    xml = r.text.encode('utf-8')
    return ET.fromstring(xml)

nt_file = '../dataone-index/NTriple/merged.nt'

graph = ConjunctiveGraph()
graph.load(nt_file, format="n3")

def resolve_entity(text):
    try:
        params = {
            'numResult':numResult,
            'minScore':minScore,
            'query':text,
            'contentWeight':contentWeight,
            'relationWeight':relationWeight,
            'query':text,
        }
        response = requests.get(entity_url, params=params)
        response = response.json()
    except:
        print 'Error processing "', text, '".'
        print response.text
        return []
    urls = collections.defaultdict(float)
    for r in response['results']:
        for annotation in r['annotations']:
            if len(annotation['url']) == 0:
                continue
            urls[URIRef(annotation['url'])] += float(annotation['score'])
    return [url for url, score in sorted(urls.items(), key=lambda x: x[1], reverse=True)]

def get_query_response(text, context=None):
    try:
        params = {
            'numResult':numResult,
            'minScore':minScore,
            #'query':text,
            'contentWeight':contentWeight,
            'relationWeight':relationWeight,
        }
        data = { 'query':text }
        if context is not None:
            data['context'] = context
        data = json.dumps(data)
        response = requests.post(service_url, params=params, data=data,
                             headers={"ContentType":"application/json"}).json()
    except:
        print 'Error processing "', text, '".'
        return
    return response

def extract_mentions(response):
    urls = collections.defaultdict(float)
    for r in response['results']:
        for annotation in r['annotations']:
            urls[URIRef(annotation['url'])] += annotation['score']
    return [url for url, score in sorted(urls.items(), key=lambda x: x[1], reverse=True)]

def find_super_class(resources):
    result = collections.defaultdict(list)
    for url in resources:
        for s in graph.transitive_objects(URIRef(url), RDFS.subClassOf):
            result[s].append(URIRef(url))
    return result

def create_id():
    return base64.urlsafe_b64encode(bytes(random.random()*datetime.datetime.now().toordinal())).rstrip("=")

def lru(original_function, maxsize=1000):
    mapping = {}

    PREV, NEXT, KEY, VALUE = 0, 1, 2, 3         # link fields
    head = [None, None, None, None]        # oldest
    tail = [head, None, None, None]   #  newest
    head[NEXT] = tail

    def fn(*key):
        PREV, NEXT = 0, 1

        link = mapping.get(key, head)
        if link is head:
            value = original_function(*key)
            if len(mapping) >= maxsize:
                old_prev, old_next, old_key, old_value = head[NEXT]
                head[NEXT] = old_next
                old_next[PREV] = head
                del mapping[old_key]
            last = tail[PREV]
            link = [last, tail, key, value]
            mapping[key] = last[NEXT] = tail[PREV] = link
        else:
            link_prev, link_next, key, value = link
            link_prev[NEXT] = link_next
            link_next[PREV] = link_prev
            last = tail[PREV]
            last[NEXT] = tail[PREV] = link
            link[PREV] = last
            link[NEXT] = tail
        return value
    return fn


@lru
def check_entity_char_pair(entity, characteristic):
    # Check if there is a restriction about the entity-characteristic pair
    query_str = '''SELECT ?s
        WHERE {
          ?r1 owl:onProperty oboe:measuresEntity ; owl:someValuesFrom <%s>.
          ?r2 owl:onProperty oboe:measuresCharacteristic ;  owl:someValuesFrom <%s>.
          ?s rdfs:subClassOf ?r1, ?r2.
        }''' % (URIRef(entity), URIRef(characteristic))
    qres = list(graph.query(query_str, initNs=dict(oboe=URIRef("http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#"),
                                                   owl=OWL,rdfs=RDFS)))
    if len(qres) > 0:
        return qres[0][0]
    return None

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

def get_keywords(dataset):
    keywords = re.split('[,;]\s*', ', '.join([x.text for x in dataset.findall('dataset/keywordSet/keyword')]))
    urls = resolve_entity(','.join(keywords))
    return urls

def get_abstract_classes(dataset):
    abstract = '\n'.join([x.text for x in dataset.findall('dataset/abstract/*')])
    urls = extract_mentions(get_query_response(abstract))
    return urls

def extract_from_tags(e, tags, filter_by=lambda x: True):
    result = []
    for tag, extr in tags:
        label = e.find(tag)
        if label is not None:
            urls = extr(label.text)
            result += [u for u in urls if filter_by(u)]
    return result

def restricted_as_entity(x):
    return len(list(graph.query('''SELECT ?r WHERE {
    ?r owl:onProperty oboe:measuresEntity ;
       owl:someValuesFrom <%s>.
} limit 1'''%x, initNs=dict(oboe=oboe,owl=OWL)))) > 0

def restricted_as_characteristic(x):
    q = '''SELECT ?r WHERE {
    ?r owl:onProperty oboe:measuresCharacteristic ;
       owl:someValuesFrom <%s>.
} limit 1'''%x
    #print q
    return len(list(graph.query(q, initNs=dict(oboe=oboe,owl=OWL)))) > 0

# Linkipedia Param Setting
numResult = 20
minScore = 1
topHits = 100
contentWeight = 6
relationWeight = 6
##########################

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
            matched_classes = collections.defaultdict(set)
            r = set()
            print dataset
            eml = get_eml(dataset)
            keyword_entities = []
            abstract_entities = []
            keywords = get_keywords(eml)
            keyword_entities = find_super_class(keywords)[Entity]
            abstract = get_abstract_classes(eml)
            abstract_entities = find_super_class(abstract)[Entity]
            for datatable in eml.findall('dataset/dataTable'):
                entities = extract_from_tags(datatable,
                                             [('entityName',lambda x: extract_mentions(get_query_response(x))),
                                              ('entityDescription',lambda x: extract_mentions(get_query_response(x)))])
                for attribute in datatable.findall('attributeList/attribute'):
                    urls = extract_from_tags(attribute,
                                             [('attributeLabel',lambda x: extract_mentions(get_query_response(x))),
                                              ('attributeName',lambda x: extract_mentions(get_query_response(x))),
                                              ('attributeDescription', lambda x: extract_mentions(get_query_response(x)))
                                             ])
                    by_super = find_super_class(urls)
                    mts = set(by_super[oboe.MeasurementType])
                    #print mts
                    characteristics = by_super[Characteristic]
                    if len(mts) == 0:
                        for entity in [e for e in by_super[Entity] + entities + keyword_entities + abstract_entities if restricted_as_entity(e)]:
                            for characteristic in [c for c in characteristics if restricted_as_characteristic(c)]:
                                mt_text = graph.label(entity) + " " + graph.label(characteristic)
                                from_generated = extract_mentions(get_query_response(mt_text))
                                by_super_from_generated = find_super_class(from_generated)
                                mts = set(by_super_from_generated[oboe.MeasurementType])
                                if len(mts) > 0:
                                    print mt_text, mts
                                    break
                                #mt = check_entity_char_pair(entity, characteristic)
                                #if mt is not None:
                                #    mts.add(mt)
                                #    break
                            if len(mts) > 0:
                                break
                    r |= mts
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
    numToProcess = 100
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
    scores = scores.append(dict(precision=precision, recall=recall, fmeasure=fmeasure,
                                numResult=numResult, minScore=minScore, topHits=topHits,
                                contentWeight=contentWeight, relationWeight=relationWeight),
                        ignore_index=True)

    print scores

if __name__ == '__main__':
    main()
