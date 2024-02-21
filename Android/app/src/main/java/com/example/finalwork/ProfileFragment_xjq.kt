package com.example.finalwork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class ProfileFragment_xjq :Fragment() {
    lateinit var account_T: TextView
    lateinit var nickname_E: EditText
    lateinit var avatar_V: ImageView
    lateinit var update:Button
    var account:String? = null
    var nickname:String? = null
    var avatar:Int? = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        update = view.findViewById(R.id.update)
        account_T = view.findViewById(R.id.account)
        nickname_E = view.findViewById(R.id.nickname)
        avatar_V = view.findViewById(R.id.avatar)
        return view
    }

    override fun onResume() {
        super.onResume()
        val bundle = arguments
        account = bundle?.getString("account")
        get_user_info(account)
        refresh()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        account_T.setText(account)
        nickname_E.setText(nickname)
        update.setOnClickListener {
            update_user_info(account, nickname_E.text.toString(), 0)
            // 处理按钮点击事件
        }
        refresh()
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
                nickname = jsonObject.getString("nickname")
                avatar = jsonObject.getString("avatar").toInt()

                // 在获取到用户信息后调用 refresh() 方法更新界面
                requireActivity().runOnUiThread {
                    refresh()
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }
    fun update_user_info(account:String?, nickname: String, avatar:Int) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("account", account)
        jsonObject.put("nickname", nickname)
        jsonObject.put("avatar", avatar.toString())
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "update_user_info")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "修改成功", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
            }
        })
    }
    fun refresh() {
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            account_T.setText(account)
            nickname_E.setText(nickname)
            avatar_V.setImageResource(R.mipmap.avatar1 + avatar!!)
        }
    }
}