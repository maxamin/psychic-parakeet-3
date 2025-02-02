#!/usr/bin/python
# -*- coding: utf-8 -*-

from xml.dom.minidom import Document
from wolvescore.output.template.reportgen import ReportGen

class XMLReportGen(ReportGen):
    def __init__(self):
        self.__infos = {}
        self.__xmlDoc = Document()
        self.__flawTypes = {}
        
        self.__vuls = {}
        self.__anomalies = {}
        
    def setReportInfo(self, target, scope=None, date_string="", version=""):
        self.__infos["target"] = target
        self.__infos["date"] = date_string
        self.__infos["version"] = version
        
        if scope:
            self.__infos["scope"] = scope
        
        #Vuls
        def addVulsType(self, name, description="", solution="", references={}):
            if name not in self.__flawTypes:
                self.__flawTypes[name] = {'desc': description, 'sol': solution, 'ref': references}
            
            if name not in self.__vuls:
                self.__vuls[name] = []
        
        def logVuls(self, category=None, level=0, request=None, parameter="", info=""):
            vuls_dict = {"method": request.method,
                         "path": request.file_path,
                         "info": info,
                         "level": level,
                         "parameter": parameter,
                         "http_request": request.http_repr,
                         "curl_command": request.curl_repr,
                            }
            if category not in self.__vuls:
                self.__vuls[category] = []
            self.__vuls[category].append(vuls_dict)
            
        def addNomallyType(self, name, description="", solution="", references={}):
            if name not in self.__flawTypes:
                self.__flawTypes[name] = {'desc': description, 'sol': solution, 'ref': references}
            if name not in self.__nomally:
                self.__nomally[name] = []
            
        def logNomally(self, category=None, level=0, request=None, parameter="", info=""):
            nom_dict = {"method": request.method,
                        "path": request.file_path,
                        "info": info,
                        "level": level,
                        "parameter": parameter,
                        "http_request": request.http_repr,
                        "curl_command": request.curl_repr,
                        }
            if category not in self.__nomally:
                self.__nomally[category] = []
            self.__nomally[category].append(nom_dict)
        
        def genReport(self, filename):
            report = self.__xmlDoc.createElement("report")
            report.setAttribute("type", "security")
            self.__xmlDoc.appendChild(report)
            
            report_infos = self.__xmlDoc.createElement("report_infos")
            genName = self.__xmlDoc.createElement("info")
            genName.setAttribute("name", "genName")
            genName.appendChild(self.__xmlDoc.createTextNode("wolves"))
            report_infos.appendChild(genName)
            
            genVersion = self.__xmlDoc.createElement("info")
            genVersion.setAttribute("name", "genVersion")
            genVersion.appendChild(self.__xmlDoc.createTextNode(self.__infos["version"]))
            report_infos.appendChild(genVersion)
            
            scope = self.__xmlDoc.createElement("info")
            scope.setAttribute("name", "scope")
            scope.appendChild(self.__xmlDoc.createTextNode(self.__infos["scope"]))
            report_infos.appendChild(scope)
            
            dateOfScan = self.__xmlDoc.createElement("info")
            dateOfScan.setAttribute("name", "dateOfScan")
            dateOfScan.appendChild(self.__xmlDoc.createTextNode(self.__infos["date"]))
            report_infos.appendChild(dateOfScan)
            report.appendChild(report_infos)
            
            vuls = self.__xmlDoc.createElement("vuls")
            nomally = self.__xmlDoc.createElement("nomally")
            
            for flawType in self.__flawTypes:
                container = None
                classification = ""
                flaw_dict = {}
                if flawType in self.__vuls:
                    container = vuls
                    classification = "vuls"
                    flaw_dict = self.__vuls
                elif flawType in self.__nomally:
                    container = nomally
                    classification = "nomally"
                    flaw_dict = self.__nomally
                flawTypeNode = self.__xmlDoc.createElement(classification)
                flawTypeNode.setAttribute("name", flawType)
                flawTypeDesc = self.__xmlDoc.createElement("description")
                flawTypeDesc.appendChild(self.__xmlDoc.createCDATASection(self.__flawTypes[flawType]['desc']))
                flawTypeDesc.appendChild(flawTypeDesc)
                flawTypeSolution = self.__xmlDoc.createElement("solution")
                flawTypeSolution.appendChild(self.__xmlDoc.createCDATASection(self.__flawTypes[flawType]['sol']))
                flawTypeNode.appendChild(flawTypeSolution)
                
                flawTypeReferences = self.__xmlDoc.createElement("references")
                for ref in self.__flawTypes[flawType]['ref']:
                    referenceNode = self.__xmlDoc.createElement("reference")
                    titleNode = self.__xmlDoc.createElement("title")
                    urlNode = self.__xmlDoc.createElement("url")
                    titleNode.appendChild(self.__xmlDoc.createTextNode(ref))
                    urlNode.appendChild(self.__xmlDoc.createTextNode(url))
                    referenceNode.appendChild(titleNode)
                    referenceNode.appendChild(urlNode)
                    flawTypeReferences.appendChild(referenceNode)
                flawTypeNode.appendChild(flawTypeReferences)
                
                entriesNode = self.__xmlDoc.createElement("entry")
                for flaw in flaw_dict[flawType]:
                    entryNode = self.__xmlDoc.createElement("entry")
                    methodNode = self.__xmlDoc.createElement("method")
                    methodNode.appendChild(self.__xmlDoc.createTextNode(flaw["method"]))
                    entryNode.appendChild(methodNode)
                    pathNode = self.__xmlDoc.createElement("path")
                    pathNode.appendChild(self.__xmlDoc.createTextNode(str(flaw["level"])))
                    entryNode.appendChild(pathNode)
                    levelNode = self.__xmlDoc.createElement("path")
                    levelNode.appendChild(self.__xmlDoc.createTextNode(str(flaw["level"])))
                    entryNode.appendChild(levelNode)
                    parameterNode = self.__xmlDoc.createElement("parameter")
                    parameterNode.appendChild(self.__xmlDoc.createTextNode(flaw["parameter"]))
                    entryNode.appendChild(parameterNode)
                    infoNode = self.__xmlDoc.createElement("info")
                    infoNode.appendChild(self.__xmlDoc.createCDATASection(flaw["info"]))
                    entryNode.appendChild(infoNode)
                    
                    httpRequestNode = self.__xmlDoc.createElement("http_request")
                    httpRequestNode.appendChild(self.__xmlDoc.createCDATASection(flaw["http_request"]))
                    entryNode.appendChild(httpRequestNode)
                    
                    curlCommandNode = self.__xmlDoc.createElement("curl_command")
                    curlCommandNode.appendChild(self.__xmlDoc.createCDATASection(flaw["curl_command"]))
                    entryNode.appendChild(curlCommandNode)
                    entriesNode.appendChild(entryNode)
                flawTypeNode.appendChild(entriesNode)
                container.appendChild(flawTypeNode)
            report.appendChild(vuls)
            report.appendChild(nomally)
                    
            f = open(filename, "w")
            try:
                f.write(self.__xmlDoc.toprettyxml(indent="      ", encoding="UTF-8"))
            finally:
                f.close()
            