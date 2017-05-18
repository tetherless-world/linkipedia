import xml.etree.ElementTree as ET
import re
import collections
import requests

class EMLReader():
    def get_eml(self, address):
        r = requests.get(address)
        xml = r.text.encode('utf-8')
        return ET.fromstring(xml)
    
    def get_keywords(self, dataset):
        try:
            keywords = re.split('[,;]\s*', ', '.join([x.text for x in dataset.findall('dataset/keywordSet/keyword') if type(x) != type(None)]))
        except TypeError:
            keywords = ''
        return keywords
    
    def get_abstract(self, dataset):
        try:
            abstract = '\n'.join([x.text for x in dataset.findall('dataset/abstract/*') if type(x) != type(None)])
        except TypeError:
            abstract = ''
        return abstract
    
    def dict_from_tags(self, e, tags):
        result = collections.defaultdict(str)
        for tag in tags:
            label = e.find(tag)
            if label is not None:
                result[tag] = label.text
        return result
    
    def get_eml_info(self, pkg_id, address, mtypes):
        result = []
        eml = self.get_eml(address)
        keywords = self.get_keywords(eml)
        keywords = [t for t in keywords if t is not None]
        keywords = ' '.join(keywords)
        abstract = self.get_abstract(eml)
        abstract = [t for t in abstract if t is not None]
        abstract = ' '.join(abstract)
        for mt in mtypes:
            datatable = eml.findall('dataset/dataTable')[int(mt['ent_no']) - 1]
            ent = self.dict_from_tags(datatable, ['entityName', 'entityDescription'])
            attribute = datatable.findall('attributeList/attribute')[int(mt['attr_no']) - 1]
            attr = self.dict_from_tags(attribute, ['attributeName', 'attributeDefinition'])
            text = {'entityName' : ent['entityName'],
                    'entityDescription' : ent['entityDescription'],
                    'attributeName' : attr['attributeName'],
                    'attributeDefinition' : attr['attributeDefinition']
                    }
            unit = attribute.find('.//standardUnit | .//customUnit')
            if unit is not None:
                text['unit'] = unit
            mt.update({'text' : text})
            result.append(mt)
        return result

        
