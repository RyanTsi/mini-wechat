from flask import Flask, request, jsonify
from database import DataBase
import pymysql
import json
app = Flask(__name__)


# 错误处理装饰器
@app.errorhandler(ValueError)
def handle_value_error(error):
    response = jsonify({'error': str(error)})
    response.status_code = 400
    return response

# user表
@app.route('/add_user', methods=['POST'])
def add_user():
    db = DataBase()
    data = request.get_json()
    password = data['password']
    query = f"INSERT INTO user (password) VALUES ('{password}')"
    account = db.exec(query, 'failed', f'{password} inserted').lastrowid
    return jsonify({'account': f'{account}'})

@app.route('/delete_user', methods=['POST'])
def delete_user():
    db = DataBase()
    data = request.get_json()
    account = data['account']
    query = f"DELETE FROM user WHERE account='{account}'"
    db.exec(query, 'failed', f'{account} deleted')
    return jsonify({'message': 'User deleted successfully'})

@app.route('/update_user', methods=['POST'])
def update_user():
    db = DataBase()
    data = request.get_json()
    account = data['account']
    password = data['password']
    query = f"UPDATE user SET password = {password} WHERE account = {account}"
    db.exec(query, 'failed', f'{account} updated')
    return jsonify({'message': 'User updated successfully'})

@app.route('/get_user', methods=['POST'])
def get_user():
    db = DataBase()
    data = request.get_json()
    account = data['account']
    query = f'SELECT * FROM user WHERE account = {account}'
    lines = db.exec(query)
    all = []
    for line in lines:
        all.append({
            'account': line[0],
            'password': line[1]
        })
    if len(all) > 0:
        user = all[0]
        print(user)
        return jsonify(user)
    else:
        return jsonify({'message': 'User not found'})

# user_info表
@app.route('/add_user_info', methods=['POST'])
def add_user_info():
    db = DataBase()
    data = request.get_json()
    account = data['account']
    nickname = data['nickname']
    avatar = 0
    query = f"INSERT INTO user_info (account, nickname, avatar) VALUES ('{account}', '{nickname}', '{avatar}')"
    db.exec(query, 'failed', f'{account}, {nickname}, {avatar} inserted')
    return jsonify({'message': 'User_info added successfully'})

@app.route('/update_user_info', methods=['POST'])
def update_user_info():
    db = DataBase()
    data = request.get_json()
    account = data['account']
    nickname = data['nickname']
    avatar = data['avatar']
    query = f"UPDATE user_info SET nickname = '{nickname}', avatar = {avatar} WHERE account = {account}"
    db.exec(query, 'failed', f'{account}, {nickname}, {avatar} updated')
    return jsonify({'message': 'User_info updated successfully'})


@app.route('/get_user_info', methods=['POST'])
def get_user_info():
    db = DataBase()
    data = request.get_json()
    account = data['account']
    query = f'SELECT * FROM user_info WHERE account = {account}'
    lines = db.exec(query)
    all = []
    for line in lines:
        all.append({
            'account': line[0],
            'nickname': line[1],
            'avatar': line[2],
        })
    if len(all) > 0:
        user_info = all[0]
        print(user_info)
        return jsonify(user_info)
    else:
        return jsonify({'message': 'User not found'})

# message 表
@app.route('/add_message', methods=['POST'])
def add_msg():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    userB = data['userB']
    msg = data['msg']
    query = f"INSERT INTO message (from_user, to_user, msg, send_time) VALUES ('{userA}', '{userB}', '{msg}', NOW())"
    db.exec(query, 'failed', f'{userA} {userB} {msg} inserted')
    return jsonify({'message': 'message added successfully'})

@app.route('/get_message', methods=['POST'])
def get_msg():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    userB = data['userB']
    query = f"SELECT * FROM message WHERE (from_user = '{userA}' AND to_user = '{userB}') OR (from_user = '{userB}' AND to_user = '{userA}')"
    lines = db.exec(query)
    all = []
    for line in lines:
        all.append({
            'from':line[0],
            'to':line[1],
            'msg':line[2],
            'send_time':line[3]
        })
    print(all)
    return jsonify({'data': all})

# wait friend 类
@app.route('/add_wait_friend', methods=['POST'])
def add_wait_friend():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    userB = data['userB']
    query = f"INSERT INTO wait_respond_friend (from_user, to_user) VALUES ('{userA}', '{userB}')"
    db.exec(query, 'failed', f'{userA} {userB} inserted')
    return jsonify({'message': 'wait_respond_friend added successfully'})

@app.route('/delete_wait_friend', methods=['POST'])
def delete_wait_friend():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    userB = data['userB']
    query = f"DELETE FROM wait_respond_friend WHERE from_user='{userA}' AND to_user='{userB}'"
    db.exec(query, 'failed', f'{userA} {userB} deleted')
    return jsonify({'message': 'wait friend deleted successfully'})

@app.route('/get_wait_friend_from', methods=['POST'])
def get_wait_friend_from():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    query = f"SELECT * FROM wait_respond_friend WHERE from_user='{userA}'"
    lines = db.exec(query)
    all = []
    for line in lines:
        all.append(line[1])
    print(all)
    return jsonify({'data': all})

@app.route('/get_wait_friend_to', methods=['POST'])
def get_wait_friend_to():
    db = DataBase()
    data = request.get_json()
    userB = data['userB']
    query = f"SELECT wrf.from_user, ui.nickname, ui.avatar FROM wait_respond_friend wrf JOIN user_info ui ON wrf.from_user = ui.account WHERE to_user='{userB}'"
    lines = db.exec(query)
    all = []
    for line in lines:
        all.append(line[0])
        all.append(line[1])
        all.append(line[2])
    print(all)
    return jsonify({'data': all})


# friend 类
@app.route('/add_friend', methods=['POST'])
def add_friend():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    userB = data['userB']
    query = f"INSERT INTO friend (from_user, to_user) VALUES ('{userA}', '{userB}')"
    db.exec(query, 'failed', f'{userA} {userB} inserted')
    return jsonify({'message': 'friend added successfully'})

@app.route('/delete_friend', methods=['POST'])
def delete_friend():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    userB = data['userB']
    query = f"DELETE FROM friend WHERE from_user='{userA}' AND to_user='{userB}'"
    db.exec(query, 'failed', f'{userA} {userB} deleted')
    return jsonify({'message': 'friend deleted successfully'})

@app.route('/get_friend', methods=['POST'])
def get_friend():
    db = DataBase()
    data = request.get_json()
    userA = data['userA']
    query = f"SELECT f.to_user, ui.nickname, ui.avatar FROM friend f JOIN user_info ui ON f.to_user = ui.account WHERE from_user='{userA}'"
    lines = db.exec(query)
    all = []
    for line in lines:
        all.append(line[0])
        all.append(line[1])
        all.append(line[2])
    print(all)
    return jsonify({'data': all})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)