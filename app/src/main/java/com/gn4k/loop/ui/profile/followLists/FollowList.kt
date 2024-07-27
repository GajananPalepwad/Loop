package com.gn4k.loop.ui.profile.followLists

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.gn4k.loop.R
import com.gn4k.loop.databinding.ActivityFollowListBinding
import com.gn4k.loop.ui.profile.self.ProfilePost
import com.gn4k.loop.ui.profile.self.ProfileProjects
import kotlin.properties.Delegates

class FollowList : AppCompatActivity() {

    private var transaction = supportFragmentManager.beginTransaction()
    lateinit var binding: ActivityFollowListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type")
        userId = intent.getStringExtra("userId")?.toInt()!!
        val name = intent.getStringExtra("name")

        binding.tvName.text = name

        binding.back.setOnClickListener {
            onBackPressed()
        }


        when (type) {
            "follower" -> switchToFollowers()
            "following" -> switchToFollowing()
            else -> {
                // Handle unexpected type value
                finish() // Close the activity or handle appropriately
                return
            }
        }

        binding.btnFollowings.setOnClickListener {
            switchToFollowing()
        }

        binding.btnFollowers.setOnClickListener {
            switchToFollowers()
        }
    }

    private fun switchToFollowers() {
        binding.btnFollowers.setTextColor(getColor(R.color.app_color))
        binding.btnFollowings.setTextColor(getColor(R.color.white))
        setFragment(FollowerList())
    }

    private fun switchToFollowing() {
        binding.btnFollowings.setTextColor(getColor(R.color.app_color))
        binding.btnFollowers.setTextColor(getColor(R.color.white))
        setFragment(FollowingList())
    }

    private fun setFragment(fragment: Fragment) {
        transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null) // Add the transaction to the back stack if needed
        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        var userId by Delegates.notNull<Int>()
    }
}
