# -*- coding:utf-8 -*-


import os
import sys, collections
from flask import Flask, render_template, g, session, redirect, url_for, request, flash, abort

import sadi.mimeparse
import rdflib
import json

from datetime import datetime

from eml2owl import create_ontology as get_eml

from matching import *

from rdflib.util import guess_format

# apps is a special folder where you can place your blueprintsem
PROJECT_PATH = os.path.abspath(os.path.dirname(__file__))
sys.path.insert(0, os.path.join(PROJECT_PATH, "apps"))

basestring = getattr(__builtins__, 'basestring', str)

class NamespaceContainer:
    @property
    def prefixes(self):
        result = {}
        for key, value in self.__dict__.items():
            if isinstance(value, Namespace):
                result[key] = value

from empty import Empty

class App(Empty): 

    def configure_database(self):
        """
        Database configuration should be set here
        """
        self.NS = NamespaceContainer()
        self.NS.RDFS = rdflib.RDFS
        self.NS.RDF = rdflib.RDF
        self.NS.OWL = rdflib.OWL
        self.NS.xsd   = rdflib.Namespace("http://www.w3.org/2001/XMLSchema#")
        self.NS.dcterms    = rdflib.Namespace("http://purl.org/dc/terms/")
        self.NS.prov  = rdflib.Namespace("http://www.w3.org/ns/prov#")
        self.NS.skos = rdflib.Namespace("http://www.w3.org/2004/02/skos/core#")
        self.NS.dcat = rdflib.Namespace("http://www.w3.org/ns/dcat#")
        self.NS.oa = rdflib.Namespace("http://www.w3.org/ns/oa#")
        self.NS.dataset = rdflib.Namespace("https://cn.dataone.org/cn/v2/object/")
        self.NS.local = rdflib.Namespace(self.config['lod_prefix']+'/')
        self.NS.oboe = Namespace('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#')
        self.NS.csvw = Namespace('http://www.w3.org/ns/csvw#')
        self.urn = rdflib.Namespace("urn:")

        self.nt_file = self.config['target_ontology']
        self.target_graph = ConjunctiveGraph()
        self.target_graph.load(self.nt_file, format=guess_format(self.nt_file))

        target_classes, idf = vectorize_ontology(self.target_graph)
        self.target_classes = target_classes
        self.idf = idf
        self.target_subtree = set(self.target_graph.transitive_subjects(self.NS.RDFS.subClassOf, self.NS.oboe.MeasurementType))
        self.target_class_subtree = [x for x in self.target_classes
                                     if x.identifier in self.target_subtree and x.identifier != self.NS.oboe.MeasurementType]
        self.targets = dict([(x.identifier, x) for x in self.target_class_subtree])

        
    def configure_views(self):

        def sort_by(resources, property):
            return sorted(resources, key=lambda x: x.value(property))

        extensions = {
            "rdf": "application/rdf+xml",
            "json": "application/ld+json",
            "ttl": "text/turtle",
            "trig": "application/trig",
            "turtle": "text/turtle",
            "owl": "application/rdf+xml",
            "nq": "application/n-quads",
            "nt": "application/n-triples",
            "html": "text/html"
        }
            
        @self.route('/<path:name>')
        def view(name=None):
            print name
            
            content_type = request.headers['Content-Type'] if 'Content-Type' in request.headers else '*/*'

            source_graph = get_eml(name)
            classes, local_idf = vectorize_ontology(source_graph, self.idf)
            source_graph.classes = classes
            source_classes = source_graph.classes
            source_subtree = set(source_graph.transitive_subjects(self.NS.RDFS.subClassOf, self.NS.oboe.MeasurementType))
            source_class_subtree = [x for x in source_classes
                                    if x.identifier in source_subtree and x.identifier != self.NS.oboe.MeasurementType]
            sources = dict([(x.identifier, x) for x in source_class_subtree])

            distances = pairwise_sparsedist(source_class_subtree, self.target_class_subtree)

            result = rdflib.Graph()
            dataset = result.resource(self.NS.dataset[name])
            dataset.add(self.NS.RDF.type, self.NS.dcat.Dataset)
            for c, dist in distances.items():
                source_class = source_graph.resource(c)
                attr_id = source_class.value(self.urn.attributeId)
                entity_id = source_class.value(self.urn.entityId)
                selector = "xpointer(/eml/dataset/dataTable[%s]/attributeList/attribute[%s])" % (entity_id, attr_id)
                attribute = result.resource(dataset.identifier+'#'+selector)
                attribute.add(self.NS.RDF.type, self.NS.csvw.Column)
                sel = result.resource(rdflib.BNode())
                attribute.add(self.NS.oa.hasSelector, sel)
                sel.add(self.NS.RDF.type, self.NS.oa.FragmentSelector)
                sel.add(self.NS.dcterms.conformsTo, rdflib.URIRef("http://tools.ietf.org/rfc/rfc3023"))
                sel.add(self.NS.RDF.value, Literal(selector))
                attribute.add(self.NS.RDFS.label, source_class.label())
                
                for target, score in sorted(dist.items(), key=lambda x: x[1])[:self.config['top_hits']]:
                    if score < self.config['max_distance']:
                        attribute.add(self.NS.dcterms.subject, target)
                        result.add((target, self.NS.RDFS.label, self.target_graph.label(target)))
            
            return sadi.serialize(result, accept=content_type)

def config_str_to_obj(cfg):
    if isinstance(cfg, basestring):
        module = __import__('config', fromlist=[cfg])
        return getattr(module, cfg)
    return cfg


def app_factory(config, app_name, blueprints=None):
    # you can use Empty directly if you wish
    app = App(app_name)
    config = config_str_to_obj(config)
    app.configure(config)
    if blueprints:
        app.add_blueprint_list(blueprints)
    app.setup()

    return app


def heroku():
    from config import Config, project_name
    # setup app through APP_CONFIG envvar
    return app_factory(Config, project_name)
