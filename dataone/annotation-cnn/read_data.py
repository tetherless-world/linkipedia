
import csv
from io import StringIO
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

from collections import defaultdict
from eml import EMLReader
def refill_description(src_list, dest_list):
    eml = Namespace('https://cn.dataone.org/cn/v2/object/')
    reader = EMLReader()
    result = []
    pkgs = defaultdict(list)
    for dm in dest_list:
        for sm in src_list:
            if dm['pkg'] == sm['pkg'] and dm['ent_no'] == sm['ent_no'] and dm['attr_no'] == sm['attr_no']:
                pkgs[dm['pkg']].append(dm)
                break
    for key in pkgs.keys():
        print key
        result += reader.get_eml_info(key, eml[key], pkgs[key])
    return result

import json
def save_data(data, file_name):
    with open(file_name, 'w') as outfile:
        json.dump(data, outfile)

raw_anno = get_manual_annotations_from_file('annotations_manual_all, corpus F - matched_template_priority_all.tsv')
complete_anno = get_manual_annotations()
manu_annotation = refill_description(raw_anno, complete_anno)
save_data(manu_annotation, 'data.json')

