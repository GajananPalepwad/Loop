package com.gn4k.loop.ui.post

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.databinding.ActivityViewImageInFullBinding

class ViewImageInFull : AppCompatActivity() {

    private lateinit var binding: ActivityViewImageInFullBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewImageInFullBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("image_url").toString()
        Log.d("ViewImageInFull", "Image URL: $imageUrl")

        if (imageUrl != null) {
            Glide.with(this)
                .load(getString(R.string.base_url)+imageUrl)
                .into(binding.image)
        } else {
            Log.e("ViewImageInFull", "No image URL provided")
        }

        binding.back.setOnClickListener {
            onBackPressed()
            finish()
        }
    }
}
