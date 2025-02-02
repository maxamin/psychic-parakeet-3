#!/usr/bin/env python

from webmapCore.net.jsparser import jsparser

class lamejs(object):

    def __init__(self, data):
        self.js_vars = {}
        self.links = []
        self.debug = False
        try:
            self.js_vars = {}
            self.links = []
            rootnode = jsparser.parse(data, None, 0)
            self.read_node(rootnode)
        except Exception:
            pass

    def print_d(self, s):
        if self.debug:
            print(s)

    def getVars(self):
        return self.js_vars

    def getLinks(self):
        return self.links

    def read_node(self, node):
        if node.type == "SCRIPT":
            self.print_d("# SCRIPT")
            for sub_node in node:
                self.read_node(sub_node)
        elif node.type == "VAR":
            self.print_d("# VAR")
            self.print_d(self.read_node(node[0]))
        elif node.type == "IDENTIFIER":
            self.print_d("# IDENTIFIER")
            if hasattr(node, 'initializer'):
                value = self.read_node(node.initializer)
                self.js_vars[node.value] = value
                return node.value, value
            return node.value
        elif node.type == "NUMBER":
            self.print_d("# NUMBER")
            return node.value
        elif node.type == "STRING":
            self.print_d("# STRING")
            return node.value
        elif node.type == "PLUS":
            self.print_d("# PLUS")
            eax = None
            for sub_node in node:
                value = self.read_node(sub_node)
                if eax is None:
                    eax = value
                else:
                    if isinstance(eax, str):
                        if isinstance(value, str):
                            eax += value
                        elif isinstance(value, int):
                            eax += str(value)
                    elif isinstance(eax, int):
                        if isinstance(value, str):
                            eax = str(eax) + value
                        elif isinstance(value, int):
                            eax += value
            return eax
        elif node.type == "FUNCTION":
            self.print_d("# FUNCTION")
            try:
                func_name = node.name
            except AttributeError:
                func_name = "anonymous"
            self.print_d("In function {0}".format(func_name))
            self.read_node(node.body)
        elif node.type == "SEMICOLON":
            self.print_d("# SEMICOLON")
            self.read_node(node.expression)
            self.print_d("Semicolon end")
        elif node.type == "CALL":
            self.print_d("# CALL")
            func_name = self.read_node(node[0])
            if not func_name:
                func_name = "anonymous"
            params = self.read_node(node[1])
            self.print_d("func_name = {0}".format(func_name))
            self.print_d("params = {0}".format(params))
            if func_name == "window.open":
                if len(params):
                    self.links.append(params[0])
            elif func_name.endswith(".asyncRequest"):
                if len(params) > 1:
                    if params[0].upper() in ["GET", "POST"]:
                        self.links.append(params[1])
        elif node.type == "DOT":
            self.print_d("# DOT")
            return node.getSource()
        elif node.type == "LIST":
            self.print_d("# LIST")
            ll = []
            for sub_node in node:
                ll.append(self.read_node(sub_node))
            self.print_d("list = {0}".format(ll))
            return ll
        elif node.type == "ASSIGN":
            self.print_d("# ASSIGN")
            left_value = self.read_node(node[0])
            right_value = self.read_node(node[1])
            self.print_d("left_value = {0}".format(left_value))
            self.print_d("right_value = {0}".format(right_value))
            if (left_value.endswith(".href") or
                left_value.endswith(".action") or
                left_value.endswith(".location") or
                    left_value.endswith(".src")):
                if node[1].type == "IDENTIFIER" and right_value in self.js_vars:
                    self.links.append(self.js_vars[right_value])
                else:
                    self.links.append(right_value)
        elif node.type == "WITH":
            self.print_d("# WITH")
            for sub_node in node.body:
                self.read_node(sub_node)
        elif node.type == "PROPERTY_INIT":
            self.print_d("# PROPERTY_INIT")
            attrib_name = self.read_node(node[0])
            attrib_value = self.read_node(node[1])
            self.print_d("attrib_name = {0}".format(attrib_name))
            self.print_d("attrib_value = {0}".format(attrib_value))
            return attrib_name
        elif node.type == "OBJECT_INIT":
            self.print_d("# OBJECT_INIT")
            for sub_node in node:
                self.read_node(sub_node)
            self.print_d("OBJECT_INIT end")
        elif node == "REGEXP":
            self.print_d("# REGEXP")
            return node.value
        elif node == "THIS":
            self.print_d("# THIS")
            return "this"