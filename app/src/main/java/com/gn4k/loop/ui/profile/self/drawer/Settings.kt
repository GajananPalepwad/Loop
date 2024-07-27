package com.gn4k.loop.ui.profile.self.drawer

import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.gn4k.loop.databinding.ActivitySettingsBinding

class Settings : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        // Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Set the switch state based on the saved preference
        val notificationsEnabled = sharedPreferences.getBoolean("notifications", false)
        binding.notification.isChecked = notificationsEnabled

        // Set up the listener for the switch
        binding.notification.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit {
                putBoolean("notifications", isChecked)
            }
            // Enable or disable notifications based on isChecked
            if (isChecked) {
                // Enable notifications
            } else {
                // Disable notifications
            }
        }
    }
}
