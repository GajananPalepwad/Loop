package com.gn4k.loop.ui.profile.self

import ApiService
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityProfileEditingBinding
import com.gn4k.loop.models.request.JoinLeaveMeetRequest
import com.gn4k.loop.models.request.UpdateProfileRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileEditing : AppCompatActivity() {

    lateinit var binding: ActivityProfileEditingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edName.setText(MainHome.USER_NAME)
        binding.edAbout.setText(MainHome.USER_ABOUT)
        binding.edLocation.setText(MainHome.USER_LOCATION)
        binding.edWebsite.setText(MainHome.USER_WEBSITE)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.btnUpdate.setOnClickListener {

            binding.tilName.error = null
            binding.tilAbout.error = null
            binding.tilLocation.error = null

            if (binding.edName.text.toString().isEmpty()) {
                binding.tilName.error = "Name is required"
                return@setOnClickListener
            }

            if (binding.edAbout.text.toString().isEmpty()) {
                binding.tilAbout.error = "About is required"
                return@setOnClickListener
            }

            if (binding.edLocation.text.toString().isEmpty()) {
                binding.tilLocation.error = "Location is required"
                return@setOnClickListener
            }

            updateProfile(
                binding.edName.text.toString(),
                binding.edAbout.text.toString(),
                binding.edLocation.text.toString(),
                binding.edWebsite.text.toString()
            )

        }

    }

    private fun updateProfile(name: String, about: String, location: String, website: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.editProfile(UpdateProfileRequest(userId = MainHome.USER_ID.toInt(), name = name, about = about, location = location, website = website))
            ?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(
                    call: Call<UserResponse?>,
                    response: Response<UserResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        Toast.makeText(baseContext, meetingResponse?.message, Toast.LENGTH_SHORT).show()
                        MainHome.USER_NAME = name
                        MainHome.USER_ABOUT = about
                        MainHome.USER_LOCATION = location
                        MainHome.USER_WEBSITE = formatUrl(website)
                        onBackPressed()
                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun formatUrl(url: String): String {
        return when {
            url.startsWith("http://") -> url.replace("http://", "https://")
            url.startsWith("https://") -> url
            else -> "https://$url"
        }
    }
}