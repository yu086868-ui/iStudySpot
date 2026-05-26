import requests
import json

url = "http://localhost:8080/api/card/generate"
data = {
    "userID": "123456",
    "studyDuration": 120
}

try:
    response = requests.post(url, json=data)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text}")
except Exception as e:
    print(f"Error: {e}")
