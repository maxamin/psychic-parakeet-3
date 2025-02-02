#!/usr/bin/python
# -*- coding: utf-8 -*-

from wolvescore.libs.xtext import XText
import os
import socket
import requests
import sys

modules = ["mod_exec", "mod_crlf", "mod_file", "mod_sql", "mod_xss", "mod_backup", "mod_htaccess", "mod_blindsql", "mod_nikto", "mod_permanentxss", "mod_delay"]

commons = ["blindsql", "exec", "file", "sql", "xss", "permanentxss"]

class Attack(object):
    name = "attack"
    
    doGET = True
    doPOST = True
    
    require = []
    
    if hasattr(sys, "frozen"):
        BASE_DIR = os.path.join(os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding())), "data")
    else:
        BASE_DIR = os.path.dirname(sys.modules['wolvescore'].__file__)
    CONFIG_DIR = os.path.join(BASE_DIR, "config", "attacks")
    
    STD = "\033[0;0m"
    RED = "\033[0;31m"
    GREEN = "\033[0;32m"
    ORANGE = "\033[0;33m"
    YELLOW = "\033[1;33m"
    BLUE = "\033[1;34m"
    MAGENTA = "\033[0;35m"
    CYAN = "\033[0;36m"
    GB = "\033[0;30m\033[47m"
    
    allowed = ['php', 'html', 'htm', 'xml', 'xhtml', 'xht', 'xhtm',
               'asp', 'aspx', 'php3', 'php4', 'php5', 'txt', 'shtm',
               'shtml', 'phtm', 'phtml', 'jhtml', 'pl', 'jsp', 'cfm',
               'cfml', 'py']
    
    PRIORITY = 5
    def __init__(self, http, report_gen):
        self.HTTP = http
        self.logVuls = report_gen.logVuls
        self.logNom = report_gen.logNom
        
        self.xText = XText()
        self.attackedGET = []
        self.attackedPOST = []
        
        self.vulsGET = []
        self.vulsPOST = []
        
        self.verbose = 0
        self.color = 0
        
        self.deps = []
    
    def setVerbose(self, verbose):
        self.verbose = verbose
    
    def setColor(self):
        self.color = 1
    
    def loadPayloads(self, filename):
        """Load the payloads from the specitifed file """
        return self.xText.readLines(filename)
    
    def attackGET(self, http_res):
        return
    
    def attackPOST(self, form):
        return
    
    def loadRequire(self, pbj=[]):
        self.deps = obj
    
    def log(self, fmt_string, *args):
        if  len(args) == 0:
            print(fmt_string)
        else:
            print(fmt_string.format(*args))
        if self.color:
            sys.stdout.write(self.STD)
    
    def logR(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.RED)
        self.log(fmt_string, *args)
    
    def logG(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.GREEN)
        self.log(fmt_string, *args)
        
    def logY(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.YELLOW)
        self.log(fmt_string, *args)    
    
    def logC(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.CYAN)
        self.log(fmt_string, *args)
    
    def logW(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.GB)
        self.log(fmt_string, *args)
    
    def logM(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.MAGENTA)
        self.log(fmt_string, *args)
    
    def logB(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.BLUE)
        self.log(fmt_string, *args)
    
    def logO(self, fmt_string, *args):
        if self.color:
            sys.stdout.write(self.ORANGE)
        self.log(fmt_string, *args)
    
    def attack(self, http_resources, forms):
        if self.doGET is True:
            for http_res in http_resources:
                url = http_res.url
                if self.verbose == 1:
                    self.log(_("+ attackGET {0} ", url))
                try:
                    self.attackGET(http_res)
                except socket.error, se:
                    self.log(_('error: {0} while aatacking {1}'), repr(str(se[0])), url)
                except requests.exceptions.Timeout:
                    self.log(_('error: timeout while attacking {0}'), url)
                
        if self.doPOST is True:
            for form in forms:
                if self.verbose == 1:
                    self.log(_("+ attackPOST {0} from {1}"), form.url, form.referer)
                try:
                    self.attackPOST(form)
                except socket.error, se:
                    self.log(_('error: {0} while attacking {1}'), repr(str(se[0])), url)
                except requests.exceptions.Timeout:
                    print(_('error: {0} while attacking {1}'), repr(str(se[0])), url)
                    