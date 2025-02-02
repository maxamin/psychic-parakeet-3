#!/usr/bin/python
# -*- coding: utf-8 -*-

from xml.parsers import expat
from wolvescore.output.contexts.context import Nomally

class NomallyXML(object):
    
    NOMALLY = "nomally"
    NOMALLY_NAME = "name"
    NOMALLY_DESCRIPTION = "description"
    NOMALLY_SOLUTION = "solution"
    NOMALLY_REFERENCE = "reference"
    NOMALLY_REFERENCES = "references"
    NOMALLY_REFERENCE_TITLE = "title"
    NOMALLY_REFERNCE_URL = "url"
    
    def __init__(self):
        self._parser = expat.ParserCreate()
        self._parser.StartElementHandler = self.start_element
        self._parser.EndElementHandler = self.end_element
        self._parser.CharacterDataHandler = self.char_data
        
        self.nomally = []
        self.nom = None
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
        if name == self.NOMALLY:
            self.nom = Nomally()
            self.nom.setName(attrs[self.NOMALLY_NAME])
        elif name == self.NOMALLY_DESCRIPTION:
            self.tag = self.NOMALLY_DESCRIPTION
        elif name == self.NOMALLY_SOLUTION:
            self.nom.setSolution(attrs["text"])
        elif name == self.NOMALLY_REFERENCES:
            self.references = {}
        elif name == self.NOMALLY_REFERENCE:
            self.tag = self.NOMALLY_REFERENCE
        elif name == self.NOMALLY_REFERENCE_TITLE:
            self.tag = self.NOMALLY_REFERNCE_TITLE
        elif name == self.NOMALLY_REFERENCE_URL:
            self.tag = self.NOMALLY_REFERENCE_URL
        
    def end_element(self, name):
        if name == self.NOMALLY:
            self.nomally.append(self.nom)
        elif name == self.NOMALLY_REFERENCE:
            self.references[self.title] = self.url
        elif name == self.NOMALLY_REFERENCES:
            self.nom.setReferences(self.references)
    
    def char_data(self, data):
        if self.tag == self.NOMALLY_DESCRIPTION:
            self.nom.setDescription(data)
        elif self.tag == self.NOMALLY_REFERENCE_TITLE:
            self.title = data
        elif self.tag == self.NOMALLY_REFERNCE_URL:
            self.url = data
        self.tag = ""
        
    def getNomally(self):
        return self.nomally