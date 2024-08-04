package com.gn4k.loop.notificationModel

//import android.content.Context
//import com.google.auth.oauth2.GoogleCredentials
//import com.google.firebase.FirebaseApp
//import com.google.firebase.messaging.FirebaseMessaging
//import okhttp3.*
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody.Companion.toRequestBody
//import org.json.JSONObject
//import java.io.IOException
//import java.io.InputStream
//
//class FCMService(private val context: Context) {
//    private val client = OkHttpClient()
//    private val JSON = "application/json; charset=utf-8".toMediaType()
//    private val FCM_SEND_ENDPOINT = "https://fcm.googleapis.com/v1/projects/loop-4aeac/messages:send"
//
//    init {
//        if (FirebaseApp.getApps(context).isEmpty()) {
//            FirebaseApp.initializeApp(context)
//        }
//    }
//
//    private fun getAccessToken(): String {
//        val googleCredentials = GoogleCredentials
//            .fromStream(getServiceAccountKeyStream())
//            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
//        googleCredentials.refresh()
//        return googleCredentials.accessToken.tokenValue
//    }
//
//    private fun getServiceAccountKeyStream(): InputStream {
//        return context.assets.open("/loop-4aeac.json")
//    }
//
//    fun sendNotification(title: String, body: String) {
//        val message = JSONObject().apply {
//            put("topic", "9")
//            put("notification", JSONObject().apply {
//                put("title", title)
//                put("body", body)
//            })
//        }
//
//        val root = JSONObject().apply {
//            put("message", message)
//        }
//
//        val requestBody = root.toString().toRequestBody(JSON)
//        val request = Request.Builder()
//            .url(FCM_SEND_ENDPOINT)
//            .post(requestBody)
//            .addHeader("Authorization", "Bearer ${getAccessToken()}")
//            .addHeader("Content-Type", "application/json")
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                println("FCM Notification sent. Response: ${response.body?.string()}")
//            }
//        })
//    }
//
//    fun getDeviceToken(callback: (String?) -> Unit) {
//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val token = task.result
//                    callback(token)
//                } else {
//                    callback(null)
//                }
//            }
//    }
//}