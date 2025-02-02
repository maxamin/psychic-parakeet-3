#!/usr/bin/env python

class ReportGeneratorInfo(object):

    def __init__(self):
        self.name = None
        self.className = None
        self.classModule = None

    def getKey(self):
        return self.name

    def getClassModule(self):
        return self.classModule

    def getClassName(self):
        return self.className

    def setKey(self, name):
        self.name = name

    def setClassModule(self, classModule):
        self.classModule = classModule

    def setClassName(self, className):
        self.className = className

    def createInstance(self):
        module = __import__(self.getClassModule(), globals(), locals(), ['NoName'], -1)
        repGenClass = getattr(module, self.getClassName())
        return repGenClass()