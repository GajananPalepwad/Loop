package com.gn4k.loop.ui.profile

import ApiService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.SkillsAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivitySkillSelectorBinding
import com.gn4k.loop.models.response.Skill
import com.gn4k.loop.ui.home.MainHome
import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SkillSelector : AppCompatActivity() {

    private lateinit var binding: ActivitySkillSelectorBinding

    private lateinit var apiService: ApiService

    private var selectedSkills = mutableSetOf<String>()
    private lateinit var skillsAdapter: SkillsAdapter

    var tagListSpinner: List<String> = java.util.ArrayList()
    lateinit var spinnerDialog: SpinnerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)!!

        selectedSkills = MainHome.USER_SKILLS.toMutableSet()

        // Initialize RecyclerView and Adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        skillsAdapter = SkillsAdapter(selectedSkills) { skill -> removeSkill(skill) }
        binding.recyclerView.adapter = skillsAdapter

        binding.back.setOnClickListener {
            onBackPressed()
        }

        // Initialize spinnerDialog with an empty list to avoid uninitialized property access exception
        spinnerDialog = SpinnerDialog(
            this,
            ArrayList(),
            "Select or Search Subject",
            R.style.DialogAnimations_SmileWindow,
            "Close"
        )

        fetchSkills()

        binding.btnAdd.setOnClickListener {
            spinnerDialog.showSpinerDialog()
        }

        binding.btnFinish.setOnClickListener {
            updateSkillsInDatabase()
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
                            this@SkillSelector,
                            tagListSpinner as ArrayList<String?>,
                            "Select or Search Skills",
                            R.style.DialogAnimations_SmileWindow,
                            "Close"
                        )

                        spinnerDialog.setCancellable(true)
                        spinnerDialog.setShowKeyboard(false)

                        spinnerDialog.bindOnSpinerListener { item, position ->
                            addSkill(item)
                        }
                    } else {
                        Log.e(
                            "SkillSelector",
                            "Response not successful: ${response.errorBody()?.string()}"
                        )
                        Toast.makeText(
                            this@SkillSelector,
                            "Failed to fetch skills",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        "SkillSelector",
                        "Response not successful: ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(this@SkillSelector, "Failed to fetch skills", Toast.LENGTH_SHORT)
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

    private fun addSkill(skill: String) {
        if (selectedSkills.add(skill)) {
            skillsAdapter.notifyItemInserted(selectedSkills.size - 1)
//            Toast.makeText(this, "$skill added", Toast.LENGTH_SHORT).show()
        } else {
//            Toast.makeText(this, "$skill already selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeSkill(skill: String) {
        val position = selectedSkills.indexOf(skill)
        if (selectedSkills.remove(skill)) {
            skillsAdapter.notifyItemRemoved(position)
//            Toast.makeText(this, "$skill removed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSkillsInDatabase() {
        // Create the request body
        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val userId = MainHome.USER_ID
        val skills = selectedSkills.joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")
        val requestBody = "user_id=$userId&skills=${skills}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url(getString(R.string.base_url)+"save_skills_in_user.php")
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        // Use OkHttpClient to make the request asynchronously
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Log.d("SkillSelector", "Network Error: ${e.message}")
                    Toast.makeText(this@SkillSelector, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
//                        Toast.makeText(baseContext, responseBody, Toast.LENGTH_SHORT).show()
                        println("Response Body: $responseBody")
                        MainHome.USER_SKILLS = selectedSkills.toList()
                        startActivity(Intent(baseContext, MainHome::class.java))
                    }
                } else {
                    runOnUiThread {
                        Log.e("SkillSelector", "Request failed with code: ${response.code}")
                        Toast.makeText(this@SkillSelector, "Request failed with code: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}
