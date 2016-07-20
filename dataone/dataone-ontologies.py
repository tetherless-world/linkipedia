#!/usr/bin/env python

from rdflib import *
import requests
import os

ontologies = '''https://raw.githubusercontent.com/DataONEorg/sem-prov-ontologies/master/observation/d1-ECSO.owl
https://raw.githubusercontent.com/NCEAS/oboe/master/oboe.owl
https://raw.githubusercontent.com/NCEAS/oboe/master/oboe-core.owl
https://raw.githubusercontent.com/NCEAS/oboe/master/oboe-characteristics.owl
https://raw.githubusercontent.com/NCEAS/oboe/master/oboe-standards.owl
http://purl.dataone.org/odo/d1-generated-ECSO.owl
http://purl.dataone.org/obo/ENVO_import.owl
http://purl.dataone.org/obo/PATO_import.owl
http://purl.dataone.org/obo/UO_import.owl
http://www.w3.org/TR/skos-reference/skos-owl1-dl.rdf'''.split('\n')

g = ConjunctiveGraph()
for o in ontologies:
    ontology = ConjunctiveGraph(identifier=URIRef(o), store=g.store)
    ontology.parse(data=requests.get(o).text)

try:
    os.makedirs('dataone/dataone-index/NTriple')
except OSError:
    pass
g.serialize(open("dataone/dataone-index/NTriple/d1-ESCO-imported.nt",'w'), format="nt")

