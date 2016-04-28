# coding: utf-8

# In[1]:

from rdflib import *
from numpy import character
from jupyter_client.consoleapp import classes
import os
from multiprocessing import Manager, Process, Pool, Queue, Event, JoinableQueue, cpu_count
from Queue import Empty



STANFORD_DIR = 'stanford/'
STANFORD_RELEASE = '2015-12-09'

os.environ['CLASSPATH'] = ':'.join([x % (STANFORD_DIR, STANFORD_RELEASE)
                                    for x in ['%s/stanford-ner-%s','%s/stanford-parser-full-%s', '%s/stanford-postagger-%s']])
os.environ['STANFORD_MODELS'] = ':'.join([x % (STANFORD_DIR, STANFORD_RELEASE)
                                    for x in ['%s/stanford-ner-%s/classifiers',
                                              '%s/stanford-parser-full-%s',
                                              '%s/stanford-postagger-%s/models']])


manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_F_dev/manual_annotations.tsv.txt'
data_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_E_id_list.txt'
dataset_service_url = 'https://cn.dataone.org/cn/v1/query/solr/?wt=json&fl=title,abstract,attributeName,attributeDescription&q=identifier:"%s"'
service_url = 'http://localhost:8080/annotate/annotate/'

# measurement = URIRef('http://purl.dataone.org/odo/ECSO_00000039')
measurement = URIRef('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#MeasurementType')
# entity = URIRef('http://purl.dataone.org/odo/ECSO_00000525')
Entity = URIRef('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#Entity')
Characteristic = URIRef('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#Characteristic')
unit = URIRef('http://purl.obolibrary.org/obo/UO_0000000')
quality = URIRef('http://purl.obolibrary.org/obo/PATO_0000001')

oboe = Namespace('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#')
oboe_char = Namespace('http://ecoinformatics.org/oboe/oboe.1.1/oboe-characteristics.owl#')
cmo = Namespace('http://purl.org/twc/ontologies/cmo.owl#')
skos = Namespace('http://www.w3.org/2004/02/skos/core#')
_prefix = Namespace('http://purl.dataone.org/odo/ECSTRA_')


# In[2]:

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

def get_dataset_columns(identifier):
    url = dataset_service_url%identifier
    result = json.loads(urllib2.urlopen(url).read())['response']['docs'][0]['attribute']
    return result

def get_attributes(identifier):
    attributes = collections.defaultdict()
    url = dataset_service_url%identifier
    names = json.loads(urllib2.urlopen(url).read())['response']['docs'][0]['attributeName']
    descriptions = json.loads(urllib2.urlopen(url).read())['response']['docs'][0]['attributeDescription']
    for i in range(len(names)):
        if i < len(descriptions):
            attributes[names[i]] = descriptions[i]
        else:
            attributes[names[i]] = ''
    return attributes

def get_attribute_names(identifier):
    url = dataset_service_url%identifier
    result = json.loads(urllib2.urlopen(url).read())['response']['docs'][0]['attributeName']
    return result

def get_attribute_descriptions(identifier):
    url = dataset_service_url%identifier
    result = json.loads(urllib2.urlopen(url).read())['response']['docs'][0]['attributeDescription']
    return result

def get_attribute_units(identifier):
    url = dataset_service_url%identifier
    result = json.loads(urllib2.urlopen(url).read())['response']['docs'][0]['attributeUnit']
    return result

def get_dataset_abstract(identifier):
    url = dataset_service_url%identifier
    try:
        result = json.loads(urllib2.urlopen(url).read())['response']['docs'][0]['abstract']
    except KeyError:
        result = None
    return result


import nltk
nltk.download(['punkt','wordnet'])
from nltk.parse.stanford import StanfordDependencyParser
from nltk.tokenize import sent_tokenize,word_tokenize
from nltk.corpus import wordnet as wn

nt_file = '../dataone-index/NTriple/merged.nt'

graph = ConjunctiveGraph()
graph.load(nt_file, format="n3")

