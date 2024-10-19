package com.gn4k.loop.ui.msg

import com.gn4k.loop.api.ApiService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.adapters.MessageAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityChattingBinding
import com.gn4k.loop.models.request.MarkSeenRequest
import com.gn4k.loop.models.request.SendMsgRequest
import com.gn4k.loop.models.response.ChattingRespnse
import com.gn4k.loop.models.response.MarkSeenResponse
import com.gn4k.loop.models.response.Msg
import com.gn4k.loop.models.response.SendMsgResponse
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Chatting : AppCompatActivity() {

    lateinit var binding: ActivityChattingBinding
    lateinit var msgMutableList: MutableList<Msg>

    lateinit var userId: String
    var lastMessageId: Int = 0
    var isPolling = true
    lateinit var loading: CustomLoading


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = CustomLoading(this)
        loading.startLoading()

        val conversationId = intent.getStringExtra("conversation_id")
        userId = intent.getStringExtra("user_id").toString()
        val userName = intent.getStringExtra("user_name")
        val profileLink = intent.getStringExtra("profile_link")

        binding.tvName.text = userName
        Glide.with(this)
            .load(getString(R.string.base_url) + profileLink)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(binding.imgProfile)


        if (conversationId != null) {
            fetchAllMessage(conversationId.toInt())
        }

        binding.header.setOnClickListener {
            if(userId.toInt()== MainHome.USER_ID.toInt()){
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }else {
                val intent = Intent(this, OthersProfile::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        }

        binding.btnSendMsg.setOnClickListener {
            if (binding.edMsg.text.isNotEmpty()) {
                val sendMsgRequest =
                    userId?.let { it1 ->
                        SendMsgRequest(
                            MainHome.USER_ID.toInt(),
                            it1.toInt(),
                            binding.edMsg.text.toString()
                        )
                    }
                if (sendMsgRequest != null) {
                    sendMessage(sendMsgRequest)
                }
                binding.edMsg.setText("")
            }
        }

    }

    private fun markSeen(lastMsgId: Int, userId: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.markMsgSeen(MarkSeenRequest(userId, lastMsgId))
            ?.enqueue(object : Callback<MarkSeenResponse?> {
                override fun onResponse(
                    call: Call<MarkSeenResponse?>,
                    response: Response<MarkSeenResponse?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                    }
                }

                override fun onFailure(call: Call<MarkSeenResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun sendMessage(sendMsgRequest: SendMsgRequest) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.sendMsg(sendMsgRequest)
            ?.enqueue(object : Callback<SendMsgResponse?> {
                override fun onResponse(
                    call: Call<SendMsgResponse?>,
                    response: Response<SendMsgResponse?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()

//                        if (msgResponse!!.status == "success"){
//                            Toast.makeText(baseContext, "send", Toast.LENGTH_SHORT).show()
//                        }

                    }
                }

                override fun onFailure(call: Call<SendMsgResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun fetchAllMessage(conversationId: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchAllMsg(conversationId)
            ?.enqueue(object : Callback<ChattingRespnse?> {
                override fun onResponse(
                    call: Call<ChattingRespnse?>,
                    response: Response<ChattingRespnse?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                        msgMutableList = msgResponse?.messages?.toMutableList() ?: mutableListOf()

                        val adapter = MessageAdapter(msgMutableList)
                        binding.recyclerView.layoutManager =
                            LinearLayoutManager(this@Chatting).apply {
                                stackFromEnd = true
                            }
                        binding.recyclerView.adapter = adapter
                        if (msgMutableList.size!=0) {
                            lastMessageId = msgMutableList[msgMutableList.size - 1].id
                            markSeen(lastMessageId, MainHome.USER_ID.toInt())
                        }
                        startLongPolling(conversationId, userId.toInt())
                        loading.stopLoading()
                    } else {
//                    handleErrorResponse(response)
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<ChattingRespnse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loading.stopLoading()
                }
            })
    }

    private fun startLongPolling(conversationId: Int, lastMsgId: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        Thread {
            while (isPolling) {
                try {
                    val response = apiService?.getMsg(conversationId, lastMsgId)?.execute()
                    if (response?.isSuccessful == true) {
                        val msgResponse = response.body()
                        val newMessages =
                            msgResponse?.messages?.filter { it.id > lastMessageId } ?: emptyList()

                        if (newMessages.isNotEmpty()) {
                            runOnUiThread {
                                msgMutableList.addAll(newMessages)
                                lastMessageId = msgMutableList.last().id
                                markSeen(lastMessageId, MainHome.USER_ID.toInt())
                                binding.recyclerView.adapter?.notifyItemRangeInserted(
                                    msgMutableList.size - newMessages.size, newMessages.size
                                )
                                binding.recyclerView.smoothScrollToPosition(msgMutableList.size - 1)
                            }
                        }
                    }
                    Thread.sleep(500) // Wait for a short duration before checking again
                } catch (e: Exception) {
                    Log.e("Chatting", "Polling error: ${e.message}")
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isPolling = false
    }

}