package com.example.finalwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import org.json.JSONObject
import java.io.IOException

class MainActivity_xjq : AppCompatActivity() {

    private val messageFragmentXjq = MessageFragment_xjq()
    private val friendFragmentXjq = FriendFragment_xjq()
    private val profileFragmentXjq = ProfileFragment_xjq()
    private var account: String? = null
    private var avatar: Int = 0
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_friends -> {
                    replaceFragment(friendFragmentXjq)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.menu_profile -> {
                    replaceFragment(profileFragmentXjq)
                    return@OnNavigationItemSelectedListener true
                }
                else -> return@OnNavigationItemSelectedListener false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch(Dispatchers.Main) {
            account = intent.getStringExtra("account")
            get_user_info(account.toString())
            delay(100)
            val bundle = Bundle()
            bundle.putString("account", account)
            bundle.putInt("avatar", avatar)
            profileFragmentXjq.arguments = bundle
            friendFragmentXjq.arguments = bundle
            messageFragmentXjq.arguments = bundle
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

            // 默认显示消息列表片段
            replaceFragment(friendFragmentXjq)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    fun get_user_info(account: String?) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("account", account)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "get_user_info")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData)
                avatar = jsonObject.getString("avatar").toInt()
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }
}