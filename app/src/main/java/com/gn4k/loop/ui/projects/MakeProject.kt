package com.gn4k.loop.ui.projects

import ApiService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.SkillsAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityMakeProjectBinding
import com.gn4k.loop.models.response.Skill
import com.overflowarchives.linkpreview.ViewListener
import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MakeProject : AppCompatActivity() {

    lateinit var binding: ActivityMakeProjectBinding

    lateinit var apiService: ApiService

    private val selectedSkills = mutableSetOf<String>()
    private lateinit var skillsAdapter: SkillsAdapter
    var tagListSpinner: List<String> = java.util.ArrayList()
    lateinit var spinnerDialog: SpinnerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)!!

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        skillsAdapter = SkillsAdapter(selectedSkills) { skill -> removeSkill(skill) }
        binding.recyclerView.adapter = skillsAdapter


        binding.edStatus.setOnClickListener {
            binding.spinner.performClick()
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        val statusOptions = arrayOf("In Progress", "Completed", "Pending")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        binding.spinner.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                 binding.edStatus.setText(statusOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Initialize spinnerDialog with an empty list to avoid uninitialized property access exception
        spinnerDialog = SpinnerDialog(
            this,
            ArrayList(),
            "Select or Search Subject",
            R.style.DialogAnimations_SmileWindow,
            "Close"
        )

        fetchSkills()

        binding.edLink.addTextChangedListener (object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val formattedUrl = formatUrl(p0.toString())
                binding.linkPreview.loadUrl(formattedUrl, object : ViewListener {
                    override fun onPreviewSuccess(status: Boolean) {}
                    override fun onFailedToLoad(e: Exception?) {}
                })
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.btnAdd.setOnClickListener {
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
                            this@MakeProject,
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
                            baseContext,
                            "Failed to fetch skills",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        "SkillSelector",
                        "Response not successful: ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(baseContext, "Failed to fetch skills", Toast.LENGTH_SHORT)
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

    private fun formatUrl(url: String): String {
        return when {
            url.startsWith("http://") -> url.replace("http://", "https://")
            url.startsWith("https://") -> url
            else -> "https://$url"
        }
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

}

