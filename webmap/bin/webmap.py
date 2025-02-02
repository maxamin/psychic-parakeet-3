import sys
import argparse
import os
import urlparse
import time

BASE_DIR = None
WEBMAP_VERSION = "Webmap 1.1.0"

if hasattr(sys, "frozen"):
    # For py2exe
    CONF_DIR = os.path.join(os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding())), "data")
else:
    parent_dir = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), os.pardir))
    if os.path.exists(os.path.join(parent_dir, "webmapCore")):
        sys.path.append(parent_dir)
    CONF_DIR = os.path.dirname(os.path.join(parent_dir,'webmapCore/'))
    CONF_DIR = os.path.dirname(os.path.join(CONF_DIR, 'config'))


from webmapCore.net import HTTP, lswww
from webmapCore.file.reportgeneratorsxml import ReportGeneratorsXMLParser
from webmapCore.file.vulnerabilityxmlparser import VulnerabilityXMLParser
from webmapCore.file.anomalyxmlparser import AnomalyXMLParser
from webmapCore.net.crawlerpersister import CrawlerPersister


class InvalidOptionValue(Exception):
    def __init__(self, opt_name, opt_value):
        self.opt_name = opt_name
        self.opt_value = opt_value

    def __str__(self):
        return ("Invalid argument for option {0} : {1}").format(self.opt_name, self.opt_value)