def traverseTree(tree, pos_dict, word_list):
    if type(tree) == nltk.tree.Tree:
        node_name = tree.label()
    else:
        node_name = tree
    pos = pos_dict.get(node_name, "None")
    if pos == 'NN' or pos == 'NNP' or pos == 'NNS' or pos == 'NNPS' or pos == 'VB':
        word_list.append(node_name)
    for subtree in tree:
        if type(subtree) == nltk.tree.Tree:
            word_list = traverseTree(subtree, pos_dict, word_list)
        else:
            node_name = subtree
            pos = pos_dict[node_name]
            if pos == 'NN' or pos == 'NNP' or pos == 'NNS' or pos == 'NNPS' or pos == 'VB':
                word_list.append(node_name)
    return word_list

def create_mention_sequence(text, parser):
    ret_list = []
    for sent in sent_tokenize(text):
        words = word_tokenize(sent)
        if '.' in words:
            if len(words) == 2:
                ret_list.append(words[0])
                continue
        else:
            if len(words) == 1:
                ret_list.append(words[0])
                continue
        #print sent
        iterTree = parser.raw_parse(sent.encode("ascii","ignore"))
        try:
            while True:
                parse = iterTree.next()
                pos_dict = {}
                for triple in parse.triples():
                    pos_dict[triple[0][0]] = triple[0][1]
                    pos_dict[triple[2][0]] = triple[2][1]
                word_list = traverseTree(parse.tree(), pos_dict, [])
                ret_list.append(word_list);
        except StopIteration:
            pass
    return ret_list

def merge_term_lists(text):
    merged_list = ''
    for term_list in text:
        merged_list += term_list
    return merged_list

def get_query_response(text, context):
    try:
        params = {
            'numResult':numResult,
            'minScore':minScore,
            #'query':text,
            'contentWeight':contentWeight,
            'relationWeight':relationWeight,
        }
        data = json.dumps({
        'query':text,
        'context':context
        })
        #response = requests.get(service_url, params=params).json()
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

def canonicalize(resource):
    digest = to_isomorphic(resource.graph).graph_digest()
    #canonical = list(iso[:RDFS.label:resource.label()])[0]
    #print str(canonical), '\n', iso.serialize(format='turtle')
    skolemized = _prefix[str(digest)]
    for s, p, o in resource.graph.triples((None, None, resource.identifier)):
        resource.graph.remove((s, p, o))
        resource.graph.add((s, p, skolemized))
    for s, p, o in resource.graph.triples((resource.identifier, None, None)):
        resource.graph.remove((s, p, o))
        resource.graph.add((skolemized, p, o))
    return resource.graph.resource(skolemized)
                
def create_class_uri():
    return _prefix[create_id()]

def by_super_class(resources):
    result = collections.defaultdict(list)
    for r in resources:
        for s in graph.transitive_objects(URIRef(r),RDFS.subClassOf):
            result[s].append(r)
    return result

def choose_class(classes, graph):
    for the_class in classes:
        bFlag = True
        for other_class in classes:
            if graph.label(other_class) not in graph.label(the_class):
                bFlag = False
        if bFlag == True:
            return the_class
    return classes[0]

def find_key_word(text, parser):
    term_list = create_mention_sequence(text, parser)
    if len(term_list) > 0:
        if len(term_list[0]) > 0:
            return term_list[0][0]
        else:
            return None
    else:
        return None

def get_entity_characteristic(text, parser):
    result = collections.defaultdict()
    characteristic = None
    for sent in sent_tokenize(text):
        words = word_tokenize(sent)
        if '.' in words:
            if len(words) == 2:
                characteristic = words[0]
                break
        else:
            if len(words) == 1:
                characteristic = words[0]
                break
        try:
            iterTree = parser.raw_parse(sent.encode("ascii","ignore"))
            while True:
                parse = iterTree.next()
                tree = parse.tree()
                if type(tree) == nltk.tree.Tree:
                    characteristic = tree.label()
                else:
                    characteristic = tree
                for triple in parse.triples():
                    if triple[0][0] == characteristic:
                        if triple[1] == 'nmod':
                            result[characteristic] = triple[2][0]
                            return result
                for triple in parse.triples():
                    if triple[0][0] == characteristic:
                        if triple[1] == 'compound':
                            result[characteristic] = triple[2][0]
                            return result
                for triple in parse.triples():
                    if triple[0][0] == characteristic:
                        if triple[1] == 'amod':
                            result[characteristic] = triple[2][0]
                            return result
        except StopIteration:
            pass
    result[characteristic] = None
    return result

