#!/usr/bin/env python
import  socket
import requests
import os
from webmapCore.attack.attack import Attack
from webmapCore.language.vulnerability import Vulnerability, Anomaly
from webmapCore.net import HTTP


class mod_permanentxss(Attack):
    """
    This class implements a cross site scripting attack
    """

    # magic strings we must see to be sure script is vulnerable to XSS
    # payloads must be created on those patterns
    script_ok = ["alert('__XSS__')", "alert(\"__XSS__\")", "String.fromCharCode(0,__XSS__,1)"]

    # simple payloads that doesn't rely on their position in the DOM structure
    # payloads injected after closing a tag attribute value (attrval) or in the
    # content of a tag (text node like between <p> and </p>)
    # only trick here must be on character encoding, filter bypassing, stuff like that
    # form the simplest to the most complex, webmap will stop on the first working
    independant_payloads = []

    name = "permanentxss"
    require = ["xss"]
    PRIORITY = 6

    # two dict for permanent XSS scanning
    GET_XSS = {}
    POST_XSS = {}

    # key = xss code, valid = payload
    SUCCESSFUL_XSS = {}

    CONFIG_FILE = "xssPayloads.txt"

    MSG_VULN = ("Stored XSS vulnerability")

    def __init__(self, http, xmlRepGenerator):
        Attack.__init__(self, http, xmlRepGenerator)
        self.independant_payloads = self.loadPayloads(os.path.join(self.CONFIG_DIR, self.CONFIG_FILE))

    # permanent XSS
    def attack(self, get_resources, forms):
        """This method searches XSS which could be permanently stored in the web application"""
        for http_resource in get_resources:
            if http_resource.method != "GET":
                continue
            url = http_resource.url
            target_req = HTTP.HTTPResource(url)
            referer = http_resource.referer
            headers = {}
            if referer:
                headers["referer"] = referer
            if self.verbose >= 1:
                print(u"+ {0}".format(url))
            try:
                resp = self.HTTP.send(target_req, headers=headers)
                data = resp.getPage()
            except requests.exceptions.Timeout, timeout:
                data = ""
            except socket.error, se:
                data = ""
                print(('error: {0} while attacking {1}').format(repr(str(se[1])), url))
            except Exception, e:
                print(('error: {0} while attacking {1}').format(repr(str(e[0])), url))
                continue

            # Search for permanent XSS vulns which were injected via GET
            if self.doGET == 1:
                for code in self.GET_XSS:
                    if code in data:
                        # code found in the webpage !
                        code_url = self.GET_XSS[code][0].url
                        page = self.GET_XSS[code][0].path
                        param_name = self.GET_XSS[code][1]
                        if code in self.SUCCESSFUL_XSS:
                            # is this an already known vuln (reflected XSS)
                            if self.validXSS(data, code, self.SUCCESSFUL_XSS[code]):
                                # if we can find the payload again, this is a stored XSS
                                evil_req = HTTP.HTTPResource(code_url.replace(code, self.SUCCESSFUL_XSS[code]))

                                if param_name == "QUERY_STRING":
                                    self.logR(Vulnerability.MSG_QS_INJECT, self.MSG_VULN, page)
                                else:
                                    self.logR(Vulnerability.MSG_PARAM_INJECT, self.MSG_VULN, page, param_name)
                                self.logR(Vulnerability.MSG_EVIL_URL, code_url)

                                self.logVuln(category=Vulnerability.XSS,
                                             level=Vulnerability.HIGH_LEVEL,
                                             request=evil_req,
                                             info=("Found permanent XSS in {0}"
                                                    " with {1}").format(page, self.HTTP.escape(evil_req.url)))
                                # we reported the vuln, now search another code
                                continue

                        # we where able to inject the ID but will we be able to inject javascript?
                        else:
                            timeouted = False
                            returned500 = False

                            for xss in self.independant_payloads:
                                payload = xss.replace("__XSS__", code)
                                evil_req = HTTP.HTTPResource(code_url.replace(code, payload))
                                try:
                                    http_code = self.HTTP.send(evil_req).getCode()
                                    dat = self.HTTP.send(target_req).getPage()
                                except requests.exceptions.Timeout:
                                    dat = ""
                                    if timeouted:
                                        continue
                                    self.logO(Anomaly.MSG_TIMEOUT, evil_req.url)
                                    self.logO(Anomaly.MSG_EVIL_REQUEST)
                                    self.logC(evil_req.http_repr)
                                    print('')
                                    self.logAnom(category=Anomaly.RES_CONSUMPTION,
                                                 level=Anomaly.MEDIUM_LEVEL,
                                                 request=evil_req,
                                                 parameter=param_name,
                                                 info=Anomaly.MSG_PARAM_TIMEOUT.format(param_name))
                                    timeouted = True

                                except Exception, e:
                                    print(('error: {0} while attacking {1}').format(repr(str(e[0])), url))
                                    continue

                                if self.validXSS(dat, code, payload):
                                    # injection successful :)
                                    if param_name == "QUERY_STRING":
                                        self.logR(Vulnerability.MSG_QS_INJECT, self.MSG_VULN, page)
                                    else:
                                        self.logR(Vulnerability.MSG_PARAM_INJECT, self.MSG_VULN, page, param_name)
                                    self.logR(Vulnerability.MSG_EVIL_URL, evil_req.url)

                                    self.logVuln(category=Vulnerability.XSS,
                                                 level=Vulnerability.HIGH_LEVEL,
                                                 request=evil_req,
                                                 info=("Found permanent XSS in {0}"
                                                        " with {1}").format(url, self.HTTP.escape(evil_req.url)))
                                    # look for another code in the webpage
                                    break
                                elif http_code == "500" and not returned500:
                                    self.logAnom(category=Anomaly.ERROR_500,
                                                 level=Anomaly.HIGH_LEVEL,
                                                 request=evil_req,
                                                 parameter=param_name,
                                                 info=Anomaly.MSG_PARAM_500.format(param_name))
                                    self.logO(Anomaly.MSG_500, evil_req.url)
                                    self.logO(Vulnerability.MSG_EVIL_REQUEST)
                                    self.logC(evil_req.http_repr)
                                    print('')
                                    returned500 = True

            if self.doPOST == 1:
                for code in self.POST_XSS:
                    if code in data:
                        # code found in the webpage
                        if code in self.SUCCESSFUL_XSS:
                            # this code has been used in a successful attack
                            if self.validXSS(data, code, self.SUCCESSFUL_XSS[code]):

                                code_req = self.POST_XSS[code][0]
                                get_params = code_req.get_params
                                post_params = code_req.post_params
                                file_params = code_req.file_params
                                referer = code_req.referer

                                for params_list in [get_params, post_params, file_params]:
                                    for i in xrange(len(params_list)):
                                        param_name, v = params_list[i]
                                        param_name = self.HTTP.quote(param_name)
                                        if v == code:
                                            if params_list is file_params:
                                                params_list[i][1][0] = self.SUCCESSFUL_XSS[code]
                                            else:
                                                params_list[i][1] = self.SUCCESSFUL_XSS[code]

                                            # we found the xss payload again -> stored xss vuln
                                            evil_req = HTTP.HTTPResource(code_req.path,
                                                                         method="POST",
                                                                         get_params=get_params,
                                                                         post_params=post_params,
                                                                         file_params=file_params,
                                                                         referer=referer)

                                            self.logVuln(category=Vulnerability.XSS,
                                                         level=Vulnerability.HIGH_LEVEL,
                                                         request=evil_req,
                                                         parameter=param_name,
                                                         info=("Found permanent XSS attacked by {0} with fields"
                                                                " {1}").format(evil_req.url,
                                                                               self.HTTP.encode(post_params)))
                                            self.logR(Vulnerability.MSG_PARAM_INJECT,
                                                      self.MSG_VULN,
                                                      evil_req.path,
                                                      param_name)
                                            self.logR(Vulnerability.MSG_EVIL_REQUEST)
                                            self.logC(evil_req.http_repr)
                                            print('')
                                            # search for the next code in the webpage
                                    continue

                        # we found the code but no attack was made
                        # let's try to break in
                        else:
                            code_req = self.POST_XSS[code][0]
                            get_params = code_req.get_params
                            post_params = code_req.post_params
                            file_params = code_req.file_params
                            referer = code_req.referer

                            for params_list in [get_params, post_params, file_params]:
                                for i in xrange(len(params_list)):
                                    param_name, v = params_list[i]
                                    param_name = self.HTTP.quote(param_name)
                                    if v == code:
                                        timeouted = False
                                        returned500 = False
                                        for xss in self.independant_payloads:
                                            payload = xss.replace("__XSS__", code)
                                            if params_list is file_params:
                                                params_list[i][1][0] = payload
                                            else:
                                                params_list[i][1] = payload
                                            try:
                                                evil_req = HTTP.HTTPResource(code_req.path,
                                                                             method=code_req.method,
                                                                             get_params=get_params,
                                                                             post_params=post_params,
                                                                             file_params=file_params,
                                                                             referer=referer)
                                                http_code = self.HTTP.send(evil_req).getCode()
                                                dat = self.HTTP.send(target_req).getPage()
                                            except requests.exceptions.Timeout:
                                                dat = ""
                                                if timeouted:
                                                    continue
                                                self.logO(Anomaly.MSG_TIMEOUT, evil_req.url)
                                                self.logO(Anomaly.MSG_EVIL_REQUEST)
                                                self.logC(evil_req.http_repr)
                                                print('')
                                                self.logAnom(category=Anomaly.RES_CONSUMPTION,
                                                             level=Anomaly.MEDIUM_LEVEL,
                                                             request=evil_req,
                                                             parameter=param_name,
                                                             info=Anomaly.MSG_PARAM_TIMEOUT.format(param_name))
                                                timeouted = True
                                            except Exception, e:
                                                print(('error: {0} while attacking {1}')
                                                      .format(repr(str(e[0])), url))
                                                continue
                                            if self.validXSS(dat, code, payload):
                                                self.logVuln(category=Vulnerability.XSS,
                                                             level=Vulnerability.HIGH_LEVEL,
                                                             request=evil_req,
                                                             parameter=param_name,
                                                             info=("Found permanent XSS attacked by {0} with fields"
                                                                    " {1}").format(evil_req.url,
                                                                                   self.HTTP.encode(post_params)))

                                                self.logR(Vulnerability.MSG_PARAM_INJECT,
                                                          self.MSG_VULN,
                                                          evil_req.path,
                                                          param_name)
                                                self.logR(Vulnerability.MSG_EVIL_REQUEST)
                                                self.logC(evil_req.http_repr)
                                                print('')
                                                break
                                            elif http_code == "500" and not returned500:
                                                self.logAnom(category=Anomaly.ERROR_500,
                                                             level=Anomaly.HIGH_LEVEL,
                                                             request=evil_req,
                                                             parameter=param_name,
                                                             info=Anomaly.MSG_PARAM_500.format(param_name))
                                                self.logO(Anomaly.MSG_500, evil_req.url)
                                                self.logO(Vulnerability.MSG_EVIL_REQUEST)
                                                self.logC(evil_req.http_repr)
                                                print('')
                                                returned500 = True

    # check whether our JS payload is injected in the webpage
    @staticmethod
    def validXSS(page, code, payload):
        if page is None or page == "":
            return False
        if payload.lower() in page.lower():
            return True
        return False

    @staticmethod
    def validContentType(http_res):
        """Check whether the returned content-type header allow javascript evaluation."""
        if not "content-type" in http_res.headers:
            return True
        if "text/html" in http_res.headers["content-type"]:
            return True
        return False

    def loadRequire(self, obj=[]):
        self.deps = obj
        for x in self.deps:
            if x.name == "xss":
                self.GET_XSS = x.GET_XSS
                self.POST_XSS = x.POST_XSS
                self.SUCCESSFUL_XSS = x.SUCCESSFUL_XSS