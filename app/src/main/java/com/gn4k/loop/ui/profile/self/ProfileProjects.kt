package com.gn4k.loop.ui.profile.self

import ApiService
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ProfileProjectAdapter
import com.gn4k.loop.adapters.ProjectAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentProfileProjectsBinding
import com.gn4k.loop.models.response.GetProjects
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileProjects : Fragment() {

    lateinit var binding: FragmentProfileProjectsBinding
    private lateinit var projectAdapter: ProfileProjectAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileProjectsBinding.inflate(inflater, container, false)

        Profile.loading.startLoading()

        fetchAllProjects()

        return binding.root

    }

    private fun fetchAllProjects() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchProjectByAuthor(MainHome.USER_ID.toInt())
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
                                ProfileProjectAdapter(it, it1)
                            }
                        }!!

                        binding.recyclerView.adapter = projectAdapter
                        binding.recyclerView.layoutManager = LinearLayoutManager(context)

                        if (projects.isEmpty()) {
                            binding.imgEmpty.visibility = View.VISIBLE
                        } else {
                            binding.imgEmpty.visibility = View.GONE
                        }

                        Profile.loading.stopLoading()

                    } else {
//                    handleErrorResponse(response)
                        Profile.loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<GetProjects?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    Profile.loading.stopLoading()
                }
            })
    }

    companion object {

    }
}

