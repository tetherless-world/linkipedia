#!/usr/bin/env python
# coding: utf-8

import csv, urllib, json, urllib2
import pandas as pd
from rdflib.extras.infixowl import *
import collections
import json
import base64
import random
import datetime
import requests
from rdflib import *
import editdistance

import sys

service_url = 'http://localhost:8080/annotate/vlinking3/'

oboe = Namespace('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#')
cmo = Namespace('http://purl.org/twc/ontologies/cmo.owl#')
skos = Namespace('http://www.w3.org/2004/02/skos/core#')
dbo = Namespace('http://dbpedia.org/ontology/')

from rdflib.plugins.stores.sparqlstore import SPARQLStore
dbpedia_store = SPARQLStore('http://dbpedia.org/sparql')
dbpedia = ConjunctiveGraph(dbpedia_store)

def extract_mentions(text):
    urls = collections.defaultdict(float)
    params = {
        'numResult':20,
        #'minScore':5,
        'query':text
    }
    response = requests.get(service_url, params=params).json()
    for r in response['results']:
        for annotation in r['annotations']:
            urls[annotation['url']] += float(annotation['score'])
    urls = [(URIRef(url), score) for url, score 
            in sorted(urls.items(), reverse=True, key=lambda x: x[1]) if len(url) > 0]
    return urls

def chunks(l, n):
    """Yield successive n-sized chunks from l."""
    for i in range(0, len(l), n):
        yield l[i:i+n]

def get(xs):
    q = '''construct {
 ?x rdfs:label ?l;
    rdfs:comment ?c;
    skos:altLabel ?a.
} where {
 values ?x { %s }
 ?x rdfs:label ?l;
    rdfs:comment ?c;
    (dbo:wikiPageRedirects*)/(^dbo:wikiPageRedirects*)/rdfs:label ?a.
 FILTER (lang(?l) = "en")
 FILTER (lang(?c) = "en")
}'''
    g = Graph()
    for chunk in chunks(xs, 20):
        query = q % ' '.join([x.n3() for x in chunk])
        try:
            g += dbpedia.query(query, initNs=dict(dbo=dbo))
        except Exception as e:
            print query
            raise e
    return g

def get_english_label(x):
    q = 'select ?l where {<%s> rdfs:label ?l FILTER (lang(?l) = "en")}'
    r = [x for x, in dbpedia.query(q%x)]
    if len(r) > 0:
        return r[0]
    return None

def get_english_definition(x):
    q = 'select ?l where {<%s> rdfs:comment ?l FILTER (lang(?l) = "en")}'
    r = [x for x, in dbpedia.query(q%x)]
    if len(r) > 0:
        return r[0]
    return None

def get_labels(x):
    q = 'select ?l where { <%s> (dbo:wikiPageRedirects*)/(^dbo:wikiPageRedirects*)/rdfs:label ?l.}'
    return [x for x, in dbpedia.query(q%x, initNs=dict(dbo=dbo))]


def getLabels(ontology_file, outputFile):
# In[29]:

#ontology_file = 'https://raw.githubusercontent.com/DataONEorg/sem-prov-ontologies/master/observation/d1-ECSO.owl'

    ontology = ConjunctiveGraph()
    ontology.load(ontology_file,format='xml')

    classes = [ontology.resource(c) for c in ontology[:RDF.type:OWL.Class]]
    classes = [c for c in classes 
               if c.identifier.startswith(oboe) 
               or c.identifier.startswith('http://purl.dataone.org/odo/')]

    output = ConjunctiveGraph()

    summary = pd.DataFrame(columns=[
        'uri','label','resource',
        'dbpl','score','combined',
        'editdist', 'altLabel','definition'])
    i=0
    for c in classes:
        i += 1
        label = c.label()
        mentions = extract_mentions(label)
        if len(mentions) == 0:
            continue
        g = get([x for x, score in mentions])
        for uri, score in mentions:
            dbpl = g.label(uri)
            if dbpl is None:
                continue
            editdist = editdistance.eval(label.value.replace("_", " "), dbpl)
            if editdist < 4:
                labels = list(g.objects(uri, skos.altLabel))
                for l in labels:
                    output.add((uri, skos.altLabel, l))
                
                defn = g.value(uri, RDFS.comment)
                if defn is not None:
                    output.add((uri, skos.definition, defn))
                for altLabel in labels:
                    summary = summary.append(dict(uri=c, label=label, resource=uri,
                                                dbpl=dbpl,
                                                score=score, combined=score/(0.1+editdist),
                                                editdist=editdist, definition=defn, 
                                                altLabel=altLabel),
                                            ignore_index=True)
                break

    f = open(outputFile+".nt", 'w')
    f.write(output.serialize(format="nt"))
    f.close()
    summary.sort('combined',ascending=False).to_csv(outputFile+".csv",encoding='utf-8')


if __name__ == '__main__':
    getLabels(*sys.argv[1:])
