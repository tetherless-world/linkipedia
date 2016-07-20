#!/usr/bin/env python

import xml.etree.ElementTree as ET
import re, sys
import io
from rdflib import *
from rdflib.extras import infixowl

import requests

d1=Namespace('https://cn.dataone.org/cn/v2/object/')
oboe = Namespace('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#')


def create_ontology(identifier):
    ontology_uri = d1[identifier]
    o_ns = Namespace(ontology_uri+"#")
    r = requests.get(ontology_uri)
    xml = r.text.encode('utf-8')
    eml = ET.fromstring(xml)

    g = Graph()
    g.bind("rdfs", RDFS)
    g.bind("owl", OWL)

    ontology = infixowl.Ontology(ontology_uri, graph=g)
    datasetTitle = eml.find('dataset/title')
    if datasetTitle is not None:
        ontology.label = Literal(datasetTitle.text.strip())
    
    ontology.comment = Literal('\n'.join([x.text for x in eml.findall('dataset/abstract/para')]).strip())

    # Data Table:
    i = 0
    for datatable in eml.findall("dataset/dataTable"):
        i += 1
        entity_ident = str(i)
        entity = infixowl.Class(o_ns['_'.join(['d',entity_ident])],graph=g)
        entity.subClassOf = [oboe.Entity]
        entityLabel = []
        for e in ['entityName','entityDescription']:
            if datatable.find(e) is not None and datatable.find(e).text is not None:
                entityLabel.append(datatable.find(e).text.strip())
        entity.label = Literal(' '.join(entityLabel))
        j = 0
        for attribute in datatable.findall('attributeList/attribute'):
            j +=1
            attribute_ident = str(j)
            characteristic = infixowl.Class(o_ns['_'.join(['d',entity_ident,'c',attribute_ident])],graph=g)
            characteristic.subClassOf = [oboe.Characteristic]
            label = []
            for e in ['.//attributeName','.//attributeLabel','.//attributeDefinition']:
                if attribute.find(e) is not None and attribute.find(e).text is not None:
                    label.append(attribute.find(e).text.strip())
            characteristic.label = Literal(' '.join(label))

            measurement_type = infixowl.Class(o_ns['_'.join(['d',entity_ident,'a',attribute_ident])],graph=g)
            measurement_type.label = Literal(g.label(entity.identifier) + " " + g.label(characteristic.identifier))
            measurement_type.subClassOf = [
                oboe.MeasurementType,
                infixowl.Restriction(oboe.measuresEntity, graph=g, someValuesFrom=entity),
                infixowl.Restriction(oboe.measuresCharacteristic, graph=g, someValuesFrom=characteristic)
            ]
    return g

def main(dataset, output_file):
    g = create_ontology(dataset)
    g.serialize(destination=output_file, format='pretty-xml')		

if __name__ == '__main__':
    main(*sys.argv[1:])
