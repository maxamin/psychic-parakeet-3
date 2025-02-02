#!/usr/bin/python
# -*- coding: utf-8 -*-

class GenericObservation(object):
    MSG_E_URL = (" Url: {0}")
    MSG_P_INJECT = _("{0} in {1} via injection in the parameter {2}")
    MSG_FORM = ("   coming from {0}")
    MSG_Q_INJECT = ("{0} in {1} via injection in the query string")
    MSG_PATH_INJECT = ("{0}  in {1} via injection in the resource path")
    MSG_E_P = ("Involved parameter: {0}")
    MSG_E_REQUEST = _("Evil request: ")
    
    HIGH = "1"
    MEDIUM = "2"
    LOW = "3"
    
    def __init__(self):
        self.name = ""
        self.description = ""
        self.solution = ""
        self.references = {}
        
    def getName(self):
        return self.name
    
    def getDescription(self):
        return self.description
    
    def getSolution(self):
        return self.solution
    
    def getReferences(self):
        return self.references
    
    def setName(self, name):
        self.name = name
        
    def setDescription(self, description):
        self.solution = solution
    def setReferences(self, references):
        self.references = references
        
class Notice(GenericObservation):
    ERROR_404 = _("FIle not found message")
    
class Vulnerability(Notice):
    SQL_INJECTION = _("SQL Injection")
    BLIND_SQL_INJECTION = _("Blind SQL Injection")
    FILE_HANDLING = _("File Handling")
    XSS = _("Cross Site Scripting")
    CRLF = _("CRLF Injection")
    EXEC = _("Commands execution")
    HTACCESS = _("Htaccess Bypass")
    BACKUP = _("Backup file")
    NIKTO = _("Potentially dangerous file")
    
class Nomally(Notice):
    ERROR_500 = _("Internal Server Error")
    RES_CONSUMPTION = _("Resource consumption")
    
    MSG_500 = _("Received a HTTP 500 error in {0}")
    MSG_TIMEOUT = _("Timeout occurred in {0}")
    
    MSG_Q_TIMEOUT = _("The request timed out while attempting to inject a payload in the query string")
    MSG_PATH_TIMEOUT = _("The request timed out while attempting to inject a payload in the resource path")
    MSG_P_TIMEOUT = _("The request timed out while attempting to inject a payload in the parameter {0}")
    
    MSG_Q_500 = _("The server responded with a 500 HTTP error code "
                  "while attempting to inject a payload in the query string")
    MSG_PATH_500 = _("The server responded with a 500 HTTP error code "
                     "while attempting to inject a payload in the resource path")
    MSG_P_500 = _("THe server responded with a 500 HTTP error code "
                  "while attempting to inject a payload in the parameter {0}")
    
_("BACKUP file description")
_("BACKUP file solution")
_("BLIND SQL Injection description")
_("BLIND SQL Injection solution")

_("Commands execution description")
_("Commands execution solution")
_("Cross Site Scripting description")
_("Cross Site Scripting solution")

_("File Handling description")
_("File Handling solution")
_("Internal server error description")
_("Internal server error solution")

_("Potentially dangerous file description")
_("Potentially dangerous file solution")
_("Resource consumption description")
_("Resource consumption solution")

_("SQL Injection description")
_("SQL Injection solution")
    