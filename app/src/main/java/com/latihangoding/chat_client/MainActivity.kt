package com.latihangoding.chat_client

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mSocket: Socket
    var gson = Gson()

    val mAdapter by lazy {
        MainAdapter()
    }

    val currData = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            mSocket = IO.socket("http://10.0.2.2:3000")
            mSocket.connect()
            mSocket.on(Socket.EVENT_CONNECT) {
                Log.d("success", "Masuk success pake eko")
            }
            mSocket.on(Socket.EVENT_CONNECT_ERROR) {
                for (data in it) {
                    Log.e("error", "cause $data")
                }
            }
            mSocket.on("restoreChat") {
                val data = it.firstOrNull() as String?
                val result = gson.fromJson(data, Test::class.java)
                Log.d("success", "Masuk result $result")
                runOnUiThread {
                    mAdapter.submitList(result.data)
                    currData.addAll(result.data)
                    rv_main.smoothScrollToPosition(currData.size - 1)
                }
            }
            mSocket.on("message") {
                val data = it.firstOrNull() as String?
                val result = gson.fromJson(data, Message::class.java)
                runOnUiThread {
                    currData.add(currData.size, result)
                    mAdapter.submitList(currData)
                    rv_main.smoothScrollToPosition(currData.size - 1)
                }
            }
        } catch (e: Exception) {
            Log.d("fail", "Failed to connect cause ${e.message}")
        }

        rv_main.adapter = mAdapter

        iv_submit.setOnClickListener {
            val text = et_main.text.toString().trim()
            if (text != "") {
                et_main.setText("")
                mSocket.emit("newMessage", gson.toJson(Message(text)))
                currData.add(currData.size, Message(text))
                mAdapter.submitList(currData)
            }
        }
    }
}

data class Test(val data: List<Message>)

data class Message(val chat: String)