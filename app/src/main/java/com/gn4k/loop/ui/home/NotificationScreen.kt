package com.gn4k.loop.ui.home

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
import com.gn4k.loop.adapters.MeetingListAdapter
import com.gn4k.loop.adapters.NotificationAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentNotificationScreenBinding
import com.gn4k.loop.models.response.MeetingResponse
import com.gn4k.loop.models.response.NotificationListResponse
import com.gn4k.loop.ui.animation.CustomLoading
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationScreen : Fragment() {

    lateinit var binding: FragmentNotificationScreenBinding
    lateinit var loading: CustomLoading
    var type: String = "all"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationScreenBinding.inflate(inflater, container, false)
        loading = CustomLoading(activity)
        loading.startLoading()
        fetchNotifications("all")

        binding.btnAll.setOnClickListener {
            fetchNotifications("all")
            type = "all"
        }
        binding.btnFollow.setOnClickListener {
            fetchNotifications("follows")
            type = "follows"
        }
        binding.btnLike.setOnClickListener {
            fetchNotifications("likes")
            type = "likes"
        }
        binding.btnComment.setOnClickListener {
            fetchNotifications("comments")
            type = "comments"
        }
        binding.btnProject.setOnClickListener {
            fetchNotifications("projects")
            type = "projects"
        }
        binding.btnOther.setOnClickListener {
            fetchNotifications("other")
            type = "other"
        }

        binding.refreshLayout.setOnRefreshListener {
            fetchNotifications(type)
            loading.startLoading()
        }


        return binding.root
    }

    private fun fetchNotifications(type: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchNotifications(MainHome.USER_ID.toInt(), type)
            ?.enqueue(object : Callback<NotificationListResponse?> {
                override fun onResponse(
                    call: Call<NotificationListResponse?>,
                    response: Response<NotificationListResponse?>
                ) {
                    if (response.isSuccessful) {
                        val notificationResponse = response.body()
                        val notificationList = notificationResponse?.notifications

                        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
                        val adapter = notificationList?.let { activity?.let { it1 ->
                            NotificationAdapter(it,
                                it1
                            )
                        } }
                        binding.recyclerView.adapter = adapter
                        binding.refreshLayout.isRefreshing = false
                        if (notificationList != null) {
                            if (notificationList.isEmpty()) {
                                binding.imgEmpty.visibility = View.VISIBLE
                            } else {
                                binding.imgEmpty.visibility = View.GONE
                            }
                        }
                        loading.stopLoading()
                    } else {
//                    handleErrorResponse(response)
                        binding.refreshLayout.isRefreshing = false
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<NotificationListResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    binding.refreshLayout.isRefreshing = false
                    loading.stopLoading()
                }
            })
    }

    companion object {

    }
}