
import csv
from io import StringIO
from eml import EMLReader
import requests
import json
from rdflib import *
import random
import numpy as np
from sklearn.preprocessing import OneHotEncoder


MeasurementType = URIRef('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#MeasurementType')
Entity = URIRef('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Entity')
Characteristic = URIRef('http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Characteristic')
SKOS = Namespace('http://www.w3.org/2004/02/skos/core#')


class DataHelper:
    def __init__(self):
        pass

    @staticmethod
    def get_manual_annotations_from_web(manual_annotation_url, size=-1):
        resp = requests.get(manual_annotation_url)
        annotations = [x for x in csv.DictReader(StringIO(resp.text, newline=None), delimiter=",")]
        resp.close()

        result = []
        for row in annotations:
            if len(row['class_uri'].strip()) == 0:
                continue
            result.append({'pkg_id': row['pkg_id'].strip(),
                           'ent_no': row['ent_no'].strip(),
                           'attr_no': row['attr_no'].strip(),
                           'entity': row['entity'].strip(),
                           'characteristic': row['characteristic'].strip(),
                           'class_uri': row['class_uri'].strip()})
            if len(result) >= size > 0:
                break
        print 'Size of the manual annotations is %d' % len(result)
        return result

    @staticmethod
    def get_one_hot_labels(target_classes):
        return OneHotEncoder().fit_transform(np.array(target_classes).reshape(-1, 1)).toarray()

    @staticmethod
    def split_dataset(dataset, train_percentage):
        indices = range(len(dataset))
        random.shuffle(indices)
        train = [dataset[indices[i]] for i in range(len(indices))
                 if dataset[indices[i]]['eml'] is not None and i <= len(indices) * train_percentage]
        test = [dataset[indices[i]] for i in range(len(indices))
                if dataset[indices[i]]['eml'] is not None and i > len(indices) * train_percentage]
        return train, test

    @staticmethod
    def get_all_measurement_types(ontology_file):
        graph = ConjunctiveGraph()
        graph.load(ontology_file, format="n3")
        query_str = '''SELECT DISTINCT ?mt ?label ?comment ?defn ?ent ?char 
                WHERE {
                  ?mt rdfs:label ?label .
                  ?mt rdfs:subClassOf <%s> .
                  ?mt rdfs:subClassOf ?r1 .
                  ?r1 owl:onProperty oboe:measuresEntity ; owl:someValuesFrom ?ent .
                  ?mt rdfs:subClassOf ?r2 .
                  ?r2 owl:onProperty oboe:measuresCharacteristic ; owl:someValuesFrom ?char .
                  OPTIONAL { ?mt rdfs:comment ?comment }
                  OPTIONAL { ?char skos:definition ?defn }
                }''' % MeasurementType
        qres = list(
            graph.query(query_str,
                        initNs=dict(oboe=URIRef("http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#"),
                                    owl=OWL, rdfs=RDFS, skos=SKOS)))
        if len(qres) > 0:
            qres.sort(key=lambda x: x[0], reverse=True)
            result = dict()
            i = 0
            for row in qres:
                result[i] = {'uri': '' if row[0] is None else row[0],
                             'label': 'null' if row[1] is None else row[1].n3(),
                             'comment': 'null' if row[2] is None else row[2].n3(),
                             'defn': 'null' if row[3] is None else row[3].n3(),
                             'ent': '' if row[4] is None else row[4],
                             'char': '' if row[5] is None else row[5]}
                i = i + 1
            print "Sparql query finished!"
            print result
            return result
        return None

    @staticmethod
    def has_diff_ent(anno, onto_class, ontology):
        entity, _ = DataHelper.get_ent_char_from_mt(anno['anno']['class_uri'], ontology)
        if entity is None:
            return True
        else:
            if entity != onto_class['ent']:
                return True
            else:
                return False

    @staticmethod
    def has_diff_char(anno, onto_class, ontology):
        _, char = DataHelper.get_ent_char_from_mt(anno['anno']['class_uri'], ontology)
        if char is None:
            return True
        else:
            if char != onto_class['char']:
                return True
            else:
                return False

    @staticmethod
    def get_pairs(ontology, manual_annotations, negative_label_size=0):
        samples = []
        for anno in manual_annotations:
            correct_idx = 0
            for idx in ontology:
                if ontology[idx]['uri'] == URIRef(anno['anno']['class_uri']):
                    correct_idx = idx
                    sim_mt = [1.0, 0.0]
                    sim_ent = [1.0, 0.0]
                    sim_char = [1.0, 0.0]
                    samples.append({'ontoLabel': ontology[idx]['label'].lower().replace('_', ' ').replace('\"', ''),
                                    'datasetLabel': DataHelper.get_label(anno['eml']).lower().replace('_', ' '),
                                    'ontoDefn': ontology[idx]['defn'].lower().replace('_', ' ').replace('\"', ''),
                                    'datasetDefn': DataHelper.get_defn(anno['eml']).lower().replace('_', ' '),
                                    'ontoUnit': ontology[idx]['defn'].lower().replace('_', ' ').replace('\"', ''),
                                    'datasetUnit': DataHelper.get_unit(anno['eml']).lower().replace('_', ' '),
                                    'datasetAbstract': DataHelper.get_abstract(anno['eml']).lower().replace('_', ' ').replace('\n', ' ').replace('\t', ' '),
                                    'mtUri': ontology[idx]['uri'],
                                    'similarity_mt': sim_mt,
                                    'similarity_ent': sim_ent,
                                    'similarity_char': sim_char})
                    break
            if negative_label_size > 0:
                shuffled_idx = range(len(ontology.keys()))
                random.shuffle(shuffled_idx)
                num_neg_label = 0
                for idx in shuffled_idx:
                    if not idx == correct_idx:
                        sim_mt = [0.0, 1.0]
                        sim_ent = [0.0, 1.0]
                        sim_char = [0.0, 1.0]
                        if not DataHelper.has_diff_ent(anno, ontology[idx], ontology):
                            print 'same entity'
                            sim_ent = [1.0, 0.0]
                        if not DataHelper.has_diff_char(anno, ontology[idx], ontology):
                            print 'same char'
                            sim_char = [1.0, 0.0]
                        samples.append({'ontoLabel': ontology[idx]['label'].lower().replace('_', ' ').replace('\"', ''),
                                        'datasetLabel': DataHelper.get_label(anno['eml']).lower().replace('_', ' '),
                                        'ontoDefn': ontology[idx]['defn'].lower().replace('_', ' ').replace('\"', ''),
                                        'datasetDefn': DataHelper.get_defn(anno['eml']).lower().replace('_', ' '),
                                        'ontoUnit': ontology[idx]['defn'].lower().replace('_', ' ').replace('\"', ''),
                                        'datasetUnit': DataHelper.get_unit(anno['eml']).lower().replace('_', ' '),
                                        'datasetAbstract': DataHelper.get_abstract(anno['eml']).lower().replace('_', ' ').replace('\n', ' ').replace('\t', ' '),
                                        'mtUri': ontology[idx]['uri'],
                                        'similarity_mt': sim_mt,
                                        'similarity_ent': sim_ent,
                                        'similarity_char': sim_char})
                        num_neg_label += 1
                        if num_neg_label == negative_label_size:
                            break
        return samples

    @staticmethod
    def get_test_pairs(anno, ontology):
        samples = []
        for i in range(len(ontology)):
            new_anno = anno.copy()
            new_anno['ontoLabel'] = ontology[i]['label'].lower().replace('_', ' ').replace('\"', '')
            new_anno['ontoDefn'] = ontology[i]['comment'].lower().replace('_', ' ').replace('\"', '')
            new_anno['ontoUnit'] = ontology[i]['defn'].lower().replace('_', ' ').replace('\"', '')
            new_anno['mtUri'] = ontology[i]['uri']
            samples.append(new_anno)
        return samples

    @staticmethod
    def get_label(eml):
        if eml is None:
            return 'null'
        try:
            result = eml['attributeName']
            if result is None:
                return 'null'
            return result
        except KeyError:
            return 'null'

    @staticmethod
    def get_abstract(eml):
        if eml is None:
            return 'null'
        try:
            result = eml['abstract']
            if result is None:
                return 'null'
            return result
        except KeyError:
            return 'null'

    @staticmethod
    def get_defn(eml):
        if eml is None:
            return 'null'
        try:
            result = eml['attributeDefinition']
            if result is None:
                return 'null'
            return result
        except KeyError:
            return 'null'

    @staticmethod
    def get_context(eml):
        if eml is None:
            return 'null'
        try:
            result = eml['entityDescription']
            if result is None:
                return 'null'
            return result
        except KeyError:
            return 'null'

    @staticmethod
    def get_unit(eml):
        if eml is None:
            return 'null'
        try:
            result = eml['unit']
            if result is None:
                return 'null'
            return result
        except KeyError:
            return 'null'

    @staticmethod
    def load_eml_data(manu_anno):
        eml = Namespace('https://cn.dataone.org/cn/v2/object/')
        reader = EMLReader()
        result = []
        for m in manu_anno:
            print m['pkg_id'], m['ent_no'], m['attr_no'], m['entity'], m['characteristic']
            result.append({'anno': m,
                           'eml': reader.get_eml_info(eml[m['pkg_id']], m['ent_no'], m['attr_no'])})
            print result[-1]['eml']
        return result

    @staticmethod
    def save_data(data, file_path):
        with open(file_path, 'w') as outfile:
            json.dump(data, outfile)
        print 'Successfully saved data set!'

    @staticmethod
    def get_data_from_file(file_path):
        with open(file_path, 'r') as data:
            return json.load(data)

    @staticmethod
    def read_and_save_data(file_path, size=-1):
        manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/' \
                                'manual_annotation/joined_annotations.csv'
        manu_anno = DataHelper.get_manual_annotations_from_web(manual_annotation_url, size)
        raw_data = DataHelper.load_eml_data(manu_anno)
        DataHelper.save_data(raw_data, file_path)
        return raw_data

    @staticmethod
    def query_measurement_types(uri, ontology_file):
        graph = ConjunctiveGraph()
        graph.load(ontology_file, format="n3")
        query_str = '''SELECT ?label
            WHERE {
              <%s> rdfs:label ?label .
            }''' % uri
        qres = list(
            graph.query(query_str,
                        initNs=dict(oboe=URIRef("http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#"),
                                    owl=OWL, rdfs=RDFS)))
        if len(qres) > 0:
            return qres[0][0]
        return None

    @staticmethod
    def get_ent_char_from_mt(uri, ontology):
        for i in ontology:
            if ontology[i]['uri'] == URIRef(uri):
                return ontology[i]['ent'], ontology[i]['char']
        return None, None
