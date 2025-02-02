#!/usr/bin/python
# -*- coding: utf-8 -*-

class XText(object):
    """Reading and writing file """
    @staticmethod
    def readLines(fileName):
        """return a array"""
        lines = []
        try:
            f = open(fileName)
            for line in f:
                clean_line = line.strip(" \n")
                clean_line = clean_line.replace("[TAB]", "\t")
                clean_line = clean_line.replace("[LF]", "\n")
                if clean_line != "":
                    lines.append(clean_line.replace("\\0", "\0"))
        except IOError, e:
            print(e)
        return lines

if __name__ == '__main__':
    try:
        x = XText()
        xx = x.readLines("./config/exPayloads.txt")
        for xa in xx:
            print(xa)
    except SystemExit:
        pass