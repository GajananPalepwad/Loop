package com.gn4k.loop.ui.profile.self.drawer

import com.gn4k.loop.api.ApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivitySettingsBinding
import com.gn4k.loop.models.request.DeleteUserRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.ui.SplashScreen
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Settings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            onBackPressed()
        }


        binding.deleteProfile.setOnClickListener {
            if(binding.deleteContainer.visibility == View.GONE){
                binding.deleteContainer.visibility = View.VISIBLE
            }else{
                binding.deleteContainer.visibility = View.GONE
            }
        }

        binding.btnConfirmDelete.setOnClickListener {
            val password = binding.edPassword.text.toString()
            if (password.isNotEmpty()) {
                deleteUser(password)
            } else {
                binding.tilPassword.error = "Please enter your password"
            }

        }


        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val notificationsEnabled = sharedPreferences.getBoolean("notifications", false)
        binding.notification.isChecked = notificationsEnabled
        binding.notification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit {
                putBoolean("notifications", isChecked)
            }
            if (isChecked) {
                // Enable notifications
            } else {
                // Disable notifications
            }
        }
    }

    private fun deleteUser(password: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.deleteUser(DeleteUserRequest(MainHome.USER_ID.toInt(), password))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                        // Remove the participant from the list and notify the adapter
                        if (msgResponse != null) {
                            Toast.makeText(baseContext, msgResponse.message, Toast.LENGTH_SHORT).show()
                            val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putString("user_id", "-1")
                            editor.apply()
                            val intent = Intent(this@Settings, SplashScreen::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else if(response.code() == 401){
                        binding.tilPassword.error = "Incorrect password"
                    }
                }

                override fun onFailure(call: Call<CreateMeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}