def extract_abstract_mentions(response):
    urls = collections.defaultdict(list)
    for r in response['results']:
        for i in range(len(r['annotations'])):
            urls[r['entity_mention']].append(r['annotations'][i]['url'])
    return urls

def filter_classes(char_word, entity_word, super_classes, graph):
    result = collections.defaultdict(list)
    for s in super_classes[Characteristic]:
        if char_word is not None:
            if char_word.replace('_', '').lower() in graph.label(s).replace('_', '').lower():
                result[Characteristic].append(s)
    for s in super_classes[quality]:
        if char_word is not None:
            if char_word.replace('_', '').lower() in graph.label(s).replace('_', '').lower():
                result[Characteristic].append(s)
    for s in super_classes[Entity]:
        if entity_word is not None:
            if entity_word.replace('_', '').lower() in graph.label(s).replace('_', '').lower():
                result[Entity].append(s)
    for s in super_classes[measurement]:
        if char_word is not None and entity_word is not None:
            if char_word.replace('_', '').lower() in graph.label(s).replace('_', '').lower():
                if entity_word.replace('_', '').lower() in graph.label(s).replace('_', '').lower():
                    result[measurement].append(s)
    return result

def check_related_class(characteristic_word, entity_word, response, g, parser):
    candidates = set()
    resources = extract_mentions(response)
    by_super = find_super_class(resources)

    cls = filter_classes(characteristic_word, entity_word, by_super, graph)
    
    for char_uri in cls[Characteristic]:
        for entity_uri in cls[Entity]:
            c = check_entity_char_pair(entity_uri, char_uri)
            if c is not None:
                candidates.add(c)
    for measurement_uri in cls[measurement]:
        candidates.add(measurement_uri)
    return candidates

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

def arrange_synonyms(synset, original):
    if original in synset:
        synset.remove(original)
    synset.insert(0, original)
    return

def get_mts():
    query_str = '''SELECT ?mt ?c ?e WHERE {
          ?r1 owl:onProperty oboe:measuresEntity ; owl:someValuesFrom ?e.
          ?r2 owl:onProperty oboe:measuresCharacteristic ;  owl:someValuesFrom ?c.
          ?mt rdfs:subClassOf ?r1, ?r2.
        }'''
    result = collections.defaultdict(dict)
    for mt, c, e in graph.query(query_str, initNs=dict(oboe=URIRef("http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#"),
                                                       owl=OWL,rdfs=RDFS)):
        result[e][c] = mt
    return result

#measurement_types = get_mts()

@lru
def extract_classes(text, context):
    return extract_mentions(get_query_response(text, context))

@lru
def extract_classes_parse(text, context, c, e):
    dep_parser = StanfordDependencyParser(encoding='utf8')
    response = get_query_response(text, context)
    classes = check_related_class(c, e, response, graph, dep_parser)
    return classes

