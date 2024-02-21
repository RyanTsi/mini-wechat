package com.example.finalwork

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Login_xjq : AppCompatActivity() {
    lateinit var account_E: EditText
    lateinit var password_E: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val register = findViewById<Button>(R.id.register)
        val login = findViewById<Button>(R.id.login)
        account_E = findViewById(R.id.account)
        password_E = findViewById(R.id.password)
        val avatar = findViewById<ImageView>(R.id.avatar)
        avatar.setImageResource(R.mipmap.avatar1)
        register.setOnClickListener {
            intent = Intent(this, Register_xjq::class.java)
            startActivityForResult(intent, 1)
        }
        login.setOnClickListener {
            val account = account_E.text.toString()
            val password = password_E.text.toString()
            CoroutineScope(Dispatchers.IO).launch {
                val check_password = get_password(account)
                println(check_password)
                withContext(Dispatchers.Main) {
                    if (check_password == null || password != check_password) {
                        Toast.makeText(this@Login_xjq, "用户名或密码有误", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this@Login_xjq, MainActivity_xjq::class.java)
                        intent.putExtra("account", account)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val account = data?.getStringExtra("account")
                println(account)
                runOnUiThread {
                    account_E.setText(account)
                }
            } else {
                // 处理取消或其他结果
            }
        }
    }

    suspend fun get_password(account: String): String? = suspendCoroutine { continuation ->
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("account", account)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "get_user")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData)
                val password = jsonObject.getString("password")
                continuation.resume(password)
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                continuation.resume(null)
            }
        })
    }
}