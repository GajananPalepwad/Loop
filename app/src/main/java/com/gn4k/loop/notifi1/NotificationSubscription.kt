package com.gn4k.loop.notifi1

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging

class NotificationSubscription (val context: Context){

    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic: $topic"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe to topic: $topic"
                }
                Log.d(TAG, msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }

    // Method to unsubscribe from a topic
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Unsubscribed from topic: $topic"
                if (!task.isSuccessful) {
                    msg = "Failed to unsubscribe from topic: $topic"
                }
                Log.d(TAG, msg)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }
}