def match_class(name, description):
    classes = set()

    dep_parser = StanfordDependencyParser(encoding='utf8')
    #jason
    #name = 'WS'
    #description = 'Wind speed'
    characteristic_word = None
    entity_word = None
    pair = get_entity_characteristic(description, dep_parser)
    for c,e in pair.items():
        characteristic_word = c #characteristic
        entity_word = e         #entity
    char_list = []
    ent_list = []
    if characteristic_word is not None:
        lemma = wn.synsets(characteristic_word)
        if len(lemma) > 0:
            char_list = lemma[0].lemma_names()
            arrange_synonyms(char_list, characteristic_word.lower())
        else:
            char_list.append(characteristic_word)
    if entity_word is not None:
        lemma = wn.synsets(entity_word)
        if len(lemma) > 0:
            ent_list = lemma[0].lemma_names()
            arrange_synonyms(ent_list, entity_word.lower())
        else:
            ent_list.append(entity_word)
        
    pre_char = characteristic_word
    pre_ent = entity_word

    # for e in ent_list:
    #    description = description.replace(pre_ent, e)
    #    pre_ent = e
    #    for c in char_list:
    #        description = description.replace(pre_char, c)
    #        #print 'Trying: ' + description
    #        pre_char = c
    #        text = name + ' ' + description
    #        classes = extract_classes_parse(text, text, c, e)
    #        if len(classes) > 0:
    #            return classes


    char_classes = set()
    for c in char_list:
       char_classes.update(extract_classes(c, description))
    char_classes = [x for x in char_classes
                   if len(list(graph.query('''SELECT ?r WHERE { ?r owl:onProperty oboe:measuresCharacteristic ; owl:someValuesFrom <%s>.} limit 1'''%x,
                                           initNs=dict(oboe=oboe,owl=OWL)))) > 0]

    ent_classes = set()
    for e in ent_list:
       if e is None:
           continue
       ent_classes.update(extract_classes(e, description))
    ent_classes = [x for x in ent_classes
                   if len(list(graph.query('''SELECT ?r WHERE { ?r owl:onProperty oboe:measuresEntity ; owl:someValuesFrom <%s>.} limit 1'''%x,
                                           initNs=dict(oboe=oboe,owl=OWL)))) > 0]

    for e in ent_classes:
       for c in char_classes:
           mt = check_entity_char_pair(e, c)
           if mt is not None:
               print "found", e, c, mt
               return [mt]
       
    if len(classes) == 0:
        response = get_query_response(name+' '+description, name+' '+description)
        resources = extract_mentions(response)
        by_super = find_super_class(resources)

        if measurement in by_super:
            return [by_super[measurement][0]]

           
    return classes

def match_abstract_class(text):
    candidates = set()
    response = get_query_response(text, text)
    resources = extract_abstract_mentions(response)
    by_super = find_super_class(resources)
    
    if len(by_super[Characteristic]) <= 10 and len(by_super[Entity]) <= 10:
        for char_uri in by_super[Characteristic]:
            for entity_uri in by_super[Entity]:
                c = check_entity_char_pair(entity_uri, char_uri)
                if c is not None:
                    candidates.add(c)
    return candidates

def find_mapped_description(description, matched_classes):
    for k in matched_classes.keys():
        if description.replace('_', '').lower() == k.replace('_', '').lower():
            return matched_classes[description]
    else:
        return None
    
def get_ir_tuples(annotations):
    result = set()
    for dataset, classes in annotations.items():
        for c in classes:
            result.add((dataset, c))
    return result

def create_class(text, g, prefix=_prefix):
    try:
        resources = list(extract_mentions(text))
    except:
        print 'Error processing "', text, '".'
        return
    print 'Input Text: "' + text + '"'
    by_super = by_super_class(resources)
    result = graph.resource(measurement)
    used = set()
        
    if measurement in by_super:
        #result = graph.resource(by_super[measurement][0])
        print "measurement:" #jason
        print by_super[measurement] #jason
        result = graph.resource(choose_class(by_super[measurement], graph)) #jason
        used.add(result.identifier)
        print result.label() #jason
    elif quality in by_super:
        subgraph = Graph()
        #characteristic = by_super[quality][0]
        print "quality:" #jason
        print by_super[quality] #jason
        characteristic = choose_class(by_super[quality], graph)
        used.add(characteristic)
        super_classes = [Class(result.identifier, graph=subgraph)]
        c = Class(BNode(), graph=subgraph)
        c.subClassOf = super_classes
        equiv = Class(result.identifier, graph=subgraph) & (Property(oboe.ofCharacteristic, graph=subgraph) | only | 
                                          Class(characteristic, graph=subgraph))
        label = graph.label(characteristic)
        print label #jason
        subgraph.add((c.identifier, RDFS.label, Literal(label)))
        c.equivalentClass = [equiv]
        skolemized = canonicalize(subgraph.resource(c.identifier))
        g += subgraph
        result = g.resource(skolemized.identifier)        

    if unit in by_super:
        subgraph = Graph()
        print "unit: " #jason
        print by_super[unit] #jason
        #uom = by_super[unit][0]
        uom = choose_class(by_super[unit], graph)
        used.add(uom)
        super_classes = [Class(result.identifier, graph=subgraph)]
        c = Class(BNode(), graph=subgraph)
        c.subClassOf = super_classes
        equiv = Class(result.identifier, graph=subgraph) & (Property(oboe.hasUnit, graph=subgraph) | only | 
                                          Class(uom, graph=subgraph))
        label = '%s in %s' % (result.label(), graph.label(uom))
        print label #jason
        subgraph.add((c.identifier, RDFS.label, Literal(label)))
        c.equivalentClass = [equiv]
        skolemized = canonicalize(subgraph.resource(c.identifier))
        g += subgraph
        result = g.resource(skolemized.identifier)
        
    if Entity in by_super:
        subgraph = Graph()
        print "entity: " #jason
        print by_super[Entity] #jason
        #entity_class = by_super[entity][0]
        entity_class = choose_class(by_super[Entity], graph)
        used.add(entity_class)
        super_classes = [Class(result.identifier, graph=subgraph)]
        c = Class(BNode(), graph=subgraph)
        c.subClassOf = super_classes
        equiv = Class(result.identifier, graph=subgraph) & (Property(oboe.measurementFor, graph=subgraph) | only | 
                                          (Property(oboe.ofEntity, graph=subgraph) | only | 
                                           Class(entity_class, graph=subgraph)))
        label = '%s %s' % (graph.label(entity_class), result.label())
        print label #jason
        subgraph.add((c.identifier, RDFS.label, Literal(label)))
        c.equivalentClass = [equiv]
        skolemized = canonicalize(subgraph.resource(c.identifier))
        g += subgraph
        result = g.resource(skolemized.identifier)

    result.add(skos.example, Literal(text)) #jason
    return g.resource(result.identifier)    

