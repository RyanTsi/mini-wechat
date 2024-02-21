package com.example.finalwork

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class Register_xjq : AppCompatActivity() {
    lateinit var account:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val nickname_E = findViewById<EditText>(R.id.nickname)
        val password_E = findViewById<EditText>(R.id.password)
        val confirm_password_E = findViewById<EditText>(R.id.confirm_password)
        val register = findViewById<Button>(R.id.register)
        register.setOnClickListener {
            val nickname = nickname_E.text.toString()
            val password = password_E.text.toString()
            val confirm_password = confirm_password_E.text.toString()
            if(!password.equals(confirm_password)) {
                Toast.makeText(this, "请确认两次密码是否一致",Toast.LENGTH_SHORT).show()
            } else {
                add_uer(password, nickname)
            }
        }
    }
    fun add_uer(password: String, nickname: String) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("password", password)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "add_user")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData)
                account = jsonObject.getString("account")
                add_user_info(account, nickname)
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }
    fun add_user_info(account: String, nickname: String) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("account", account)
        jsonObject.put("nickname", nickname)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "add_user_info")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
                val intent = Intent()
                intent.putExtra("account", account)
                setResult(Activity.RESULT_OK, intent)
                runOnUiThread {
                    Toast.makeText(this@Register_xjq, "注册成功", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }
}