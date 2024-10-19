package com.gn4k.loop.notificationModel

import com.gn4k.loop.api.ApiService
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.NotificationRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SaveNotificationInDB {

    fun save(context: Context, senderUserId: Int, recipientUserId: Int, postId: Int, type: String, message: String) {
        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.saveNotification(NotificationRequest(senderUserId, recipientUserId, postId, type, message))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                    }
                }

                override fun onFailure(call: Call<CreateMeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

}