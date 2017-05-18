import xml.etree.ElementTree as ElemTree
import collections
import requests


class EMLReader:

    def __init__(self):
        pass

    @staticmethod
    def get_eml(address):
        r = requests.get(address)
        xml = r.text.encode('utf-8')
        return ElemTree.fromstring(xml)

    @staticmethod
    def get_keywords(dataset):
        try:
            keywords = ', '.join([x for x in dataset.findall('dataset/keywordSet/keyword') if x is not None])
        except TypeError:
            return ''
        return keywords

    @staticmethod
    def get_abstract(dataset):
        elems = dataset.findall('dataset/abstract')
        if elems is None:
            return ''
        para = elems[0].find('para')
        if para is not None:
            abstract = para.text
        else:
            abstract = elems[0].text
        return abstract

    @staticmethod
    def dict_from_tags(e, tags):
        result = collections.defaultdict(str)
        for tag in tags:
            label = e.find(tag)
            if label is not None:
                result[tag] = label.text
        return result

    @staticmethod
    def get_child_tag_text(e, tag):
        for elem in e.iter(tag):
            return elem.text
        return ''

    @staticmethod
    def get_eml_info(address, ent_no, attr_no):
        eml = EMLReader.get_eml(address)
        try:
            abstract_text = EMLReader.get_abstract(eml)
            datatable = eml.findall('dataset/dataTable')[int(ent_no) - 1]
            ent = EMLReader.dict_from_tags(datatable, ['entityName', 'entityDescription'])
            attribute = datatable.findall('attributeList/attribute')[int(attr_no) - 1]
            attr = EMLReader.dict_from_tags(attribute, ['attributeName', 'attributeDefinition'])
            unit = EMLReader.get_child_tag_text(attribute, 'standardUnit')
            if unit is '':
                unit = EMLReader.get_child_tag_text(attribute, 'customUnit')
            result = {'abstract': abstract_text,
                      'entityName': ent['entityName'],
                      'entityDescription': ent['entityDescription'],
                      'attributeName': attr['attributeName'],
                      'attributeDefinition': attr['attributeDefinition'],
                      'unit': unit}
            return result
        except IndexError:
            return None


