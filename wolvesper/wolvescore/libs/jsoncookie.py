#!/usr/bin/python
# -*- coding: utf-8 -*-

import json
import cookielib
import requests


class jsoncookie(object):
    """"Store and load cookies in a JSON formatted file."""
    
    def __init__(self):
        self.cookiedict = None
        self.fd = None
    
    def open(self, filename):
        if not filename:
            return None
        try:
            self.fd = open(filename, "r+")
            self.cookiedict = json.load(self.fd)
        except IOError:
            self.fd = open(filename, "w+")
            self.cookiedict = {}
        return self.cookiedict
    
    def addcookies(self, cookies):
        if not isinstance(cookies, requests.cookies.RequestsCookieJar):
            return False
        for domain, pathdict in cookies._cookies.items():
            dotdomain = domain if domain[0] == '.' else '.' + domain
            if dotdomain not in self.cookiedict.keys():
                self.cookiedict[dotdomain] = {}
            for path, keydict in pathdict.items():
                if path not in self.cookiedict[dotdomain].keys():
                    self.cookiedict[dotdomain][path] = {}
                for key, cookieobj in keydict.items():
                    if isinstance(cookieobj, cookielib.Cookie):
                        print cookieobj
                        cookie_attr = {"value": cookieobj.value, "expires": cookieobj.expires, "secure": cookieobj.secure, "port": cookieobj.port, "version": cookieobj.version}
                        self.cookiedict[dotdomain][path][key] = cookie_attr
    
    def cookiejar(self, domain):
        """ Return a cookielib.CookieJar object containing matching the given domain."""
        cj = cookielib.CookieJar()
        if not domain:
            return cj
        if ":" in domain:
            domain, port = domain.split(":", 1)
        
        if not '.' in domain:
            domain += ".local"
        
        dotdomain = domain if domain[0] == '.' else '.' + domain
        exploded = dotdomain.split(".")
        parent_domains = [".%s" % (".".join(exploded[x:])) for x in range(1, len(exploded) -1 )]
        matching_domains = [d for d in parent_domains if d in self.cookiedict]
        if not matching_domains:
            return cj
        
        for d in matching_domains:
            for path in self.cookiedict[d]:
                ck = cookielib.Cookie(version=cookie_attrs["version"],
                                      name = cookie_name,
                                      value = cookie_attrs["value"],
                                      port = None,
                                      port_specified = True,
                                      domain = d,
                                      domain_specified = True,
                                      domain_initial_dot = False,
                                      path=path,
                                      path_specified = True,
                                      secure=cookie_attrs["secure"],
                                      expires = cookie_attrs["expires"],
                                      discard = True,
                                      comment = None,
                                      comment_url = None,
                                      rest = {'HttpOnly': None},
                                      rfc2109=False
                                      )
                if cookie_attrs["port"]:
                    ck.port = cookie_attrs["port"]
                    ck.port_specified = True
                cj.set_cookie(ck)
        return cj
    
            