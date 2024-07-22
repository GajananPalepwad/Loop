package com.gn4k.loop.ui.auth

import ApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.rive.runtime.kotlin.core.Rive
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityLoginBinding
import com.gn4k.loop.models.request.LoginRequest
import com.gn4k.loop.models.request.RegisterRequest
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.UserAllDataResponse
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.SkillSelector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val stateMachineName = "State Machine 1"

    private var apiService: ApiService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)


        Rive.init(this)


        binding.edEmail.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.loginCharacter.controller.setBooleanState(stateMachineName, "Check", true)
            } else {
                binding.loginCharacter.controller.setBooleanState(stateMachineName, "Check", false)

            }
        }

        binding.edPasssword.setOnFocusChangeListener { view, b ->
            if (b) {
                binding.loginCharacter.controller.setBooleanState(
                    stateMachineName,
                    "hands_up",
                    true
                )
            } else {
                binding.loginCharacter.controller.setBooleanState(
                    stateMachineName,
                    "hands_up",
                    false
                )

            }
        }




        binding.edEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {

                try {
                    binding.loginCharacter.controller.setNumberState(
                        stateMachineName,
                        "Look",
                        p0!!.length.toFloat()
                    )
                } catch (e: Exception) {
                }
            }

        })


        binding.btnLogin.setOnClickListener {

            binding.edPasssword.clearFocus()
            binding.tilPassword.error = null
            binding.tilEmail.error = null

            val email = binding.edEmail.text.toString().trim()
            if (email.isEmpty()) {
                // Show error message for empty password field
                binding.tilEmail.error = "Please enter a Email"
                binding.loginCharacter.controller.fireState(stateMachineName, "fail");
                return@setOnClickListener
            }

            val password = binding.edPasssword.text.toString().trim()
            if (password.isEmpty()) {
                // Show error message for empty password field
                binding.tilPassword.error = "Please enter a Password"
                binding.loginCharacter.controller.fireState(stateMachineName, "fail");
                return@setOnClickListener
            }


            val user = LoginRequest(email, password)
            loginUser(user);


        }

    }


    private fun loginUser(user: LoginRequest) {
        apiService?.loginUser(user)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {

                    binding.loginCharacter.controller.setBooleanState(
                        stateMachineName,
                        "hands_up",
                        false
                    )

                    binding.loginCharacter.controller.fireState(stateMachineName, "success");


                    val userResponse = response.body()
                    Log.d("Reg", "User login successfully: ${userResponse?.message}")
                    Toast.makeText(this@Login, "User login successfully", Toast.LENGTH_SHORT).show()

                    val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    if (userResponse != null) {
                        editor.putString("user_id", userResponse.userId.toString())
                    }
                    editor.apply()
                    if (userResponse != null) {
                        MainHome.USER_ID = userResponse.userId.toString()

                        val user = UserRequest(MainHome.USER_ID.toInt(), MainHome.USER_ID.toInt())
                        getUserData(user)
                    }

                } else {

                    binding.loginCharacter.controller.fireState(stateMachineName, "fail");
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(this@Login, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleErrorResponse(response: Response<UserResponse?>) {
        when (response.code()) {
            400 -> {
                Log.d("Reg", "Bad Request: All fields are required")
                Toast.makeText(this, "Bad Request: All fields are required", Toast.LENGTH_SHORT)
                    .show()
            }

            401 -> {
                Log.d("Reg", "Incorrect password")
                binding.tilPassword.error = "Incorrect password"
            }

            402 -> {
                Log.d("Reg", "Invalid email")
                binding.tilEmail.error = "Invalid email"
            }

            500 -> {
                Log.d("Reg", "Internal Server Error")
                Toast.makeText(this, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Log.d("Reg", "Unexpected Error: ${response.message()}")
                Toast.makeText(this, "Unexpected Error: ${response.code()}", Toast.LENGTH_SHORT)
                    .show()
            }
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


                    val intent: Intent = Intent(baseContext, MainHome::class.java)
                    startActivity(intent)
                    finish()


                } else {
                    Toast.makeText(this@Login, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserAllDataResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(this@Login, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

}