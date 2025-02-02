import sqlite3

conn = sqlite3.connect("vulnmanager.db")
cursor = conn.cursor()
cursor.execute('''
    CREATE TABLE IF NOT EXISTS users (
    site TEXT NOT NULL,
    vuln TEXT NOT NULL,
    local TEXT NOT NULL,
    description TEXT NOT NULL
);
''')
def show(site):
    cursor.execute(f'''
        SELECT vuln, local, description FROM users
        WHERE site = '{site}'
    ''')
    if cursor.rowcount == 0:
        print("Site was not found.")
    else:
        for user in cursor.fetchall():
            print("Web Site:   ",site)
            print("Vuln:       ",user[0])
            print("URL:        ",user[1])
            print("Description:",user[2])
            print("\n")

def new_vuln(site,vuln,local,description):
    cursor.execute(f'''
            INSERT INTO users (site, vuln, local, description) 
            VALUES ('{site}', '{vuln}', '{local}', '{description}')
        ''')
    conn.commit()
def show_all():
    cursor.execute('''
            SELECT site,vuln FROM users; 
        ''')
    for site in cursor.fetchall():
        print(" ",site[0]," ",site[1])

def main():
    def help():
        print("""
help       Help Menu.
show       All about a web site.
show all   Show all web sites registered.
new        Input a new vuln.
exit       Exit.
        """)
    print("""
---Vulnerability Manager---
    For Web Application
 Type "help" for help menu
    """)

    while True:
        co = ["show","show all","help","new","exit"]
        op = input("> ")
        if op not in co:
            print("Command not found.")
        if op == "help":
            help()
        if op == "exit":
            break
        if op == "new":
            print("\nWeb Site.")
            site = input("-> ")
            print("Vulnerability.")
            vuln = input("-> ")
            print("URL where the flaw was found.")
            local = input("-> ")
            print("A short description.")
            description = input("-> ")
            new_vuln(site, vuln, local, description)
            print("Finished...")
        if op == "show all":
            print("\n")
            show_all()
            print("\n")
        if op == "show":
            print("Web Site.")
            site = input("-> ")
            print("\n")
            show(site)


try:
    main()
except:
    print("Quitting...")
print(" ")
conn.close()