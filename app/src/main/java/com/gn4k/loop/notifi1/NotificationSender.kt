package com.gn4k.loop.notifi1

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NotificationSender(val context: Context) {
    private val FCM_API = "https://fcm.googleapis.com/v1/projects/loop-4aeac/messages:send"
    private val serverKey = "Bearer 5c7e5cc998dc20b1529324303b29c79a548c3e7f" // Replace with your FCM server key
    private val contentType = "application/json"
    private lateinit var requestQueue: RequestQueue

    init {
        requestQueue = Volley.newRequestQueue(context)
    }

    fun sendNotificationToTopic(topic: String, title: String, message: String) {
        val topicPath = "/topics/$topic"
        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", title)
            notificationBody.put("body", message)
            notification.put("to", topicPath)
            notification.put("notification", notificationBody)
        } catch (e: Exception) {
            Log.e("NotificationSender", "Error creating notification JSON", e)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, FCM_API, notification,
            Response.Listener { response ->
                Log.i("NotificationSender", "Notification sent: $response")
            },
            Response.ErrorListener {
                Log.e("NotificationSender", "Error sending notification: $it")
            }) {
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(request)
    }
}