class Webmap(object):
    """This class parse the options from the command line and set the modules and the HTTP engine accordingly.
    Launch wapiti without arguments or with the "-h" option for more informations."""

    REPORT_DIR = "report"
    REPORT_FILE = "vulnerabilities"
    HOME_DIR = os.getenv('HOME') or os.getenv('USERPROFILE')
    COPY_REPORT_DIR = os.path.join(HOME_DIR, ".webmap", "generated_report")

    def __init__(self, root_url):
        self.target_url = root_url
        self.target_scope = "folder"
        server = urlparse.urlparse(root_url).netloc
        self.http_engine = HTTP.HTTP(server)
        self.myls = lswww.lswww(root_url, http_engine=self.http_engine)

        self.report_gen = None
        self.report_generator_type = "html"
        self.xml_rep_gen_parser = ReportGeneratorsXMLParser()
        self.xml_rep_gen_parser.parse(os.path.join(CONF_DIR, "config", "reports", "generators.xml"))
        self.output_file = ""

        self.urls = {}
        self.forms = []
        self.attacks = []

        self.color = 0
        self.verbose = 0
        self.options = None

    def __initReport(self):
        for rep_gen_info in self.xml_rep_gen_parser.getReportGenerators():
            if self.report_generator_type.lower() == rep_gen_info.getKey():
                self.report_gen = rep_gen_info.createInstance()
                self.report_gen.setReportInfo(target=self.target_url,
                                             scope=self.target_scope,
                                             date_string=time.strftime("%a, %d %b %Y %H:%M:%S +0000", time.gmtime()),
                                             version=WEBMAP_VERSION)
                break

        vuln_xml_parser = VulnerabilityXMLParser()
        vuln_xml_parser.parse(os.path.join(CONF_DIR, "config", "vulnerabilities", "vulnerabilities.xml"))
        for vul in vuln_xml_parser.getVulnerabilities():
            self.report_gen.addVulnerabilityType((vul.getName()),
                                                (vul.getDescription()),
                                                (vul.getSolution()),
                                                vul.getReferences())

        anom_xml_parser = AnomalyXMLParser()
        anom_xml_parser.parse(os.path.join(CONF_DIR, "config", "vulnerabilities", "anomalies.xml"))
        for anomaly in anom_xml_parser.getAnomalies():
            self.report_gen.addAnomalyType((anomaly.getName()),
                                          (anomaly.getDescription()),
                                          (anomaly.getSolution()),
                                          anomaly.getReferences())

    def __initAttacks(self):
        self.__initReport()

        from webmapCore.attack import attack

        print(("[*] Loading modules:"))
        print(u"\t {0}".format(u", ".join(attack.modules)))
        for mod_name in attack.modules:
            mod = __import__("webmapCore.attack." + mod_name, fromlist=attack.modules)
            mod_instance = getattr(mod, mod_name)(self.http_engine, self.report_gen)
            if hasattr(mod_instance, "setTimeout"):
                mod_instance.setTimeout(self.http_engine.getTimeOut())
            self.attacks.append(mod_instance)

            self.attacks.sort(lambda a, b: a.PRIORITY - b.PRIORITY)

        for attack_module in self.attacks:
            attack_module.setVerbose(self.verbose)
            if self.color == 1:
                attack_module.setColor()

        # Custom list of modules was specified
        if self.options is not None:
            # First deactivate all modules
            for attack_module in self.attacks:
                attack_module.doGET = False
                attack_module.doPOST = False

            opts = self.options.split(",")

            for opt in opts:
                if opt.strip() == "":
                    continue

                method = ""
                if opt.find(":") > 0:
                    module, method = opt.split(":", 1)
                else:
                    module = opt

                # deactivate some module options
                if module.startswith("-"):
                    module = module[1:]
                    if module == "all":
                        for attack_module in self.attacks:
                            if attack_module.name in attack.commons:
                                if method == "get" or method == "":
                                    attack_module.doGET = False
                                if method == "post" or method == "":
                                    attack_module.doPOST = False
                    else:
                        found = False
                        for attack_module in self.attacks:
                            if attack_module.name == module:
                                found = True
                                if method == "get" or method == "":
                                    attack_module.doGET = False
                                if method == "post" or method == "":
                                    attack_module.doPOST = False
                        if not found:
                            print("[!] Unable to find a module named {0}".format(module))

                # activate some module options
                else:
                    if module.startswith("+"):
                        module = module[1:]
                    if module == "all":
                        print("[!] Keyword 'all' was removed for activation. Use 'common' and modules names instead.")
                    elif module == "common":
                        for attack_module in self.attacks:
                            if attack_module.name in attack.commons:
                                if method == "get" or method == "":
                                    attack_module.doGET = True
                                if method == "post" or method == "":
                                    attack_module.doPOST = True
                    else:
                        found = False
                        for attack_module in self.attacks:
                            if attack_module.name == module:
                                found = True
                                if method == "get" or method == "":
                                    attack_module.doGET = True
                                if method == "post" or method == "":
                                    attack_module.doPOST = True
                        if not found:
                            print("[!] Unable to find a module named {0}".format(module))

    def browse(self, crawler_file):
        """Extract hyperlinks and forms from the webpages found on the website"""
        #self.urls, self.forms = self.myls.go(crawlerFile)
        self.myls.go(crawler_file)
        self.urls = self.myls.getLinks()
        self.forms = self.myls.getForms()

    def attack(self):
        """Launch the attacks based on the preferences set by the command line"""
        if self.urls == {} and self.forms == []:
            print(("No links or forms found in this page !"))
            print(("Make sure the url is correct."))
            sys.exit(1)

        self.__initAttacks()

        for x in self.attacks:
            if x.doGET is False and x.doPOST is False:
                continue
            print('')
            if x.require:
                t = [y.name for y in self.attacks if y.name in x.require and (y.doGET or y.doPOST)]
                if x.require != t:
                    print("[!] Missing dependencies for module {0}:".format(x.name))
                    print(u"  {0}".format(",".join([y for y in x.require if y not in t])))
                    continue
                else:
                    x.loadRequire([y for y in self.attacks if y.name in x.require])

            x.logG("[+] Launching module {0}", x.name)
            x.attack(self.urls, self.forms)

        if self.myls.getUploads():
            print('')
            print("Upload scripts found:")
            print("----------------------")
            for upload_form in self.myls.getUploads():
                print(upload_form)
        if not self.output_file:
            if self.report_generator_type == "html":
                self.output_file = self.COPY_REPORT_DIR
            else:
                if self.report_generator_type == "txt":
                    self.output_file = self.REPORT_FILE + ".txt"
                else:
                    self.output_file = self.REPORT_FILE + ".xml"
        self.report_gen.generateReport(self.output_file)
        print('')
        print "Report"
        print("------")
        print("A report has been generated in the file {0}".format(self.output_file))
        if self.report_generator_type == "html":
            print("Open {0}/index.html with a browser to see this report.".format(self.output_file))
        if self.http_engine.sslErrorOccured:
            print('')
            print("Warning: Webmap came across some SSL errors during the scan, it maybe missed some webpages.")

    def setTimeOut(self, timeout=6.0):
        """Set the timeout for the time waiting for a HTTP response"""
        self.http_engine.setTimeOut(timeout)

    def setVerifySsl(self, verify=True):
        """Set whether SSL must be verified."""
        self.http_engine.setVerifySsl(verify)

    def setProxy(self, proxy=""):
        """Set a proxy to use for HTTP requests."""
        self.http_engine.setProxy(proxy)

    def addStartURL(self, url):
        """Specify an URL to start the scan with. Can be called several times."""
        self.myls.addStartURL(url)

    def addExcludedURL(self, url):
        """Specify an URL to exclude from the scan. Can be called several times."""
        self.myls.addExcludedURL(url)

    def setCookieFile(self, cookie):
        """Load session data from a cookie file"""
        self.http_engine.setCookieFile(cookie)

    def setAuthCredentials(self, auth_basic):
        """Set credentials to use if the website require an authentication."""
        self.http_engine.setAuthCredentials(auth_basic)

    def setAuthMethod(self, auth_method):
        """Set the authentication method to use."""
        self.http_engine.setAuthMethod(auth_method)

    def addBadParam(self, bad_param):
        """Exclude a parameter from an url (urls with this parameter will be
        modified. This function can be call several times"""
        self.myls.addBadParam(bad_param)

    def setNice(self, nice):
        """Define how many tuples of parameters / values must be sent for a
        given URL. Use it to prevent infinite loops."""
        self.myls.setNice(nice)

    def setMaxDepth(self, depth):
        """Set how deep the scanner should explore the website"""
        self.myls.setMaxLinkDepth(depth)

    def setScope(self, scope):
        """Set the scope of the crawler for the analysis of the web pages"""
        self.target_scope = scope
        self.myls.setScope(scope)

    def setColor(self):
        """Put colors in the console output (terminal must support colors)"""
        self.color = 1

    def verbosity(self, vb):
        """Define the level of verbosity of the output."""
        self.verbose = vb
        self.myls.verbosity(vb)

    def setModules(self, options=""):
        """Activate or deactivate (default) all attacks"""
        self.options = options

    def setReportGeneratorType(self, repGentype="xml"):
        """Set the format of the generated report. Can be xml, html of txt"""
        self.report_generator_type = repGentype

    def setOutputFile(self, outputFile):
        """Set the filename where the report will be written"""
        self.output_file = outputFile

    def addCustomHeader(self, key, value):
        self.http_engine.addCustomHeader(key, value)

