#!/usr/bin/env python

class AuxText(object):
    """Class for reading and writing in text files"""
    @staticmethod
    def readLines(fileName):
        """returns a array"""
        lines = []
        try:
            # Reminder : don't try to read payload files as UTF-8, must give str type
            f = open(fileName)
            for line in f:
                clean_line = line.strip(" \n")
                clean_line = clean_line.replace("[TAB]", "\t")
                clean_line = clean_line.replace("[LF]", "\n")
                if clean_line != "":
                    lines.append(clean_line.replace("\\0", "\0"))
        except IOError, e:
            print(e)
        #finally clause do not work with jyton
        #finally:
            #if f!=None:
                #f.close()
        return lines
#class

if __name__ == "__main__":
    try:
        l = AuxText()
        ll = l.readLines("./config/execPayloads.txt")
        for li in ll:
            print(li)
    except SystemExit:
        pass