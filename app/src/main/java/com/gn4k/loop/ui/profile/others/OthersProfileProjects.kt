package com.gn4k.loop.ui.profile.others

import ApiService
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ProjectAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentProfileProjectsBinding
import com.gn4k.loop.models.response.GetProjects
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OthersProfileProjects: Fragment() {

    lateinit var binding: FragmentProfileProjectsBinding
    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileProjectsBinding.inflate(inflater, container, false)

        fetchAllProjects(OthersProfile.userId)

        return binding.root

    }

    private fun fetchAllProjects(id: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchProjectByAuthor(id)
            ?.enqueue(object : Callback<GetProjects?> {
                override fun onResponse(
                    call: Call<GetProjects?>,
                    response: Response<GetProjects?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                        val projects = msgResponse?.projects

                        projectAdapter = projects?.let {
                            activity?.let { it1 ->
                                ProjectAdapter(it, it1)
                            }
                        }!!

                        binding.recyclerView.adapter = projectAdapter
                        binding.recyclerView.layoutManager = LinearLayoutManager(context)

                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<GetProjects?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    companion object {

    }

}