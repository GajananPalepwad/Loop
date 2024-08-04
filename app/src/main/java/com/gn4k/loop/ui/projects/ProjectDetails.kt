package com.gn4k.loop.ui.projects

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.AcceptedUserAdapter
import com.gn4k.loop.adapters.ProfileBadgeAdapter
import com.gn4k.loop.adapters.RequestUserAdapter
import com.gn4k.loop.databinding.ActivityProjectDetailsBinding
import com.gn4k.loop.models.response.ParticipantList

class ProjectDetails : AppCompatActivity() {
    lateinit var binding: ActivityProjectDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val joinedPersons = intent.getSerializableExtra("joinedPersons") as? ArrayList<ParticipantList>
        val requestPersons = intent.getSerializableExtra("requestPersons") as? ArrayList<ParticipantList>
        val project = intent.getStringExtra("projectId")
        val authorId = intent.getStringExtra("authorId")
        val tagList = intent.getStringArrayListExtra("tags")
        binding.tvTitle.text = intent.getStringExtra("title")
        binding.tvDescription.text = intent.getStringExtra("description")
        binding.tvStatus.text = intent.getStringExtra("status")

        when (intent.getStringExtra("status")) {
            "Yet to start" -> binding.tvStatus.setTextColor(getColor(R.color.green))
            "In progress" -> binding.tvStatus.setTextColor(getColor(R.color.app_color))
            "Completed" -> binding.tvStatus.setTextColor(getColor(R.color.red))
            "On hold" -> binding.tvStatus.setTextColor(getColor(R.color.white))
        }


        val badgeAdapter =
            tagList?.let { intent.getStringExtra("title")
                ?.let { it1 -> ProfileBadgeAdapter(it, authorId.toString().toInt(), it1, this) } }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcTags.layoutManager = layoutManager
        binding.rcTags.adapter = badgeAdapter


        binding.back.setOnClickListener {
            onBackPressed()
        }


        if (project != null && authorId != null) {
            val acceptedUserAdapter = AcceptedUserAdapter(joinedPersons ?: arrayListOf(), this, project.toInt(), authorId.toInt())
            val requestUserAdapter = RequestUserAdapter(requestPersons ?: arrayListOf(), this, project.toInt(), acceptedUserAdapter, authorId.toInt())

            binding.rcRequest.adapter = requestUserAdapter
            binding.rcRequest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            if (requestUserAdapter.itemCount == 0) {
                binding.imgEmpty.visibility = View.VISIBLE
            }else{
                binding.imgEmpty.visibility = View.GONE
            }

            binding.rcAccepted.adapter = acceptedUserAdapter
            binding.rcAccepted.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
    }
}