if __name__ == "__main__":
    doc = "webmapDoc"

    class HelpMessage(argparse.Action):
        def __call__(self, prsr, namespace, values, option_string=None):
            print doc
            sys.exit()

    try:
        prox = ""
        auth = []
        crawlerPersister = CrawlerPersister()
        crawlerFile = None
        attackFile = None

        print("Webmap-1.0.0 (http://www.github.com/in/cehkunal/webmap)")

        # Fix bor bug #31
        if sys.getdefaultencoding() != "utf-8":
            reload(sys)
            sys.setdefaultencoding("utf-8")

        import requests
        if requests.__version__.startswith("0."):
            print("Error: You have an outdated version of python-requests. Please upgrade")
            sys.exit(1)

        parser = argparse.ArgumentParser(description="Webmap: Web application vulnerability scanner", add_help=False)
        parser.add_argument('BASE_URL')
        parser.add_argument('-s', '--start',
                            action='append', default=[],
                            help='Adds an url to start scan with',
                            metavar='URL', dest='starting_urls')
        parser.add_argument('-x', '--exclude',
                            action='append', default=[],
                            help='Adds an url to exclude from the scan',
                            metavar='URL', dest='excluded_urls')
        parser.add_argument('-p', '--proxy',
                            action='append', default=[],
                            help='Set a proxy for a given protocol',
                            metavar='PROXY_URL', dest='proxies')
        parser.add_argument('-c', '--cookie',
                            help='Set a json cookie file to use',
                            default=argparse.SUPPRESS,
                            metavar='COOKIE_FILE')
        parser.add_argument('-t', '--timeout',
                            type=float, default=6.0,
                            help='Set timeout for requests',
                            metavar='SECONDS')
        parser.add_argument('-a', '--auth',
                            dest='credentials',
                            default=argparse.SUPPRESS,
                            help='Set credentials to use',
                            metavar='CREDENTIALS')
        parser.add_argument("--auth-method",
                            default=argparse.SUPPRESS,
                            help="Set the authentication method to use",
                            choices=['basic', 'digest', 'kerberos', 'ntlm'])
        parser.add_argument('-r', '--remove',
                            action='append', default=[],
                            help='Remove this parameter from urls',
                            metavar='PARAMETER', dest='excluded_parameters')
        parser.add_argument('-H', '--header',
                            action='append', default=[],
                            help='Set a custom header to use for every requests',
                            metavar='HEADER', dest='headers')
        parser.add_argument('-U', '--user-agent',
                            default=argparse.SUPPRESS,
                            help='Set a custom user-agent to use for every requests',
                            metavar='AGENT', dest='user_agent')
        parser.add_argument('-n', '--nice',
                            dest='repeat_limit', default=0,
                            help='Max occurences of parameter',
                            metavar='COUNT', type=int)
        parser.add_argument("-d", "--depth",
                            help="Set how deep the scanner should explore the website",
                            type=int, default=40)
        parser.add_argument('-m', '--module',
                            dest='modules', default=None,
                            help='List of modules to load',
                            metavar='MODULES_LIST')
        parser.add_argument('-u', '--color',
                            action='store_true',
                            help='Highlight output')
        parser.add_argument('-v', '--verbose',
                            metavar='LEVEL', dest='verbosity',
                            help='Set verbosity level', default=0,
                            type=int, choices=xrange(0,3))
        parser.add_argument('-b', '--scope',
                            help='Set scan scope', default="folder",
                            choices=['page', 'folder', 'domain', 'url'])
        parser.add_argument('-f', '--format',
                            help='Set output format', default="html",
                            choices=['json', 'html', 'txt', 'openvas', 'vulneranet', 'xml'])
        parser.add_argument('-o', '--output',
                            metavar='OUPUT_PATH', default=argparse.SUPPRESS,
                            help='Output file or folder')
        parser.add_argument('-i', '--continue', dest="resume",
                            nargs='?', metavar='FILE', default=argparse.SUPPRESS,
                            help='File to import')
        parser.add_argument('-k', '--attack', default=argparse.SUPPRESS,
                            nargs='?', metavar='FILE',
                            help='File to import')
        parser.add_argument('--verify-ssl',
                            default=1, dest="check_ssl",
                            help='Set SSL check',
                            type=int, choices=[0, 1])
        parser.add_argument('--version', action='version', version=WEBMAP_VERSION)
        parser.add_argument('-h', '--help', action=HelpMessage, nargs=0, help="Show detailed usage")
        args = parser.parse_args()

        if not os.path.isdir(crawlerPersister.CRAWLER_DATA_DIR):
            os.makedirs(crawlerPersister.CRAWLER_DATA_DIR)

        url = args.BASE_URL
        wap = Webmap(url)

        try:
            for start_url in args.starting_urls:
                if start_url.startswith("http://") or start_url.startswith("https://"):
                    wap.addStartURL(start_url)
                elif os.path.isfile(start_url):
                    import codecs
                    try:
                        urlfd = codecs.open(start_url, encoding="UTF-8")
                        for urlline in urlfd:
                            urlline = urlline.strip()
                            if urlline.startswith("http://") or urlline.startswith("https://"):
                                wap.addStartURL(urlline)
                        urlfd.close()
                    except UnicodeDecodeError:
                        print(("Error: File given with the -s option must be UTF-8 encoded !"))
                        raise InvalidOptionValue("-s", start_url)
                else:
                    raise InvalidOptionValue('-s', start_url)

            for exclude_url in args.excluded_urls:
                if exclude_url.startswith("http://") or exclude_url.startswith("https://"):
                    wap.addExcludedURL(exclude_url)
                else:
                    raise InvalidOptionValue("-x", exclude_url)

            for proxy_url in args.proxies:
                if proxy_url.startswith("http://") or proxy_url.startswith("https://"):
                    wap.setProxy(proxy_url)
                else:
                    raise InvalidOptionValue("-p", proxy_url)

            if "cookie" in args:
                if os.path.isfile(args.cookie):
                    wap.setCookieFile(args.cookie)
                else:
                    raise InvalidOptionValue("-c", args.cookie)

            if "credentials" in args:
                if "%" in args.credentials:
                    wap.setAuthCredentials(args.credentials.split("%", 1))
                else:
                    raise InvalidOptionValue("-a", args.credentials)

            if "auth_method" in args:
                wap.setAuthMethod(args.auth_method)

            for bad_param in args.excluded_parameters:
                wap.addBadParam(bad_param)

            wap.setNice(args.repeat_limit)
            wap.setMaxDepth(args.depth)
            # should be a setter
            wap.verbosity(args.verbosity)
            if args.color:
                wap.setColor()
            wap.setTimeOut(args.timeout)
            wap.setModules(args.modules)

            if "user_agent" in args:
                wap.addCustomHeader("user-agent", args.user_agent)

            for custom_header in args.headers:
                if ":" in custom_header:
                    hdr_name, hdr_value = custom_header.split(":", 1)
                    wap.addCustomHeader(hdr_name.strip(), hdr_value.strip())

            if "output" in args:
                wap.setOutputFile(args.output)

            found_generator = False
            for repGenInfo in wap.xml_rep_gen_parser.getReportGenerators():
                if args.format == repGenInfo.getKey():
                    wap.setReportGeneratorType(args.format)
                    found_generator = True
                    break
            if not found_generator:
                raise InvalidOptionValue("-f", args.format)

            wap.setScope(args.scope)
            wap.setVerifySsl(args.check_ssl)

            hostname = url.split("://")[1].split("/")[0]

            if "attack" in args:
                # Option was used
                if not args.attack:
                    # no value set, use default
                    attackFile = u"{0}{1}{2}.xml".format(crawlerPersister.CRAWLER_DATA_DIR,
                                                         os.path.sep,
                                                         hostname)
                else:
                    crawlerFile = args.attack

            if "resume" in args:
                if not args.resume:
                    crawlerFile = u"{0}{1}{2}.xml".format(crawlerPersister.CRAWLER_DATA_DIR,
                                                          os.path.sep,
                                                          hostname)
                else:
                    crawlerFile = args.resume

        except InvalidOptionValue, msg:
            print(msg)
            sys.exit(2)


        if attackFile is not None:
            if crawlerPersister.isDataForUrl(attackFile) == 1:
                crawlerPersister.loadXML(attackFile)
                wap.urls = crawlerPersister.getLinks()
                wap.forms = crawlerPersister.getForms()
                print ("File {0} loaded. Wapiti will use it to perform the attack".format(attackFile))
            else:
                print("File {0} not found. Wapiti will scan the web site again".format(attackFile))
                wap.browse(crawlerFile)
        else:
            wap.browse(crawlerFile)
        try:
            wap.attack()
        except KeyboardInterrupt:
            print('')
            print(("Attack process interrupted. To perform again the attack, "
                    "lauch Webmap with \"-i\" or \"-k\" parameter."))
            print('')
            pass
    except SystemExit:
        pass