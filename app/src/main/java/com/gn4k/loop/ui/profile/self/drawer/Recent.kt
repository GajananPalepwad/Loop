package com.gn4k.loop.ui.profile.self.drawer

import ApiService
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.RecentPostAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityRecentBinding
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Recent : AppCompatActivity() {
    lateinit var binding: ActivityRecentBinding
    lateinit var loading: CustomLoading
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type")

        loading = CustomLoading(this)
        loading.startLoading()

        binding.back.setOnClickListener {
            onBackPressed()
        }

        if (type == "liked") {
            fetchRecentLiked()
        } else if (type == "commented") {
            fetchRecentCommented()
        }

    }

    private fun fetchRecentLiked() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchRecentLikes()
            ?.enqueue(object : Callback<List<Post>?> {
                override fun onResponse(
                    call: Call<List<Post>?>,
                    response: Response<List<Post>?>
                ) {
                    if (response.isSuccessful) {
                        val postResponse = response.body()?.toMutableList()

                        for (post in postResponse!!) {
                            post.isLiked = true
                        }

                        val adapter = RecentPostAdapter(postResponse, this@Recent)
                        binding.recyclerView.layoutManager = LinearLayoutManager(baseContext)
                        binding.recyclerView.adapter = adapter

                        loading.stopLoading()
                    } else {
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<List<Post>?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loading.stopLoading()
                }
            })
    }

    private fun fetchRecentCommented() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchRecentComments()
            ?.enqueue(object : Callback<List<Post>?> {
                override fun onResponse(
                    call: Call<List<Post>?>,
                    response: Response<List<Post>?>
                ) {
                    if (response.isSuccessful) {
                        val postResponse = response.body()?.toMutableList()

                        for (post in postResponse!!) {
                            post.isLiked =
                                post.liked_by.toString().contains(", ${MainHome.USER_ID}]") ||
                                        post.liked_by.toString()
                                            .contains("[${MainHome.USER_ID},") ||
                                        post.liked_by.toString().contains(", ${MainHome.USER_ID},")
                        }

                        val adapter = RecentPostAdapter(postResponse, this@Recent)
                        binding.recyclerView.layoutManager = LinearLayoutManager(baseContext)
                        binding.recyclerView.adapter = adapter

                        loading.stopLoading()
                    } else {
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<List<Post>?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loading.stopLoading()
                }
            })
    }
}