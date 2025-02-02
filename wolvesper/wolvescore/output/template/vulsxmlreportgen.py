#!/usr/bin/python
# -*- coding: utf-8 -*-

from xml.dom.minidom import Document
from wolvescore.output.template.reportgen import ReportGen
import datetime

def isPeerAddrPort(p):
    if type(p) == tuple and len(p) == 2:
        return type(p[0]) == str and type(p[1]) == int
    else:
        return False
    
class VulsXMLReportGen(ReportGen):
    def __init__(self):
        self.__ts = datetime.datetime.now()
        self.__xmlDoc = Document()
        self.__vulsTypeList = None
        
    def setReportInfo(self, target, scope=None, date_string="", version=""):
        report = self.__xmlDoc.createElement("Report")
        
        report.setAttribute("generatedBy", version)
        report.setAttribute("generationDate", self.__ts.isoformat())
        self.__vulsTypeList = self.__xmlDoc.createElement("VulnerabilityTypeList")
        report.appendChild(self.__vulsTypeList)
        
        self.__xmlDoc.appendChild(report)
    
    def __addToVulsTypeList(self, vulsType):
        self.__vulsTypeList.appendChild(vulsType)
    
    def addVulsType(self, name, description="", solution="", references={}):
        vulsType = self.__xmlDoc.createElement("VulsType")
        vulsType.appendChild(self.__xmlDoc.createElement("VulsList"))
        
        vulsTitleNode = self.__xmlDoc.createElement("Title")
        vulsTitleNode.appendChild(self.__xmlDoc.createTextNode(name))
        vulsType.appendChild(vulsTitleNode)
        
        self.__addToVulsTypeList(vulsType)
        if description != "":
            descriptionNode = self.__xmlDoc.createElement("Description")
            descriptionNode.appendChild(self.__xmlDoc.createCDATASection(description))
            vulsType.appendChild(descriptionNode)
        if solution != "":
            solutionNode = self.__xmlDoc.createElement("Solution")
            solutionNode.appendChild(self.__xmlDoc.createCDATASection(solution))
            vulsType.appendChild(solutionNode)
        if references != "":
            referencesNode = self.__xmlDoc.createElement("References")
            for ref in references:
                referenceNode = self.__xmlDoc.createElement("Reference")
                nameNode = self.__xmlDoc.createElement("name")
                urlNode = self.__xmlDoc.createElement("url")
                nameNode.appendChild(self._xmlDoc.createTextNode(ref))
                urlNode.appendChild(self.__xmlDoc.createTextNode(references[ref]))
                referenceNode.appendChild(nameNode)
                referenceNode.appendChild(urlNode)
                referencesNode.appendChild(referenceNode)
                referencesNode.appendChild(referencesNode)
            vulsType.appendChild(referenceNode)
        return vulsType
    
    def __addToVulsList(self, category, vuls):
        vulsType = None
        for node in self.__vulsTypeList.childNodes:
            titleNode = node.getElementsByTagName("Title")
            if (titleNode.length >= 1 and titleNode[0].childNodes.length == 1 and titleNode[0].childNodes[0].wholeText == category):
                vulsType = None
                break
        if vulsType is None:
            vulsType = self.addVulsType(category)
        vulsType.childNodes[0].appendChild(vuls)
        
    def logVuls(self, category=None, level=0, reuqest=None, parameter="", info=""):
        peer = None
        vuls = self.__xmlDoc.createElement("Vuls")
        
        if level == 1:
            stLevel = "Low"
        elif level == 2:
            stLevel = "Moder"
        else:
            stLevel = "Important"
        levelNode = self.__xmlDoc.createElement("Severity")
        levelNode.appendChild(self.__xmlDoc.createTextNode(stLevel))
        vuls.appendChild(levelNode)
        
        tsNode = self.__xmlDoc.createElement("DetectionDate")
        vuls.appendChild(tsNode)
        
        urlDetailNode = self.__xmlDoc.createElement("URLDetail")
        vuls.appendChild(urlDetailNode)
        
        urlNode = self.__xmlDoc.createElement("URL")
        urlNode.appendChild(self.__xmlDoc.createTextNode(request.url))
        urlDetailNode.appendChild(urlNode)
        
        if peer is not None:
            peerNode = self.__xmlDoc.createElement("Addr")
            if isPeerAddrPort(peer):
                addrNode = self.__xmlDoc.createElement("Addr")
                addrNode.appendChild(self.__xmlDoc.createTextNode(peer[0]))
                peerNode.appendChild(addrNode)
                
                portNode = self.__xmlDoc.createElement("Addr")
                portNode.appendChild(self.__xmlDoc.createTextNode(str(peer[1])))
                peerNode.appendChild(portNode)
                
            else:
                addrNode = self.__xmlDoc.createElement("Addr")
                addrNode.appendChild(self.__xmlDoc.createTextNode(str(peer)))
                peerNode.appendChild(addrNode)
            urlDetailNode.appendChild(peerNode)
            
        parameterNode = self.__xmlDoc.createElement("Parameter")
        parameterNode.appendChild(self.__xmlDoc.createTextNode(parameter))
        urlDetailNode.appendChild(parameterNode)
        
        infoNode = self.__xmlDoc.createElement("Info")
        info = info.replace("\n", "<br />")
        infoNode.appendChild(self.__xmlDoc.createTextNode(info))
        urlDetailNode.appendChild(infoNode)
        
        self.__addToVulsList(category, vuls)
        
    def genReport(self, filename):
        f = open(filename, "w")
        try:
            f.write(self.__xmlDoc.toxml(encoding="UTF-8"))
        finally:
            f.close()
            
        
        