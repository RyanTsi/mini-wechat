package com.example.finalwork

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class MessageFragment_xjq : Fragment() {
    var account:String? = null
    var avatar:Int? = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        return view
    }
    override fun onResume() {
        super.onResume()
        val bundle = arguments
        account = bundle?.getString("account")
        avatar = bundle?.getInt("avatar")
    }

}