# import ipywidgets 
# from IPython.display import display

def extract(datasets):
    #f = ipywidgets.FloatProgress(min=0, max=len(datasets))
    #ftext = ipywidgets.Text(value='0', description='%')
    #display(f)
    #display(ftext)
    result_graph = Graph()
    result_graph.parse(data='''
    @prefix owl: <http://www.w3.org/2002/07/owl#>.
    <https://purl.org/dataone/ontologies/observation/ecstra.owl> a owl:Ontology;
        owl:imports <https://purl.org/dataone/ontologies/observation/d1-ECSO.owl>.''', format="turtle")
    i = 0
    for dataset in datasets:
        try:
            columns = get_dataset_columns(dataset)
        except:
            print "Problem processing the dataset '", dataset, "'."
            continue
        for column in columns:
            create_class(column, result_graph)
        i += 1
        #ftext.value = str(100 * float(i)/len(datasets))
        #f.value = i
        print (str(100 * float(i)/len(datasets)))
        print i
    return result_graph

# Linkipedia Param Setting
numResult = 20
minScore = 1
topHits = 100
contentWeight = 6
relationWeight = 6
##########################


# 
# # In[4]:
# 
# print len(datasets)
# extracted_graph = extract(datasets)
# # with open("/Users/jimmccusker/Dropbox/Public/ecstra-unstemmed.ttl",'wb') as out:
# with open("/home/jason/Documents/TWC/linkipedia/output_file/ecstra-unstemmed-dataone-0318.ttl",'wb') as out:
#     out.write(extracted_graph.serialize(format='turtle'))

NUMBER_OF_PROCESSES = 6

processed = Event()

def work(id, jobs, result):
    while True:
        try:
            dataset = jobs.get(timeout=10)
        except Empty:
            processed.set()
            break
        try:
            matched_classes = collections.defaultdict(set)
            r = set()
            print dataset
            #abstract = get_dataset_abstract(dataset)
            attributes = get_attributes(dataset)
            for name, description in attributes.items():
                #print description
                class_set = find_mapped_description(description, matched_classes)
                classes = set()
                if class_set is None:
                    classes = match_class(name, description)
                    matched_classes[description] = classes
                else:
                    classes = class_set
                if len(classes) > 0:
                    for c in classes:
                        r.add(c)
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

    for i in xrange(NUMBER_OF_PROCESSES):
        p = Process(target=work, args=(i, jobs, result))
        p.daemon = True
        p.start()

    automated_annotations = {}
    while not processed.is_set():
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
