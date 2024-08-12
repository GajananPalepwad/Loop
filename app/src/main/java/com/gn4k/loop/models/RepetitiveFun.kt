package com.gn4k.loop.models

import ApiService
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.UserAllDataResponse
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Matcher

class RepetitiveFun {
    lateinit var context: Context
    fun getUserData(context: Context) {
        this.context = context

        val user = UserRequest(MainHome.USER_ID.toInt(), MainHome.USER_ID.toInt())

        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.getUserData(user)?.enqueue(object : Callback<UserAllDataResponse?> {
            override fun onResponse(
                call: Call<UserAllDataResponse?>,
                response: Response<UserAllDataResponse?>
            ) {
                if (response.isSuccessful) {
                    val userResponse = response.body()

                    if (userResponse != null) {
                        MainHome.USER_NAME = userResponse.user.name
                        MainHome.USER_EMAIL = userResponse.user.email
                        MainHome.USER_USERNAME = userResponse.user.username
                        MainHome.USER_NAME = userResponse.user.name
                        MainHome.USER_BADGES = userResponse.user.badges ?: emptyList()
                        MainHome.USER_ABOUT = userResponse.user.about ?: ""
                        MainHome.USER_PHOTO_URL = userResponse.user.photo_url ?: ""
                        MainHome.USER_CREATED_AT = userResponse.user.created_at
                        MainHome.USER_UPDATED_AT = userResponse.user.updated_at
                        MainHome.USER_LAST_LOGIN = userResponse.user.last_login ?: ""
                        MainHome.USER_STATUS = userResponse.user.status ?: ""
                        MainHome.USER_ROLE = userResponse.user.role ?: ""
                        MainHome.USER_LOCATION = userResponse.user.location ?: ""
                        MainHome.USER_WEBSITE = userResponse.user.website ?: ""
                        MainHome.USER_SKILLS = userResponse.user.skills ?: emptyList()
                        MainHome.USER_FOLLOWERS_COUNT = userResponse.user.followers_count
                        MainHome.USER_FOLLOWING_COUNT = userResponse.user.following_count
                        MainHome.GEMINI_KEY = userResponse.gemini_key
                        userResponse.is_following
                    }

                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserAllDataResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 100_000 -> String.format("%dK", count / 1_000)
            count >= 10_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun makeLinksClickable(textView: TextView, text: String) {
        val spannableString = SpannableString(text)

        val linkifyPattern = Patterns.WEB_URL
        val matcher: Matcher = linkifyPattern.matcher(text)

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            var url = text.substring(start, end)

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }

            spannableString.setSpan(URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(textView.context.getColor(R.color.link_color)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance() // Enable link clicking
    }
}