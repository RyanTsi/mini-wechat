package com.example.finalwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class Activity_info_xjq : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_xjq)
        val account = intent.getStringExtra("friend_id")
        val nickname = intent.getStringExtra("friend_nickname")
        val avatar = intent.getIntExtra("friend_avatar", 0)
        val a = findViewById<TextView>(R.id.account)
        a.text = account
        val b = findViewById<TextView>(R.id.nickname)
        b.text = nickname
        val c = findViewById<ImageView>(R.id.avatar)
        c.setImageResource(R.mipmap.avatar1 + avatar)
    }
}