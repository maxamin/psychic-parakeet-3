#!/usr/bin/env python

import os
from webmapCore.report.jsonreportgenerator import JSONReportGenerator
from shutil import copytree, rmtree
import sys

class HTMLReportGenerator(JSONReportGenerator):
    """
    This class generates a Webmap scan report in HTML format.
    It first generates a JSON report and insert in the HTML template.
    For more information see JSONReportGenerator class
    Then it copies the template structure (which js and css files) in the output directory.
    """
    if hasattr(sys, "frozen"):
        BASE_DIR = os.path.join(os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding())), "data")
    else:
        BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), os.pardir))
    REPORT_DIR = "report_template"
    REPORT_JSON_FILE = "vulnerabilities.json"

    def generateReport(self, filename):
        """
        Copy the report structure in the specified 'fileName' directory
        If these path exists, it will be overwritten
        """
        if os.path.exists(filename):
            rmtree(filename)
        copytree(os.path.join(self.BASE_DIR, self.REPORT_DIR), filename)

        JSONReportGenerator.generateReport(self, os.path.join(filename, self.REPORT_JSON_FILE))
        fd = open(os.path.join(filename, self.REPORT_JSON_FILE))
        json_data = fd.read()
        json_data = json_data.replace('</', r'<\/')
        fd.close()

        fd = open(os.path.join(filename, "index.html"), "r+")
        html_data = fd.read()
        html_data = html_data.replace('__JSON_DATA__', json_data)
        fd.seek(0)
        fd.truncate(0)
        fd.write(html_data)
        fd.close()

if __name__ == "__main__":

    SQL_INJECTION = "Sql Injection"
    FILE_HANDLING = "File Handling"
    XSS = "Cross Site Scripting"
    CRLF = "CRLF Injection"
    EXEC = "Commands execution"
