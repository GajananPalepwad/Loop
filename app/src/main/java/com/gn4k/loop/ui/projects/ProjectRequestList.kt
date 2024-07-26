package com.gn4k.loop.ui.projects

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.AcceptedUserAdapter
import com.gn4k.loop.adapters.ImageAdapter
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


        val requestAdapter = requestPersons?.let { RequestUserAdapter(it, baseContext, project!!.toInt()) }
        binding.rcRequest.adapter = requestAdapter
        binding.rcRequest.layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)

        val joinedAdapter = joinedPersons?.let { AcceptedUserAdapter(it, baseContext) }
        binding.rcAccepted.adapter = joinedAdapter
        binding.rcAccepted.layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.VERTICAL, false)

    }
}