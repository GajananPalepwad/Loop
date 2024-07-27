package com.gn4k.loop.ui.projects

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.adapters.AcceptedUserAdapter
import com.gn4k.loop.adapters.RequestUserAdapter
import com.gn4k.loop.databinding.ActivityProjectRequestListBinding
import com.gn4k.loop.models.response.ParticipantList

class ProjectRequestList : AppCompatActivity() {
    lateinit var binding: ActivityProjectRequestListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectRequestListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val joinedPersons = intent.getSerializableExtra("joinedPersons") as? ArrayList<ParticipantList>
        val requestPersons = intent.getSerializableExtra("requestPersons") as? ArrayList<ParticipantList>
        val project = intent.getStringExtra("projectId")
        val authorId = intent.getStringExtra("authorId")

        binding.back.setOnClickListener {
            onBackPressed()
        }


        if (project != null && authorId != null) {
            val acceptedUserAdapter = AcceptedUserAdapter(joinedPersons ?: arrayListOf(), this, project.toInt(), authorId.toInt())
            val requestUserAdapter = RequestUserAdapter(requestPersons ?: arrayListOf(), this, project.toInt(), acceptedUserAdapter)

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