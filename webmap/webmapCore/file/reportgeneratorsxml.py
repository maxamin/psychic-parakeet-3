#!/usr/bin/env python

from xml.parsers import expat
from webmapCore.report.reportgeneratorinfo import ReportGeneratorInfo

class ReportGeneratorsXMLParser(object):

    REPORT_GENERATOR = "reportGenerator"
    REPORT_GENERATOR_KEY = "reportTypeKey"
    REPORT_GENERATOR_CLASS_MODULE = "classModule"
    REPORT_GENERATOR_CLASSNAME = "className"

    def __init__(self):
        self._parser = expat.ParserCreate()
        self._parser.StartElementHandler = self.start_element
        self._parser.EndElementHandler = self.end_element
        self._parser.CharacterDataHandler = self.char_data
        self.reportGenerators = []
        self.repGen = None
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
        if name == self.REPORT_GENERATOR:
            self.repGen = ReportGeneratorInfo()
        elif name == self.REPORT_GENERATOR_KEY:
            self.tag = self.REPORT_GENERATOR_KEY
        elif name == self.REPORT_GENERATOR_CLASSNAME:
            self.tag = self.REPORT_GENERATOR_CLASSNAME
        elif name == self.REPORT_GENERATOR_CLASS_MODULE:
            self.tag = self.REPORT_GENERATOR_CLASS_MODULE

    def end_element(self, name):
        if name == self.REPORT_GENERATOR:
            self.reportGenerators.append(self.repGen)

    def char_data(self, data):
        if self.tag == self.REPORT_GENERATOR_KEY:
            self.repGen.setKey(data)
        elif self.tag == self.REPORT_GENERATOR_CLASSNAME:
            self.repGen.setClassName(data)
        elif self.tag == self.REPORT_GENERATOR_CLASS_MODULE:
            self.repGen.setClassModule(data)
        self.tag = ""

    def getReportGenerators(self):
        return self.reportGenerators