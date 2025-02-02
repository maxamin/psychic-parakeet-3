import http.server
from urllib.parse import urlparse
from urllib.parse import parse_qs
import os, sys
from http.server import HTTPServer, BaseHTTPRequestHandler
import socketserver
import pathlib
import mysql.connector
import traceback
import subprocess

class serverResponse(BaseHTTPRequestHandler):

    ########################################################################################################################################
    #Variables
    
    Error_Page = "\
        <html>\
        <body>\
        <h1>Error accessing {path}</h1>\
        <p>{msg}</p>\
        </body>\
        </html>"
    mydb = ""
    
    sql_dynamic_content = [" .34.s.2 ", " .34.s.2 "]
    currWorkingDir = os.path.abspath(os.getcwd())
    
    ########################################################################################################################################
    #Constructor
        
    try:
        mydb = mysql.connector.connect(host="localhost", user="httpUser", password="simpleP4ssPhrase4Testing", database="htmlDB")
        print("DB htmlDB connected")
    except Exception as excep:
        mydb = mysql.connector.connect(host="localhost", user="httpUser", password="simpleP4ssPhrase4Testing")
        dbcursor = mydb.cursor()
        dbcursor.execute("CREATE DATABASE htmlDB")
        print("DB htmlDB created")
        
    dbcursor = mydb.cursor(buffered=True)
    try:
        dbcursor.execute("CREATE TABLE htmlInput (id INT AUTO_INCREMENT PRIMARY KEY, input TEXT)")
        print("Table htmlInput created")
    except mysql.connector.errors.ProgrammingError as pe:
        print("Table probably already existed")
    except Exception as excep:
        traceback.print_exc()
        
    ########################################################################################################################################
    #Functions
        
    def do_OPTIONS(self):           
        self.send_response(200, "ok")       
        self.send_header('Access-Control-Allow-Origin', '*')                
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header("Access-Control-Allow-Headers", "X-Requested-With")
        
        
    def do_GET(self):
        try:
            
            #print("workingDir: ", currWorkingDir)
            #print(self.path)
            full_path = os.path.join(self.currWorkingDir, self.path.replace("/", "")) #.replace("/", "\\")
            #print(full_path)
            url_comp = urlparse(self.path)
            query_comp = parse_qs(urlparse(self.path).query)
            
            # It doesn't exist...
            if not os.path.exists(full_path):
                #print("received: ", self.path)
                #print("url_comp: ", url_comp)
                #print("query_comp: ", query_comp)
                
                #Check if its input from sqlinjection.html odr fileInclusion.html
                if url_comp.path == '/sqlinjection.html':
                    self.handle_sqlInjection_page(query_comp)
                elif url_comp.path == '/fileinclusion.html':
                    self.handle_fileInclusion_page(query_comp)
                elif url_comp.path == '/cmdinjection.html':
                    self.handle_cmdInjection_page(query_comp)
                elif url_comp.path == '/phpinjection.html':
                    self.handle_phpInjection_page(query_comp)
                elif url_comp.path == '/xss.html':
                    self.handle_xss_page(query_comp)
                else:
                    #self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "value.html"), self.path)
                    raise Exception("Path " + full_path + " not found")
            # ...it's a file...
            elif os.path.isfile(full_path):
                if url_comp.path == '/sqlinjection.html':
                    self.handle_file(full_path, self.sql_dynamic_content)
                else:
                    self.handle_file(full_path)
            # ...it's something we don't handle.
            else:
                if self.path == '/':
                    self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "index.html"), self.path)
                else:
                    raise Exception("Unknown object: "+ full_path)

        # Handle errors.
        except Exception as msg:
            print("Error occured: \n", msg)
            traceback.print_exc()
            self.handle_error(msg)

    #Handle the input from page xss.html
    #writes back the input from form 
    def handle_xss_page(self, query_comp):
        output = [query_comp["xss"][0]]
        print("Output: ", output)
        self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "xss.html"), output )

    #Handle the input from page phpinjection.html
    #Directly executes phpi.php with the given input as parameter
    def handle_phpInjection_page(self, query_comp):
        #command = "php -f " + os.path.join(self.currWorkingDir, "phpi.php") + " phpi=\"" + query_comp["phpi"][0] +"\""
        #command = command.replace("\\", "\\\\")
        command = "php -f phpi.php phpi=\"" + query_comp["phpi"][0] +"\""
        print("Command: ", command)
        res = subprocess.run(command, shell=True, stdout=subprocess.PIPE)
        output = ["PHP executed: \n"+res.stdout.decode("utf-8")]
        print("Output: ", output)
        self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "phpinjection.html"), output )
            
    #Handle the input from page cmdinjection.html
    #Directly executes the code given by the input in shell
    def handle_cmdInjection_page(self, query_comp):
        command = subprocess.run(query_comp["cmdi"][0], shell=True, capture_output=True)
        output = ["Command executed: "+command.stdout.decode("utf-8")]
        print("Output: ", output)
        self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "cmdinjection.html"), output )
    
    #Handle the input from page fileInclusion.html
    #Enables Directory Traversal and Remote and Local file inclusion by executing the given file
    def handle_fileInclusion_page(self, query_comp):
        filePath = query_comp['fi'][0]
        res = subprocess.run(filePath, shell=True, stdout=subprocess.PIPE)
        output = ["File executed: "+filePath+ "; Result: "+ res.stdout.decode("utf8")]
        self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "fileinclusion.html"), output )

    # Handle the input from page sqlinjection.html
    # Exploit input: '; drop table htmlInput; select * from htmlInput where input = '
    def handle_sqlInjection_page (self, query_comp):
        if 'sql_insert' in query_comp:
            sql = "INSERT INTO htmlInput (input) VALUES (%s)"
            val = (query_comp['sql_insert'][0],)
            self.dbcursor.execute(sql, val)
            self.mydb.commit()
            self.dbcursor.execute("SELECT * FROM htmlInput")
            result = self.dbcursor.fetchall()
            self.sql_dynamic_content[1] = "."
            self.sql_dynamic_content[0] = result
            for res in result:
                print("Result insert: ", res)
        else:
            sql = "select * from htmlInput where input = '" + query_comp['sql'][0] + "'"
            print("Injection String: ", sql)
            results = self.dbcursor.execute(sql, multi=True)
            result = []
            for res in results:
                print("Executed command: ", res)
                if res.with_rows:
                    result += self.dbcursor.fetchall()
            self.mydb.commit()
            self.sql_dynamic_content[0] = "."
            self.sql_dynamic_content[1] = result
            print("Result of execution:", result)
            
        self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "sqlinjection.html"), self.sql_dynamic_content)
   
 
    # Handle unknown objects.
    def handle_error(self, msg):
        content = self.Error_Page.format(path=self.path, msg=msg).encode("utf-8")
        self.send_content(content, 404)

    # Send actual content.
    def send_content(self, content, status=200):
        self.send_response(status)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header("Content-type", "text/html")
        self.send_header("Content-Length", str(len(content)))
        self.end_headers()
        self.wfile.write(content)

    # Prepare content to be sent    
    def handle_file(self, full_path, value=[" .34.s.2 "]):
        try:
            with open(full_path, 'rb') as reader:
                content = reader.read()
            if not ".js" in self.path:
                content = content.decode("utf-8").format(*value)
                content = content.replace(".34.s.2", "")
                content = content.encode("utf-8")
            self.send_content(content)
        except IOError as msg:
            msg = "'{0}' cannot be read: {1}".format(self.path, msg)
            self.handle_error(msg)
            
########################################################################################################################################
#Starter

if __name__ == "__main__": 
    httpd = socketserver.TCPServer(("", 8000), serverResponse)
    print("Server started!")
    print("serving at port:", 8000)
    httpd.serve_forever()
        
    print("Server ende")