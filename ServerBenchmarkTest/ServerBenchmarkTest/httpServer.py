import http.server
from urllib.parse import urlparse
from urllib.parse import parse_qs
import os, sys
from http.server import HTTPServer, BaseHTTPRequestHandler
import socketserver
import pathlib
import mysql.connector
import traceback

class serverResponse(BaseHTTPRequestHandler):

    Error_Page = "\
        <html>\
        <body>\
        <h1>Error accessing {path}</h1>\
        <p>{msg}</p>\
        </body>\
        </html>"
    mydb = ""
    
    sql_dynamic_content = [".", "."]
        
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
        
    def do_GET(self):
        try:
            
            currWorkingDir = os.path.abspath(os.getcwd())
            #print("workingDir: ", currWorkingDir)
            #print(self.path)
            full_path = os.path.join(currWorkingDir, self.path.replace("/", "")) #.replace("/", "\\")
            #print(full_path)
            url_comp = urlparse(self.path)
            query_comp = parse_qs(urlparse(self.path).query)
            
            # It doesn't exist...
            if not os.path.exists(full_path):
                #print("received: ", self.path)
                #print("url_comp: ", url_comp)
                #print("query_comp: ", query_comp)
                if url_comp.path == '/sqlinjection.html':
                    self.handle_sqlInjection_page(query_comp)  
                else:
                    self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "value.html"), self.path)
                #raise Exception("'{0}' not found".format(self.path))
            # ...it's a file...
            elif os.path.isfile(full_path):
                if url_comp.path == '/sqlinjection.html':
                    self.handle_file(full_path, [".", "."])
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
            self.sql_dynamic_content[0] = result
            self.sql_dynamic_content[1] = "."
            for res in result:
                print("Result insert: ", res)
        else:
            sql = "select * from htmlInput where input = '" + query_comp['sql'][0] + "'"
            print("Injection String: ", sql)
            results = self.dbcursor.execute(sql, multi=True)
            result = []
            for res in results:
                print("Executing command: ", res)
                if res.with_rows:
                    result += self.dbcursor.fetchall()
            self.mydb.commit()
            self.sql_dynamic_content[0] = "."
            self.sql_dynamic_content[1] = result
            
        self.handle_file(os.path.join(os.path.abspath(os.getcwd()), "sqlinjection.html"), self.sql_dynamic_content)
        
            
    # Handle unknown objects.
    def handle_error(self, msg):
        content = self.Error_Page.format(path=self.path, msg=msg).encode("utf-8")
        self.send_content(content, 404)

    # Send actual content.
    def send_content(self, content, status=200):
        self.send_response(status)
        self.send_header("Content-type", "text/html")
        self.send_header("Content-Length", str(len(content)))
        self.end_headers()
        self.wfile.write(content)
        
    # Prepare content to be sent    
    def handle_file(self, full_path, value="."):
        try:
            with open(full_path, 'rb') as reader:
                content = reader.read()
            if not ".js" in self.path:
                content = content.decode("utf-8").format(*value)
                content = content.encode("utf-8")
            self.send_content(content)
        except IOError as msg:
            msg = "'{0}' cannot be read: {1}".format(self.path, msg)
            self.handle_error(msg)
        
if __name__ == "__main__": 
    httpd = socketserver.TCPServer(("", 8000), serverResponse)
    print("Server started!")
    print("serving at port:", 8000)
    httpd.serve_forever()
        
    print("Server ende")