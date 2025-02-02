import requests

r = requests.get(url = "http://localhost:8000", data = {'address':"Hallo"})
print("Result: ",r)
print("Body: ", r.content)
print("-------------------------")
print("Body(Text): ", r.text)