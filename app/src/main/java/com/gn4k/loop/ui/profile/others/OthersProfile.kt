package com.gn4k.loop.ui.profile.others

import ApiService
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ProfileBadgeAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityOthersProfileBinding
import com.gn4k.loop.models.request.FollowUnfollowRequest
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.FollowUnfollowResponse
import com.gn4k.loop.models.response.UserAllDataResponse
import com.gn4k.loop.notificationModel.SaveNotificationInDB
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.msg.Chatting
import com.gn4k.loop.ui.profile.followLists.FollowList
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OthersProfile : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        var userId: Int = 0
        var userName: String = ""
        var userEmail: String = ""
        var userUsername: String = ""
        var userBadges: List<String> = emptyList()
        var userAbout: String = ""
        var userPhotoUrl: String = ""
        var userCreatedAt: String = ""
        var userUpdatedAt: String = ""
        var userLastLogin: String = ""
        var userStatus: String = ""
        var userRole: String = ""
        var userLocation: String = ""
        var userWebsite: String = ""
        var userSkills: List<String> = emptyList()
        var userFollowersCount: String = ""
        var userFollowingCount: String = ""
        var isFollowing: Boolean = false
        var isFollowedBy: Boolean = false
        lateinit var loading: CustomLoading
    }

    lateinit var binding: ActivityOthersProfileBinding
    private var transaction = supportFragmentManager.beginTransaction()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOthersProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loading = CustomLoading(this)

        userId = intent.getStringExtra("userId")?.toInt()!!

        setFragment(OthersProfilePost())

        drawerLayout = binding.drawerLayout
        navView = binding.navView

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener(this)


        binding.back.setOnClickListener {
            onBackPressed()
            finish()
        }


        binding.btnPost.setOnClickListener {
            binding.btnPost.setTextColor(getColor(R.color.app_color))
            binding.btnProjects.setTextColor(getColor(R.color.white))
            setFragment(OthersProfilePost())
        }

        binding.btnProjects.setOnClickListener {
            binding.btnProjects.setTextColor(getColor(R.color.app_color))
            binding.btnPost.setTextColor(getColor(R.color.white))
            setFragment(OthersProfileProjects())
        }

        binding.btnOptions.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        binding.btnFollow.setOnClickListener {

            if (isFollowing) {
                binding.textFollow.text = "Follow"
                binding.textFollow.setTextColor(getColor(R.color.black))
                binding.btnFollowBackground.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(baseContext, R.color.white)
                )
                followUnfollow(userId, "unfollow")

            } else {
                binding.textFollow.text = "Unfollow"
                binding.textFollow.setTextColor(getColor(R.color.white))
                binding.btnFollowBackground.backgroundTintList = ColorStateList.valueOf(
                    Color.parseColor("#181818")
                )
                followUnfollow(userId, "follow")

                SaveNotificationInDB().save(
                    baseContext,
                    MainHome.USER_ID.toInt(),
                    userId,
                    1,
                    "follows",
                    "${MainHome.USER_NAME} started following you"
                )
            }

        }

        binding.btnFollowings.setOnClickListener {
            val intent = Intent(this, FollowList::class.java)
            intent.putExtra("type", "following")
            intent.putExtra("userId", userId.toString())
            intent.putExtra("name", userName)
            startActivity(intent)
        }

        binding.btnFollower.setOnClickListener {
            val intent = Intent(this, FollowList::class.java)
            intent.putExtra("type", "follower")
            intent.putExtra("userId", userId.toString())
            intent.putExtra("name", userName)
            startActivity(intent)
        }

        binding.btnMsg.setOnClickListener {
            var temp1 = MainHome.USER_ID.toInt()
            var temp2 = userId
            if (temp1 > temp2) {
                val temp = temp1
                temp1 = temp2
                temp2 = temp
            }

            var conversationId = "${temp1}1001${temp2}"

            val intent = Intent(this, Chatting::class.java)
            intent.putExtra("conversation_id", conversationId)
            intent.putExtra("user_id", userId.toString())
            intent.putExtra("user_name", userName)
            intent.putExtra("profile_link", userPhotoUrl)

            startActivity(intent)
        }

        getUserData(UserRequest(MainHome.USER_ID.toInt(), userId))
    }

    private fun followUnfollow(userId: Int, action: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.followUnfollow(FollowUnfollowRequest(MainHome.USER_ID.toInt(), userId, action))
            ?.enqueue(object : Callback<FollowUnfollowResponse?> {
                override fun onResponse(
                    call: Call<FollowUnfollowResponse?>,
                    response: Response<FollowUnfollowResponse?>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()

                        if (userResponse?.success!!) {
                            getUserData(UserRequest(MainHome.USER_ID.toInt(), userId))
                        }

                    } else {
//                    handleErrorResponse(response)
                        Toast.makeText(baseContext, "Something went wrong", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<FollowUnfollowResponse?>, t: Throwable) {
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
                    userResponse?.let {
                        userName = it.user.name
                        userEmail = it.user.email
                        userUsername = it.user.username
                        userBadges = it.user.badges ?: emptyList()
                        userAbout = it.user.about ?: ""
                        userPhotoUrl = it.user.photo_url ?: ""
                        userCreatedAt = it.user.created_at
                        userUpdatedAt = it.user.updated_at
                        userLastLogin = it.user.last_login ?: ""
                        userStatus = it.user.status ?: ""
                        userRole = it.user.role ?: ""
                        userLocation = it.user.location ?: ""
                        userWebsite = it.user.website ?: ""
                        userSkills = it.user.skills ?: emptyList()
                        userFollowersCount = it.user.followers_count.toString()
                        userFollowingCount = it.user.following_count.toString()
                        isFollowing = it.is_following
                        isFollowedBy = it.is_followed_by

                        binding.tvName.text = userName
                        Glide
                            .with(baseContext)
                            .load(getString(R.string.base_url) + userPhotoUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_profile)
                            .into(binding.imgProfile)

                        binding.tvFollowersC.text = userFollowersCount
                        binding.tvFollowingC.text = userFollowingCount

                        val badgeAdapter = ProfileBadgeAdapter(userBadges, userId, userName, baseContext)
                        val layoutManager =
                            LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
                        binding.badges.layoutManager = layoutManager
                        binding.badges.adapter = badgeAdapter

                        if (userAbout.isEmpty()) {
                            binding.tvAbout.visibility = View.GONE
                        }
                        if (userLocation.isEmpty()) {
                            binding.tvLocation.visibility = View.GONE
                        }
                        if (userWebsite.isEmpty()) {
                            binding.tvWebsite.visibility = View.GONE
                        }
                        binding.tvAbout.text = userAbout
                        binding.tvLocation.text = userLocation
                        binding.tvWebsite.text = userWebsite

                        if (isFollowing && isFollowedBy) {
                            binding.textFollow.text = "Unfollow"
                            binding.textFollow.setTextColor(getColor(R.color.white))
                            binding.btnFollowBackground.backgroundTintList = ColorStateList.valueOf(
                                Color.parseColor("#181818")
                            )
                        } else if (isFollowing) {
                            binding.textFollow.text = "Unfollow"
                            binding.textFollow.setTextColor(getColor(R.color.white))
                            binding.btnFollowBackground.backgroundTintList = ColorStateList.valueOf(
                                Color.parseColor("#181818")
                            )
                        } else if (isFollowedBy) {
                            binding.textFollow.text = "Follow Back"
                            binding.textFollow.setTextColor(getColor(R.color.black))
                            binding.btnFollowBackground.backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(baseContext, R.color.white)
                            )
                        } else {
                            binding.textFollow.text = "Follow"
                            binding.textFollow.setTextColor(getColor(R.color.black))
                            binding.btnFollowBackground.backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(baseContext, R.color.white)
                            )
                        }
                    }

                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserAllDataResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
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
                Toast.makeText(this, "Unexpected Error: ${response.code()}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_share -> {
                shareProfileLink()
            }
            R.id.nav_block -> {
                Toast.makeText(this, "Block", Toast.LENGTH_SHORT).show()
            }

        }
        drawerLayout.closeDrawer(navView)
        return true
    }

    private fun shareProfileLink() {
        val shareableLink = getString(R.string.deep_link)+"user=$userId"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "See $userName's profile on Loop: $shareableLink")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }



    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun setFragment(fragment: Fragment) {
        transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null) // Add the transaction to the back stack if needed
        transaction.commit()
    }
}
