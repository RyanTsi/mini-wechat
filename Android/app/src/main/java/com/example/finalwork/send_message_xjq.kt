package com.example.finalwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.time.format.DateTimeFormatter

class message (val content: String, val type: Int) {
    companion object {
        const val TYPE_RECEIVED = 0
        const val TYPE_SENT = 1
    }
}


class send_message_xjq : AppCompatActivity() {
    private var my_id:String? = null
    private var your_id:String? = null
    private var my_avatar:Int? = 0
    private var your_avatar:Int? = 0
    lateinit var yourNickname:TextView
    private var msgList = ArrayList<message> ()
    lateinit var recyclerView:RecyclerView
    lateinit var inputText: EditText
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)
        my_id = intent.getStringExtra("my_id")
        your_id = intent.getStringExtra("your_id")
        my_avatar = intent.getIntExtra("my_avatar", 0)
        your_avatar = intent.getIntExtra("your_avatar", 0)
        val your_nickname = intent.getStringExtra("your_nickname")
        yourNickname = findViewById(R.id.yourNickname)
        yourNickname.text = your_nickname
        recyclerView = findViewById(R.id.recyclerView)
        inputText = findViewById(R.id.inputText)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val send = findViewById<Button>(R.id.send)
        send.setOnClickListener {
            val content = inputText.text.toString()
            if (content.isNotEmpty()) {
                add_message(my_id.toString(), your_id.toString(), content)
                refresh(my_id.toString(), your_id.toString())
                inputText.setText("") // 清空输入框中的内容
            }
        }

    }
    inner class MsgAdapterxjq(val msglist: ArrayList<message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        inner class LeftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val leftMsg: TextView = view.findViewById(R.id.leftMsg)
            val avatar:ImageView = view.findViewById(R.id.avatar)
        }
        inner class RightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val rightMsg: TextView = view.findViewById(R.id.rightMsg)
            val avatar:ImageView = view.findViewById(R.id.avatar)
        }
        override fun getItemViewType(position: Int): Int {
            val msg = msglist[position]
            return msg.type
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder = if (viewType ==
            message.TYPE_RECEIVED) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_left_item,
                parent, false)
            LeftViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_right_item,
                parent, false)
            RightViewHolder(view)
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = msglist[position]
            when (holder) {
                is LeftViewHolder -> {
                    holder.leftMsg.text = msg.content
                    holder.avatar.setImageResource(R.mipmap.avatar1 + (your_avatar?.toInt() ?: 0))
                }
                is RightViewHolder -> {
                    holder.rightMsg.text = msg.content
                    holder.avatar.setImageResource(R.mipmap.avatar1 + (my_avatar?.toInt() ?: 0))
                }
                else -> throw IllegalArgumentException()
            }
        }
        override fun getItemCount() = msglist.size
    }
    private var isPolling = false
    private val pollingInterval = 500L // 0.5秒

    override fun onResume() {
        super.onResume()
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    private fun startPolling() {
        if (isPolling) return // 如果已经在轮询中，则无需再次启动

        isPolling = true
        lifecycleScope.launch(Dispatchers.Main) {
            while (isPolling) {
                refresh(my_id.toString(), your_id.toString())
                delay(pollingInterval)
            }
        }
    }

    private fun stopPolling() {
        isPolling = false
    }


    private fun refresh(userA: String, userB:String) {
        get_message(userA, userB) {msgList ->
            runOnUiThread {
                val adapter = MsgAdapterxjq(msgList)
                recyclerView.adapter = adapter
                recyclerView.scrollToPosition(msgList.size - 1) // 将RecyclerView定位到最后一行
            }
        }
    }
    fun get_message(userA:String, userB:String, callback: (ArrayList<message>) -> Unit) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("userA", userA)
        jsonObject.put("userB", userB)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "get_message")
            .post(requestBody)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData)
                val jsonArray = JSONArray(jsonObject.getString("data"))
                msgList.clear()
                var count = 0
                for (i in 0 until jsonArray.length()) {
                    val temp = jsonArray.getJSONObject(i)
                    val from = temp.getString("from")
                    val to = temp.getString("to")
                    val msg = temp.getString("msg")
                    if(from.equals(userB)) {
                        msgList.add(
                            message(
                                msg,
                                message.TYPE_RECEIVED
                            ))
                    } else {
                        msgList.add(
                            message(
                                msg,
                                message.TYPE_SENT
                            ))
                    }
                    count ++
                    if(count == jsonArray.length()) {
                        callback(msgList)
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("get_message", "Failed to get message: " + e.message)
                // 处理插入失败的情况
                callback(ArrayList()) // 返回一个空的用户列表
            }
        })
    }

    fun add_message(userA: String, userB: String, msg:String) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("userA", userA)
        jsonObject.put("userB", userB)
        jsonObject.put("msg", msg)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "add_message")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // 处理插入成功的情况
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("add_message", "Failed to add message: " + e.message)
                // 处理插入失败的情况
            }
        })
    }
}