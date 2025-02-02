#!/usr/bin/env python

from xml.parsers import expat
from webmapCore.language.vulnerability import Anomaly


class AnomalyXMLParser(object):

    ANOMALY = "anomaly"
    ANOMALY_NAME = "name"
    ANOMALY_DESCRIPTION = "description"
    ANOMALY_SOLUTION = "solution"
    ANOMALY_REFERENCE = "reference"
    ANOMALY_REFERENCES = "references"
    ANOMALY_REFERENCE_TITLE = "title"
    ANOMALY_REFERENCE_URL = "url"

    def __init__(self):
        self._parser = expat.ParserCreate()
        self._parser.StartElementHandler = self.start_element
        self._parser.EndElementHandler = self.end_element
        self._parser.CharacterDataHandler = self.char_data
        self.anomalies = []
        self.anom = None
        self.references = {}
        self.title = ""
        self.url = ""
        self.tag = ""

    def parse(self, filename):
        f = None
        try:
            f = open(filename)
            content = f.read()
            self.feed(content)
        finally:
            if f is not None:
                f.close()

    def feed(self, data):
        self._parser.Parse(data, 0)

    def close(self):
        self._parser.Parse("", 1)
        del self._parser

    def start_element(self, name, attrs):
        if name == self.ANOMALY:
            self.anom = Anomaly()
            self.anom.setName(attrs[self.ANOMALY_NAME])
        elif name == self.ANOMALY_DESCRIPTION:
            self.tag = self.ANOMALY_DESCRIPTION
        elif name == self.ANOMALY_SOLUTION:
            #self.tag = self.ANOMALY_SOLUTION
            self.anom.setSolution(attrs["text"])
        elif name == self.ANOMALY_REFERENCES:
            self.references = {}
        elif name == self.ANOMALY_REFERENCE:
            self.tag = self.ANOMALY_REFERENCE
        elif name == self.ANOMALY_REFERENCE_TITLE:
            self.tag = self.ANOMALY_REFERENCE_TITLE
        elif name == self.ANOMALY_REFERENCE_URL:
            self.tag = self.ANOMALY_REFERENCE_URL

    def end_element(self, name):
        if name == self.ANOMALY:
            self.anomalies.append(self.anom)
        elif name == self.ANOMALY_REFERENCE:
            self.references[self.title] = self.url
        elif name == self.ANOMALY_REFERENCES:
            self.anom.setReferences(self.references)

    def char_data(self, data):
        if self.tag == self.ANOMALY_DESCRIPTION:
            self.anom.setDescription(data)
#    elif self.tag==self.ANOMALY_SOLUTION:
#      self.anom.setSolution(data)
        elif self.tag == self.ANOMALY_REFERENCE_TITLE:
            self.title = data
        elif self.tag == self.ANOMALY_REFERENCE_URL:
            self.url = data
        self.tag = ""

    def getAnomalies(self):
        return self.anomalies