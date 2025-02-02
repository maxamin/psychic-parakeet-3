#!/usr/bin/env python

class ReportGenerator(object):
    def generateReport(self, filename):
        pass

    def setReportInfo(self, target, scope=None, date_string="", version=""):
        pass

    # Vulnerabilities
    def addVulnerabilityType(self, name, description="", solution="", references={}):
        pass

    def logVulnerability(self, category=None, level=0, request=None, parameter="", info=""):
        pass

    # Anomalies
    def addAnomalyType(self, name, description="", solution="", references={}):
        pass

    def logAnomaly(self, category=None, level=0, request=None, parameter="", info=""):
        pass