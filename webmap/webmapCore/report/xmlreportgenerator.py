#!/usr/bin/env python

from xml.dom.minidom import Document
from webmapCore.report.reportgenerator import ReportGenerator


class XMLReportGenerator(ReportGenerator):
    """
    This class generates a report with the method printToFile(fileName) which contains
    the information of all the vulnerabilities notified to this object through the
    method logVulnerability(vulnerabilityTypeName,level,url,parameter,info).
    The format of the file is XML and it has the following structure:
    <report type="security">
        <generatedBy id="Webmap 2.3.0"/>
        <vulnerabilityTypeList>
            <vulnerabilityType name="SQL Injection">

        <vulnerabilityTypeList>
            <vulnerabilityType name="SQL Injection">
                <vulnerabilityList>
                    <vulnerability level="3">
                        <url>http://www.a.com</url>
                        <parameters>id=23</parameters>
                        <info>SQL Injection</info>
                    </vulnerability>
                </vulnerabilityList>
            </vulnerabilityType>
        </vulnerabilityTypeList>
    </report>
    """


    def __init__(self):
        self.__infos = {}
        self.__xmlDoc = Document()
        self.__flawTypes = {}

        self.__vulns = {}
        self.__anomalies = {}

    def setReportInfo(self, target, scope=None, date_string="", version=""):
        self.__infos["target"] = target
        self.__infos["date"] = date_string
        self.__infos["version"] = version
        if scope:
            self.__infos["scope"] = scope

    # Vulnerabilities
    def addVulnerabilityType(self, name,
                             description="",
                             solution="",
                             references={}):
        if name not in self.__flawTypes:
            self.__flawTypes[name] = {'desc': description,
                                      'sol': solution,
                                      'ref': references}
        if name not in self.__vulns:
            self.__vulns[name] = []

    def logVulnerability(self,
                         category=None,
                         level=0,
                         request=None,
                         parameter="",
                         info=""):
        """
        Store the information about the vulnerability to be printed later.
        The method printToFile(fileName) can be used to save in a file the
        vulnerabilities notified through the current method.
        """

        vuln_dict = {"method": request.method,
                     "path": request.file_path,
                     "info": info,
                     "level": level,
                     "parameter": parameter,
                     "http_request": request.http_repr,
                     "curl_command": request.curl_repr,
                     }
        if category not in self.__vulns:
            self.__vulns[category] = []
        self.__vulns[category].append(vuln_dict)

    # Anomalies
    def addAnomalyType(self, name,
                       description="",
                       solution="",
                       references={}):
        if name not in self.__flawTypes:
            self.__flawTypes[name] = {'desc': description,
                                      'sol': solution,
                                      'ref': references}
        if name not in self.__anomalies:
            self.__anomalies[name] = []

    def logAnomaly(self,
                   category=None,
                   level=0,
                   request=None,
                   parameter="",
                   info=""):
        """
        Store the information about the vulnerability to be printed later.
        The method printToFile(fileName) can be used to save in a file the
        vulnerabilities notified through the current method.
        """

        anom_dict = {"method": request.method,
                     "path": request.file_path,
                     "info": info,
                     "level": level,
                     "parameter": parameter,
                     "http_request": request.http_repr,
                     "curl_command": request.curl_repr,
                     }
        if category not in self.__anomalies:
            self.__anomalies[category] = []
        self.__anomalies[category].append(anom_dict)

    def generateReport(self, filename):
        """
        Create a xml file with a report of the vulnerabilities which have been logged with
        the method logVulnerability(vulnerabilityTypeName,level,url,parameter,info)
        """

        report = self.__xmlDoc.createElement("report")
        report.setAttribute("type", "security")
        self.__xmlDoc.appendChild(report)

        # Add report infos
        report_infos = self.__xmlDoc.createElement("report_infos")
        generatorName = self.__xmlDoc.createElement("info")
        generatorName.setAttribute("name", "generatorName")
        generatorName.appendChild(self.__xmlDoc.createTextNode("wapiti"))
        report_infos.appendChild(generatorName)

        generatorVersion = self.__xmlDoc.createElement("info")
        generatorVersion.setAttribute("name", "generatorVersion")
        generatorVersion.appendChild(self.__xmlDoc.createTextNode(self.__infos["version"]))
        report_infos.appendChild(generatorVersion)

        scope = self.__xmlDoc.createElement("info")
        scope.setAttribute("name", "scope")
        scope.appendChild(self.__xmlDoc.createTextNode(self.__infos["scope"]))
        report_infos.appendChild(scope)

        dateOfScan = self.__xmlDoc.createElement("info")
        dateOfScan.setAttribute("name", "dateOfScan")
        dateOfScan.appendChild(self.__xmlDoc.createTextNode(self.__infos["date"]))
        report_infos.appendChild(dateOfScan)
        report.appendChild(report_infos)

        vulnerabilities = self.__xmlDoc.createElement("vulnerabilities")
        anomalies = self.__xmlDoc.createElement("anomalies")

        # Loop on each flaw classification
        for flawType in self.__flawTypes:
            container = None
            classification = ""
            flaw_dict = {}
            if flawType in self.__vulns:
                container = vulnerabilities
                classification = "vulnerability"
                flaw_dict = self.__vulns
            elif flawType in self.__anomalies:
                container = anomalies
                classification = "anomaly"
                flaw_dict = self.__anomalies

            # Child nodes with a description of the flaw type
            flawTypeNode = self.__xmlDoc.createElement(classification)
            flawTypeNode.setAttribute("name", flawType)
            flawTypeDesc = self.__xmlDoc.createElement("description")
            flawTypeDesc.appendChild(self.__xmlDoc.createCDATASection(self.__flawTypes[flawType]['desc']))
            flawTypeNode.appendChild(flawTypeDesc)
            flawTypeSolution = self.__xmlDoc.createElement("solution")
            flawTypeSolution.appendChild(self.__xmlDoc.createCDATASection(self.__flawTypes[flawType]['sol']))
            flawTypeNode.appendChild(flawTypeSolution)

            flawTypeReferences = self.__xmlDoc.createElement("references")
            for ref in self.__flawTypes[flawType]['ref']:
                referenceNode = self.__xmlDoc.createElement("reference")
                titleNode = self.__xmlDoc.createElement("title")
                urlNode = self.__xmlDoc.createElement("url")
                titleNode.appendChild(self.__xmlDoc.createTextNode(ref))
                url = self.__flawTypes[flawType]['ref'][ref]
                urlNode.appendChild(self.__xmlDoc.createTextNode(url))
                referenceNode.appendChild(titleNode)
                referenceNode.appendChild(urlNode)
                flawTypeReferences.appendChild(referenceNode)
            flawTypeNode.appendChild(flawTypeReferences)

            # And child nodes with each flaw of the current type
            entriesNode = self.__xmlDoc.createElement("entries")
            for flaw in flaw_dict[flawType]:
                entryNode = self.__xmlDoc.createElement("entry")
                methodNode = self.__xmlDoc.createElement("method")
                methodNode.appendChild(self.__xmlDoc.createTextNode(flaw["method"]))
                entryNode.appendChild(methodNode)
                pathNode = self.__xmlDoc.createElement("path")
                pathNode.appendChild(self.__xmlDoc.createTextNode(flaw["path"]))
                entryNode.appendChild(pathNode)
                levelNode = self.__xmlDoc.createElement("level")
                levelNode.appendChild(self.__xmlDoc.createTextNode(str(flaw["level"])))
                entryNode.appendChild(levelNode)
                parameterNode = self.__xmlDoc.createElement("parameter")
                parameterNode.appendChild(self.__xmlDoc.createTextNode(flaw["parameter"]))
                entryNode.appendChild(parameterNode)
                infoNode = self.__xmlDoc.createElement("info")
                infoNode.appendChild(self.__xmlDoc.createTextNode(flaw["info"]))
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
        report.appendChild(vulnerabilities)
        report.appendChild(anomalies)

        f = open(filename, "w")
        try:
            f.write(self.__xmlDoc.toprettyxml(indent="    ", encoding="UTF-8"))
        finally:
            f.close()