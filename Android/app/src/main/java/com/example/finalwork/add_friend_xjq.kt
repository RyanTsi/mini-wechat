package com.example.finalwork

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class add_friend_xjq : AppCompatActivity() {
    var account: String? = null
    private var userList: ArrayList<User_info_xjq> = arrayListOf()
    lateinit var list: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        account = intent.getStringExtra("account")
        list = findViewById(R.id.add_friendList)
        val add = findViewById<Button>(R.id.add)
        val add_userid = findViewById<EditText>(R.id.userid)
        add.setOnClickListener {
            send_request(account.toString(), add_userid.text.toString())
            runOnUiThread {
                Toast.makeText(this, "发送好友申请成功", Toast.LENGTH_SHORT).show()
            }
        }
        refresh(account)
    }

    inner class request_adapter_xjq(activity: Activity, val id: Int, data: ArrayList<User_info_xjq>) :
        ArrayAdapter<User_info_xjq>(activity, id, data) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(context).inflate(id, parent, false)
            val item_account = view.findViewById<TextView>(R.id.userAccount)
            val item_nickname = view.findViewById<TextView>(R.id.userNickname)
            val image = view.findViewById<ImageView>(R.id.avatar)
            val acceptButton = view.findViewById<Button>(R.id.acceptButton)
            val user = getItem(position)
            if (user != null) {
                item_account.text = user.account.toString()
                item_nickname.text = user.nickname
                image.setImageResource(R.mipmap.avatar1 + user.avatar)
                acceptButton.setOnClickListener {
                    runOnUiThread {
                        del_request_friend(user.account.toString(), account.toString())
                        add_friend(user.account.toString(), account.toString())
                        add_friend(account.toString(), user.account.toString())
                        GlobalScope.launch(Dispatchers.Main) {
                            delay(500) // 延迟500毫秒
                            refresh(account)
                            Toast.makeText(this@add_friend_xjq, "添加好友成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            return view
        }
    }
    fun refresh(account: String?) {
        get_request_friend(account.toString()) { userList ->
            runOnUiThread {
                val adapter = request_adapter_xjq(this, R.layout.item_user_request, userList)
                list.adapter = adapter
            }
        }
    }
    fun get_request_friend(account: String, callback: (ArrayList<User_info_xjq>) -> Unit) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("userB", account)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "get_wait_friend_to")
            .post(requestBody)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData)
                val jsonArray = JSONArray(jsonObject.getString("data"))

                val tempList = ArrayList<String>()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getString(i)
                    tempList.add(item)
                }
                userList.clear()
                var count = 0
                for(i in 0 until tempList.size step 3) {
                    val user = User_info_xjq(tempList[i].toInt(), tempList[i + 1], tempList[i + 2].toInt())
                    if (user != null) {
                        userList.add(user)
                    }
                    count++
                    // 当所有用户信息都添加完毕时，通过回调返回用户列表
                    if (3 * count == tempList.size) {
                        callback(userList)
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
                callback(ArrayList()) // 返回一个空的用户列表
            }
        })
    }

    fun del_request_friend(userA:String, userB: String) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("userA", userA)
        jsonObject.put("userB", userB)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "delete_wait_friend")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }

    fun send_request(userA:String, userB: String) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("userA", userA)
        jsonObject.put("userB", userB)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "add_wait_friend")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }

    fun add_friend(userA:String, userB:String) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("userA", userA)
        jsonObject.put("userB", userB)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "add_friend")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }
}

