
import csv
from rdflib import *
from io import StringIO
manual_annotation_file = 'annotations_manual_all, corpus F - matched_template_priority_all.tsv'
def get_manual_annotations_from_file(file_name, nSize=-1):
    with open(file_name,'r') as tsv: 
        annotations = [x for x in csv.DictReader(tsv, delimiter="\t")]
        result = []
        for annotation in annotations:
            if len(annotation['ent_no'].strip()) == 0:
                continue
            pkg_id = annotation['pkg_id'].strip()
            ent_no = annotation['ent_no'].strip()
            attr_no = annotation['attr_no'].strip()
            description = annotation['attribute'].strip()
            result.append({'pkg':pkg_id, 'ent_no': ent_no, 
                           'attr_no':attr_no, 'text':description})
            if nSize > 0 and len(result) >= nSize:
                print 'Size of the manual annotations is %d' % nSize 
                break
        return result

import requests
#manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_F_dev/manual_annotations.tsv.txt'
manual_annotation_url = 'https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/manual_annotation/joined_manual_annotations.csv'
def get_manual_annotations(nSize=-1):
    resp = requests.get(manual_annotation_url)
    annotations = [x for x in csv.DictReader(StringIO(resp.text,newline=None), delimiter=',')]
    resp.close()
    result = []
    for annotation in annotations:
        if len(annotation['class_id_int'].strip()) == 0:
            continue
        pkg_id = annotation['pkg_id'].strip()
        ent_no = annotation['ent_no'].strip()
        attr_no = annotation['attr_no'].strip()
        mtype_uri = 'http://purl.dataone.org/odo/ECSO_%08d' % int(annotation['class_id_int'].strip())
        result.append({'pkg':pkg_id, 'ent_no':ent_no, 'attr_no':attr_no, 'mt':mtype_uri})
        if nSize > 0 and len(result) >= nSize:
            print 'Size of the manual annotations is %d' % nSize 
            break
    return result
raw_anno = get_manual_annotations_from_file(manual_annotation_file)
complete_anno = get_manual_annotations()

import xml.etree.ElementTree as ET
import re
import collections
eml=Namespace('https://cn.dataone.org/cn/v2/object/')
def get_eml(identifier):
    r = requests.get(eml[identifier])
    xml = r.text.encode('utf-8')
    return ET.fromstring(xml)

def get_keywords(dataset):
    try:
        keywords = re.split('[,;]\s*', ', '.join([x.text for x in dataset.findall('dataset/keywordSet/keyword') if type(x) != type(None)]))
    except TypeError:
        keywords = ''
    return keywords

def get_abstract(dataset):
    try:
        abstract = '\n'.join([x.text for x in dataset.findall('dataset/abstract/*') if type(x) != type(None)])
    except TypeError:
        abstract = ''
    return abstract

def dict_from_tags(e, tags):
    result = collections.defaultdict(str)
    for tag in tags:
        label = e.find(tag)
        if label is not None:
            result[tag] = label.text
    return result

def get_eml_info(pkg_id, mtypes):
    result = []
    eml = get_eml(pkg_id)
    keywords = get_keywords(eml)
    keywords = [t for t in keywords if t is not None]
    keywords = ' '.join(keywords)
    abstract = get_abstract(eml)
    abstract = [t for t in abstract if t is not None]
    abstract = ' '.join(abstract)
    for mt in mtypes:
        datatable = eml.findall('dataset/dataTable')[int(mt['ent_no']) - 1]
        ent = dict_from_tags(datatable, ['entityName', 'entityDescription'])
        attribute = datatable.findall('attributeList/attribute')[int(mt['attr_no']) - 1]
        attr = dict_from_tags(attribute, ['attributeName', 'attributeDefinition'])
        text = [ent['entityName'],
                ent['entityDescription'],
                attr['attributeName'],
                attr['attributeDefinition']]
        unit = attribute.find('.//standardUnit | .//customUnit')
        if unit is not None:
            text.append(unit)
        text = [t for t in text if t is not None]
        text = ' '.join(text)
        description = ' '.join([keywords, abstract, text])
        mt.update({'text' : description})
        result.append(mt)
    return result

from collections import defaultdict
def refill_description(src_list, dest_list):
    result = []
    pkgs = defaultdict(list)
    for dm in dest_list:
        for sm in src_list:
            if dm['pkg'] == sm['pkg'] and dm['ent_no'] == sm['ent_no'] and dm['attr_no'] == sm['attr_no']:
                pkgs[dm['pkg']].append(dm)
                break
    for key in pkgs:
        print key
        result += get_eml_info(key, pkgs[key])
    return result

manu_annotation = refill_description(raw_anno, complete_anno)
print 'Size of manual annotation: ', len(manu_annotation)
del complete_anno
del raw_anno
