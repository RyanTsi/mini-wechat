package com.example.finalwork

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class FriendFragment_xjq :Fragment() {
    var account:String? = null
    var avatar:Int? = 0
    private var friendXjqList:ArrayList<User_info_xjq> = arrayListOf()
    lateinit var recyclerView:RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_friend, container, false)
        recyclerView = view.findViewById(R.id.friendRecyclerView)
        // 设置布局管理器
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        return view
    }

    override fun onResume() {
        super.onResume()
        val bundle = arguments
        account = bundle?.getString("account")
        avatar = bundle?.getInt("avatar")
        refresh(account)
    }
    fun refresh(account: String?) {
        get_friend(account.toString()) {
            requireActivity().runOnUiThread {
                val adapter = FriendAdapter_xjq()
                recyclerView.adapter = adapter
            }
        }
    }

    inner class FriendAdapter_xjq : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_ADD_FRIEND = 0
        private val VIEW_TYPE_FRIEND_ITEM = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_ADD_FRIEND -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_friend, parent, false)
                    AddFriendViewHolder(view)
                }
                VIEW_TYPE_FRIEND_ITEM -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
                    FriendViewHolder(view)
                }
                else -> throw IllegalArgumentException("Invalid view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is AddFriendViewHolder) {
                // 设置 "添加新好友" 的点击事件
                holder.itemView.setOnClickListener {
                    val intent = Intent(requireContext(), add_friend_xjq::class.java)
                    intent.putExtra("account", account)
                    startActivity(intent)
                    // 执行添加新好友的逻辑
                }
            } else if (holder is FriendViewHolder) {
                // 设置好友项的数据和点击事件
                val friend = friendXjqList[position - 1] // 减去第一项 "添加新好友"
                holder.bind(friend)
                holder.itemView.setOnClickListener {
                    val intent = Intent(requireContext(), Activity_info_xjq::class.java)
                    intent.putExtra("friend_id", friend.account.toString())
                    intent.putExtra("friend_nickname", friend.nickname)
                    intent.putExtra("friend_avatar", friend.avatar)
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int {
            return friendXjqList.size + 1 // 加上 "添加新好友" 这一项
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) VIEW_TYPE_ADD_FRIEND else VIEW_TYPE_FRIEND_ITEM
        }

        // 定义 "添加新好友" 的 ViewHolder
        inner class AddFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        // 定义好友项的 ViewHolder
        inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(friendXjq: User_info_xjq) {
                val itemAccount = itemView.findViewById<TextView>(R.id.userAccount)
                val itemNickname = itemView.findViewById<TextView>(R.id.userNickname)
                val image = itemView.findViewById<ImageView>(R.id.avatar)
                val send_msg = itemView.findViewById<Button>(R.id.send_msg)
                itemAccount.text = friendXjq.account.toString()
                itemNickname.text = friendXjq.nickname
                image.setImageResource(R.mipmap.avatar1 + friendXjq.avatar)
                send_msg.setOnClickListener {
                    val intent = Intent(requireContext(), send_message_xjq::class.java)
                    intent.putExtra("my_id", account)
                    intent.putExtra("your_id", friendXjq.account.toString())
                    intent.putExtra("your_nickname", friendXjq.nickname)
                    intent.putExtra("my_avatar", avatar)
                    intent.putExtra("your_avatar", friendXjq.avatar)
                    startActivity(intent)
                }
            }
        }
    }
    fun get_friend(account: String, callback: (ArrayList<User_info_xjq>) -> Unit) {
        val url = "http://47.115.207.251:5000/"
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonObject = JSONObject()
        jsonObject.put("userA", account)
        val requestBody = jsonObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url + "get_friend")
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
                friendXjqList.clear()
                var count = 0
                for(i in 0 until tempList.size step 3) {
                    val user = User_info_xjq(tempList[i].toInt(), tempList[i + 1], tempList[i + 2].toInt())
                    if (user != null) {
                        friendXjqList.add(user)
                    }
                    count++
                    // 当所有用户信息都添加完毕时，通过回调返回用户列表
                    if (3 * count == tempList.size) {
                        callback(friendXjqList)
                    }
                }
                callback(friendXjqList)
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // 处理插入失败的情况
                callback(ArrayList()) // 返回一个空的用户列表
            }
        })
    }
}