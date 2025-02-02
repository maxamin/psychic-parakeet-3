#!/usr/bin/env python

from webmapCore.attack.attack import Attack
from webmapCore.language.vulnerability import Vulnerability, Anomaly
from webmapCore.net import HTTP


class mod_htaccess(Attack):
    """
    This class implements a htaccess attack
    """

    name = "htaccess"

    doGET = False
    doPOST = False

    def __init__(self, http, xmlRepGenerator):
        Attack.__init__(self, http, xmlRepGenerator)

    #this function return code signification when htaccess protection enabled
    @staticmethod
    def __returnErrorByCode(code):
        code = int(code)
        if code == 401:
            err = "Authorization Required"
        elif code == 402:
            err = "Payment Required"
        elif code == 403:
            err = "Forbidden"
        else:
            err = "ok"
        return err

    def attackGET(self, http_res):
        url = http_res.path
        resp_headers = http_res.headers
        referer = http_res.referer
        headers = {}
        if referer:
            headers["referer"] = referer

        if url not in self.attackedGET:
            if self.verbose == 2:
                print(u"+ {0}".format(url))

            err1 = self.__returnErrorByCode(resp_headers["status_code"])

            if err1 != "ok":
                test_req = HTTP.HTTPResource(url)
                data1 = self.HTTP.send(test_req, headers=headers).getPage()
                # .htaccess protection detected
                if self.verbose >= 1:
                    self.log(_("HtAccess protection found: {0}"), url)

                evil_req = HTTP.HTTPResource(url, method="ABC")
                data2, code2 = self.HTTP.send(evil_req, headers=headers).getPageCode()
                err2 = self.__returnErrorByCode(code2)

                if err2 == "ok":
                    # .htaccess bypass success

                    if self.verbose >= 1:
                        self.logC(_("|HTTP Code: {0} : {1}"), resp_headers["status_code"], err1)

                    if self.verbose == 2:
                        self.logY(_("Source code:"))
                        self.logW(data1)

                    self.logVuln(category=Vulnerability.HTACCESS,
                                 level=Vulnerability.HIGH_LEVEL,
                                 request=evil_req,
                                 info=_("{0} HtAccess").format(err1))
                    self.logR(_("  .htaccess bypass vulnerability: {0}"), evil_req.url)

                    # print output informations by verbosity option
                    if self.verbose >= 1:
                        self.logC(_("|HTTP Code: {0}"), code2)

                    if self.verbose == 2:
                        self.logY(_("Source code:"))
                        self.logW(data2)

                self.attackedGET.append(url)
