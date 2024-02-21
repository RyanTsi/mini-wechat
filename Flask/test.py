import requests
import json

data = {
    'account' : '10004',
    'nickname' : 'Ryan',
    'avatar': '0',
    'password': '000100',
    'userA' : '10003',
    'userB' : '10004',
    'msg': '你好！'
}

response = requests.post('http://47.115.207.251:5000/add_user', json=data)

