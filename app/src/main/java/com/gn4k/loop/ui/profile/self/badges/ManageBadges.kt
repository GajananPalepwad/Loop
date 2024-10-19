package com.gn4k.loop.ui.profile.self.badges

import com.gn4k.loop.api.ApiService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gn4k.loop.R
import com.gn4k.loop.adapters.BadgeAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityManageBadgesBinding
import com.gn4k.loop.models.response.Skill
import com.gn4k.loop.ui.home.MainHome
import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageBadges : AppCompatActivity() {

    lateinit var binding: ActivityManageBadgesBinding
    private lateinit var apiService: ApiService
    var tagListSpinner: List<String> = java.util.ArrayList()
    lateinit var spinnerDialog: SpinnerDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageBadgesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var badges = MainHome.USER_BADGES
        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")

        if (!userId.equals(MainHome.USER_ID)) {
            badges = intent.getStringArrayListExtra("badges") ?: ArrayList()
            binding.btnAddBadge.visibility = View.GONE
            binding.tvTitle.text = "$userName's Badges"
        }


        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)!!

        spinnerDialog = SpinnerDialog(
            this,
            ArrayList(),
            "Select or Search Subject",
            R.style.DialogAnimations_SmileWindow,
            "Close"
        )

        fetchSkills()


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = BadgeAdapter(this, badges)

        binding.back.setOnClickListener {
            onBackPressed()
            finish()
        }

        if (badges.isEmpty()) {
            binding.imgEmpty.visibility = RecyclerView.VISIBLE
        } else {
            binding.imgEmpty.visibility = RecyclerView.GONE
        }

        binding.btnAddBadge.setOnClickListener {
            spinnerDialog.showSpinerDialog()
        }

    }

    private fun fetchSkills() {

        apiService?.getSkills()?.enqueue(object : Callback<Skill> {
            override fun onResponse(call: Call<Skill>, response: Response<Skill>) {
                if (response.isSuccessful) {
                    val skillResponse = response.body()
                    val skills = skillResponse?.skills
                    if (skills != null) {
                        tagListSpinner = skills.map { it.skill }
                        spinnerDialog = SpinnerDialog(
                            this@ManageBadges,
                            tagListSpinner as ArrayList<String?>,
                            "Select or Search Skills",
                            R.style.DialogAnimations_SmileWindow,
                            "Close"
                        )

                        spinnerDialog.setCancellable(true)
                        spinnerDialog.setShowKeyboard(false)

                        spinnerDialog.bindOnSpinerListener { item, position ->
                            val intent = Intent(this@ManageBadges, ExamRulesAndInfo::class.java)
                            intent.putExtra("badge", item)
                            startActivity(intent)
                        }

                    } else {
                        Log.e(
                            "SkillSelector",
                            "Response not successful: ${response.errorBody()?.string()}"
                        )
                        Toast.makeText(
                            this@ManageBadges,
                            "Failed to fetch skills",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        "SkillSelector",
                        "Response not successful: ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(this@ManageBadges, "Failed to fetch skills", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Skill>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}