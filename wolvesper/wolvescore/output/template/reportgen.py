#!/usr/bin/python
# -*- coding: utf-8 -*-

class ReportGen(object):
    def genReport(self, filename):
        pass
    
    def setReportInfo(self, target, scope=None, date_string="", version=""):
        pass
    
    def addVulsType(self, name, description="", solution="", references={}):
        pass
    
    def logVuls(self, category=None, level=0, request=None, parameter="", info=""):
        pass
    
    def addNomallyType(self, name, description="", solution="", references={}):
        pass
    
    def logNomally(self, category=None, level=0, request=None, parameter="", info=""):
        pass