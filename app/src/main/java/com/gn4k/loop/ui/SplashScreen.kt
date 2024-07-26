package com.gn4k.loop.ui

import ApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.RegisterRequest
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.Skills
import com.gn4k.loop.models.response.UserAllDataResponse
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.auth.ChooseRegOrLog
import com.gn4k.loop.ui.home.HomeFeed
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.SkillSelector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashScreen : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        MainHome.USER_ID = sharedPref.getString("user_id", "").toString()

        if(MainHome.USER_ID=="" || MainHome.USER_ID==null){
            val intent: Intent = Intent(baseContext, ChooseRegOrLog::class.java)
            startActivity(intent)
            finish()
        }else{
            val user = UserRequest(MainHome.USER_ID.toInt(), MainHome.USER_ID.toInt())
            getUserData(user)
        }


    }

    fun getUserData(user: UserRequest) {

        val BASE_URL = getString(R.string.base_url)
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
                        MainHome.USER_FOLLOWERS_COUNT = userResponse.user.followers_count.toString()
                        MainHome.USER_FOLLOWING_COUNT = userResponse.user.following_count.toString()
                        userResponse.is_following
                    }


                    if (MainHome.USER_ID.equals(-1) || MainHome.USER_ID.isEmpty()) {
                        val intent: Intent = Intent(baseContext, ChooseRegOrLog::class.java)
                        startActivity(intent)
                        finish()
                    } else if (MainHome.USER_SKILLS.isEmpty()) {
                        val intent: Intent = Intent(baseContext, SkillSelector::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent: Intent = Intent(baseContext, MainHome::class.java)
                        startActivity(intent)
                        finish()
                    }

                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserAllDataResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(this@SplashScreen, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleErrorResponse(response: Response<UserAllDataResponse?>) {
        when (response.code()) {
            400 -> {
                Log.d("Reg", "Missing required fields")
                Toast.makeText(this, "Missing required fields", Toast.LENGTH_SHORT).show()
            }

            404 -> {
                Log.d("Reg", "User not found")
            }

            500 -> {
                Log.d("Reg", "Database connection failed")
                Toast.makeText(this, "Database connection failed", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Log.d("Reg", "Unexpected Error: ${response.message()}")
                Toast.makeText(this, "Unexpected Error: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}