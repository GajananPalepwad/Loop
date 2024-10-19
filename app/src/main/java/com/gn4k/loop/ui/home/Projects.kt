package com.gn4k.loop.ui.home

import com.gn4k.loop.api.ApiService
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ProjectAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentProjectsBinding
import com.gn4k.loop.models.response.GetProjects
import com.gn4k.loop.models.response.Project
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.projects.MakeProject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Projects : Fragment() {

    lateinit var binding: FragmentProjectsBinding
    private lateinit var projectAdapter: ProjectAdapter
    private var projectList: List<Project> = listOf()
    var filter = "all"
    lateinit var loading: CustomLoading

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProjectsBinding.inflate(layoutInflater, container, false)
        loading = CustomLoading(activity)
        loading.startLoading()

        fetchAllProjects("all")

        binding.btnAddProject.setOnClickListener {
            val intent = Intent(requireContext(), MakeProject::class.java)
            startActivity(intent)
        }

        binding.btnAll.setOnClickListener {
            fetchAllProjects("all")
            loading.startLoading()
            filter = "all"
        }

        binding.btnYetToStart.setOnClickListener {
            fetchAllProjects("Yet to start")
            loading.startLoading()
            filter = "Yet to start"
        }

        binding.btnInProgress.setOnClickListener {
            fetchAllProjects("In progress")
            loading.startLoading()
            filter = "In progress"
        }

        binding.btnCompleted.setOnClickListener {
            fetchAllProjects("Completed")
            loading.startLoading()
            filter = "Completed"
        }

        binding.btnOnHold.setOnClickListener {
            fetchAllProjects("On hold")
            loading.startLoading()
            filter = "On hold"
        }

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        binding.refreshLayout.setOnRefreshListener {
            fetchAllProjects(filter)
            binding.searchBox.text.clear()
            loading.startLoading()
        }

        return binding.root
    }

    private fun fetchAllProjects(filter: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchAllProjects(filter)
            ?.enqueue(object : Callback<GetProjects?> {
                override fun onResponse(
                    call: Call<GetProjects?>,
                    response: Response<GetProjects?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                        projectList = msgResponse?.projects ?: listOf()
                        projectAdapter = ProjectAdapter(projectList, requireActivity())
                        binding.recyclerView.adapter = projectAdapter
                        binding.recyclerView.layoutManager = LinearLayoutManager(context)
                        binding.refreshLayout.isRefreshing = false

                        binding.imgEmpty.visibility = if (projectList.isEmpty()) View.VISIBLE else View.GONE
                        loading.stopLoading()
                    } else {
                        // handleErrorResponse(response)
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<GetProjects?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loading.stopLoading()
                }
            })
    }

    private fun filter(text: String) {
        val filteredList = projectList.filter {
            it.title.contains(text, ignoreCase = true) ||
                    it.description.contains(text, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(text, ignoreCase = true) }
        }
        projectAdapter.updateList(filteredList)
    }
}
