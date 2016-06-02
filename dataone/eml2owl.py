#!/usr/bin/env python

import xml.etree.ElementTree as ET
import re, sys
import io
from rdflib import *
from rdflib.extras import infixowl

import requests

d1=Namespace('https://cn.dataone.org/cn/v2/object/')
oboe = Namespace('http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl#')


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
    ontology.label = Literal(eml.find('dataset/title').text.strip())
    ontology.comment = Literal('\n'.join([x.text for x in eml.find('dataset/abstract/para')]).strip())

    # Data Table:
    for datatable in eml.findall("dataset/dataTable"):
        entity_ident = datatable.attrib['id']
        entity = infixowl.Class(o_ns[entity_ident],graph=g)
        entity.subClassOf = [oboe.Entity]
        entity.label = Literal(datatable.find('entityName').text.strip())
        entity.comment = Literal(datatable.find('entityDescription').text.strip())
        for attribute in datatable.findall('attributeList/attribute'):
            attribute_ident = attribute.attrib['id']
            characteristic = infixowl.Class(o_ns[attribute_ident],graph=g)
            characteristic.subClassOf = [oboe.Characteristic]
            characteristic.label = Literal(datatable.find('.//attributeLabel').text.strip())
            characteristic.comment = Literal(datatable.find('.//attributeDefinition').text.strip())

            measurement_type = infixowl.Class(o_ns[entity_ident +"_"+attribute_ident],graph=g)
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
