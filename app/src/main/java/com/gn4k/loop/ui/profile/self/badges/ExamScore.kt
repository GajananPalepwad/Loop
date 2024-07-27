package com.gn4k.loop.ui.profile.self.badges

import ApiService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityExamScoreBinding
import com.gn4k.loop.models.request.AddBadgeRequest
import com.gn4k.loop.models.request.JoinLeaveMeetRequest
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.models.response.UserAllDataResponse
import com.gn4k.loop.ui.auth.ChooseRegOrLog
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.SkillSelector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExamScore : AppCompatActivity() {

    lateinit var binding: ActivityExamScoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getStringExtra("score")?.toInt()
        val badge = intent.getStringExtra("badge")

        binding.tvScore.text = score.toString()

        if (score != null) {
            binding.scoreProgressBar.progress = score

            if (score >= 60) {
                binding.tvMsg.text = "Congratulations! You passed."
                binding.btnCollect.text = "Collect Your Badge"
            } else {
                binding.animation.visibility = View.GONE
                binding.tvMsg.text = "Better luck next time!"
                binding.btnCollect.text = "Quit"
            }

            // Add click listener to button if needed
            binding.btnCollect.setOnClickListener {
                if (score >= 60) {
                    addBadge(badge.toString())
                } else {
                    val intent: Intent = Intent(baseContext, MainHome::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

    }

    private fun addBadge(badge: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.addBadges(AddBadgeRequest(MainHome.USER_ID.toInt(), badge))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        Toast.makeText(baseContext, meetingResponse?.message, Toast.LENGTH_SHORT).show()
                        val user = UserRequest(MainHome.USER_ID.toInt(), MainHome.USER_ID.toInt())
                        getUserData(user)
                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<CreateMeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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

                    val intent: Intent = Intent(baseContext, MainHome::class.java)
                    startActivity(intent)
                    finish()

                }
            }

            override fun onFailure(call: Call<UserAllDataResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(this@ExamScore, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


}