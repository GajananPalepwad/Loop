package com.gn4k.loop.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gn4k.loop.R
import com.gn4k.loop.databinding.ActivityChooseRegOrLogBinding
import com.gn4k.loop.databinding.ActivityLoginBinding

class ChooseRegOrLog : AppCompatActivity() {

    private lateinit var binding : ActivityChooseRegOrLogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_reg_or_log)
        binding = ActivityChooseRegOrLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val intent: Intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
        binding.btnRegister.setOnClickListener {
            val intent: Intent = Intent(this, Reg::class.java)
            startActivity(intent)
        }
    }
}