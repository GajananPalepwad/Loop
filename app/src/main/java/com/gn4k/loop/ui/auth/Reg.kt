package com.gn4k.loop.ui.auth

import com.gn4k.loop.api.ApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityRegBinding
import com.gn4k.loop.models.request.OtpResponse
import com.gn4k.loop.models.request.RegisterRequest
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.UserAllDataResponse
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Reg : AppCompatActivity() {

    private var apiService: ApiService? = null
    private lateinit var binding : ActivityRegBinding
    var otp: String = ""
    var isOTPVerified = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.btnLogin.setOnClickListener {
            val intent: Intent = Intent(baseContext, Login::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSendOTP.setOnClickListener {
            val email = binding.edEmail.text.toString().trim()
            if (email.isEmpty()) {
                // Show error message for empty email field
                binding.tilEmail.error = "Please enter your email address"
                return@setOnClickListener // Exit the function if email is empty
            }
            binding.btnSendOTP.isEnabled = false
            sendOTP(email)
        }

        binding.btnVerify.setOnClickListener {
            val otp = binding.edOTP.text.toString().trim()
            if (otp.isEmpty()) {
                // Show error message for empty OTP field
                binding.tilOTP.error = "Please enter the OTP"
                return@setOnClickListener
            }
            if (otp == this.otp) {
                isOTPVerified = true
                binding.btnVerify.backgroundTintList =
                    ContextCompat.getColorStateList(baseContext, R.color.green)
                binding.btnVerify.setTextColor(getResources().getColor(R.color.white))
                binding.tilOTP.error = null
                binding.btnVerify.setText("Verified")
            } else {
                binding.tilOTP.error = "Invalid OTP"
            }
        }

        binding.btnReg.setOnClickListener {

            binding.tilPassword.error = null
            binding.tilName.error = null
            binding.tilEmail.error = null
            binding.tilUserName.error = null
            binding.tilConfirmPassword.error = null

            val fullName = binding.edName.text.toString().trim()
            if (fullName.isEmpty()) {
                // Show error message for empty full name field
                binding.tilName.error = "Please enter your full name"
                return@setOnClickListener
            }

            val username = binding.edUserId.text.toString().trim()
            if (username.isEmpty()) {
                // Show error message for empty username field
                binding.tilUserName.error = "Please enter a username"
                return@setOnClickListener
            }

            if (!isValidUsername(username)) {
                binding.tilUserName.error = "Username must be 4-20 characters and contain only letters, numbers, periods, and underscores."
                return@setOnClickListener
            }

            val email = binding.edEmail.text.toString().trim()
            if (email.isEmpty()) {
                // Show error message for empty email field
                binding.tilEmail.error = "Please enter your email address"
                return@setOnClickListener // Exit the function if email is empty
            }

            if (!isValidEmail(email)) {
                binding.tilEmail.error = "Please enter a valid email address"
                return@setOnClickListener
            }

            if(otp.isEmpty()){
                binding.tilEmail.error = "Please send your OTP"
                return@setOnClickListener
            }

            if (!isOTPVerified) {
                binding.tilOTP.error = "Please verify your OTP"
                return@setOnClickListener
            }

            val password = binding.edPassword.text.toString().trim()
            if (password.isEmpty()) {
                // Show error message for empty password field
                binding.tilPassword.error = "Please enter a password"
                return@setOnClickListener
            }

            val confirmPassword = binding.edConfirmPassword.text.toString().trim()
            if (password != confirmPassword) {
                // Show error message for password mismatch
                binding.tilConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Check password strength
            if (!isPasswordStrong(password)) {
                // Show error message for weak password
                binding.tilPassword.error = "Password is weak. Please use a combination of uppercase letters, lowercase letters, numbers, and symbols."
                return@setOnClickListener
            }

            // Create the RegisterRequest object with validated fields
            val user = RegisterRequest(email, username, fullName, password)
            registerUser(user)
        }



    }

    private fun registerUser(user: RegisterRequest) {
        apiService?.registerUser(user)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    Log.d("Reg", "User registered successfully: ${userResponse?.message}")
                    Toast.makeText(this@Reg, "User registered successfully", Toast.LENGTH_SHORT).show()

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
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(this@Reg, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleErrorResponse(response: Response<UserResponse?>) {
        when (response.code()) {
            400 -> {
                Log.d("Reg", "Bad Request: All fields are required")
                Toast.makeText(this, "Bad Request: All fields are required", Toast.LENGTH_SHORT).show()
            }
            409 -> {
                Log.d("Reg", "Conflict: Email or username already exists")
                binding.tilEmail.error = "Email or username already exists"
                binding.tilUserName.error = "Email or username already exists"
            }
            500 -> {
                Log.d("Reg", "Internal Server Error")
                Toast.makeText(this, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.d("Reg", "Unexpected Error: ${response.message()}")
                Toast.makeText(this, "Unexpected Error: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOTP(email: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.sendOtp(email)
            ?.enqueue(object : Callback<OtpResponse?> {
                override fun onResponse(
                    call: Call<OtpResponse?>,
                    response: Response<OtpResponse?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                        Log.d("Reg", "OTP sent successfully")
                        if (msgResponse != null) {
                            if(msgResponse.Error == "200") {
                                binding.btnSendOTP.backgroundTintList =
                                    ContextCompat.getColorStateList(baseContext, R.color.app_color)
                                otp = msgResponse.OTP
                                binding.tilEmail.error = null
                                Handler(Looper.getMainLooper()).postDelayed({
                                    binding.btnSendOTP.isEnabled = true
                                }, 3000)
                            }
                        }

                    } else {
                        // Handle error response
                    }
                }

                override fun onFailure(call: Call<OtpResponse?>, t: Throwable) {
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
                        MainHome.USER_FOLLOWERS_COUNT = userResponse.user.followers_count
                        MainHome.USER_FOLLOWING_COUNT = userResponse.user.following_count
                        userResponse.is_following
                    }


                    val intent: Intent = Intent(baseContext, MainHome::class.java)
                    startActivity(intent)
                    finish()


                } else {
                    Toast.makeText(this@Reg, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserAllDataResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(this@Reg, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun isPasswordStrong(password: String): Boolean {
        val minLength = 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= minLength && hasUpperCase && hasLowerCase && hasDigit
    }

    private fun isValidUsername(username: String): Boolean {
        val allowedChars = "[a-zA-Z0-9_.]*"
        return Regex(allowedChars).matches(username)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*"
        return Regex(emailPattern).matches(email)
    }


}
