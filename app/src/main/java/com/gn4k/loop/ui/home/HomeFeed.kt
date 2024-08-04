package com.gn4k.loop.ui.home

import ApiService
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.adapters.PostAdapter
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ExploreAdapter
import com.gn4k.loop.adapters.HomeFeedAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentHomeFeedBinding
import com.gn4k.loop.models.StaticVariables.Companion.posts
import com.gn4k.loop.models.request.HomeFeedRequest
import com.gn4k.loop.models.request.MarkSeenPostRequest
import com.gn4k.loop.models.request.MarkSeenRequest
import com.gn4k.loop.models.response.ExploreResponse
import com.gn4k.loop.models.response.MarkSeenResponse
import com.gn4k.loop.ui.post.MakePost
import com.gn4k.loop.ui.profile.self.Profile
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.models.response.Posts
import com.gn4k.loop.ui.animation.CustomLoading
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class HomeFeed : Fragment() {

    private lateinit var binding: FragmentHomeFeedBinding
    lateinit var loading: CustomLoading

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        loading = CustomLoading(activity)
        loading.startLoading()

        binding.tvName.text = MainHome.USER_NAME

        posts = mutableListOf()

        activity?.let {
            Glide
                .with(it)
                .load(getString(R.string.base_url) + MainHome.USER_PHOTO_URL)
                .centerCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.imgProfile)
        }

        val greeting = getGreeting()
        binding.tvGreetings.text = greeting

        binding.btnProfile.setOnClickListener {
            val intent = Intent(activity, Profile::class.java)
            startActivity(intent)
        }

        binding.btnMakePost.setOnClickListener {
            val intent = Intent(activity, MakePost::class.java)
            startActivity(intent)
        }



        binding.btnNotifications.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.container, NotificationScreen()).addToBackStack(null).commit()
        }

        binding.btnSearch.setOnClickListener {
            MainHome.isSearch = true
            parentFragmentManager.beginTransaction().replace(R.id.container, Explore()).addToBackStack(null).commit()
        }


        binding.btnExplore.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.container, Explore()).addToBackStack(null).commit()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

        binding.nest.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (binding.nest.getChildAt(0).bottom <= scrollY + binding.nest.height) {
//                Toast.makeText(context, "We are at end", Toast.LENGTH_SHORT).show()
                fetchHomeFeed()
                loading.startLoading()
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            posts.clear()
            fetchHomeFeed()
            loading.startLoading()
        }

        fetchHomeFeed()
        return binding.root;
    }


    private fun fetchHomeFeed() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.getHomeFeed(HomeFeedRequest(MainHome.USER_ID.toInt(), 4))
            ?.enqueue(object : Callback<Posts?> {
                override fun onResponse(
                    call: Call<Posts?>,
                    response: Response<Posts?>
                ) {
                    if (response.isSuccessful) {
                        val homeFeedResponse = response.body()
                        if (homeFeedResponse != null) {
                            posts.addAll(homeFeedResponse.posts as MutableList<Post>)
                            var postIds: MutableList<Int> = mutableListOf()
                            if (homeFeedResponse.posts.size >= 1) {
                                postIds.add(homeFeedResponse.posts[0].postId.toInt())
                                if (homeFeedResponse.posts.size >= 2) {
                                    postIds.add(homeFeedResponse.posts[1].postId.toInt())
                                    if (homeFeedResponse.posts.size >= 3) {
                                        postIds.add(homeFeedResponse.posts[2].postId.toInt())
                                        if (homeFeedResponse.posts.size >= 4) {
                                            postIds.add(homeFeedResponse.posts[3].postId.toInt())
                                        }
                                    }
                                }
                            }
                            binding.refreshLayout.isRefreshing = false
                            markPostSeen(postIds)
                        }

                        var homeFeedAdapter = activity?.let { HomeFeedAdapter(posts, it) }
                        binding.recyclerView.adapter = homeFeedAdapter

                        if (homeFeedResponse != null) {
                            if (homeFeedResponse.posts.isEmpty()) {
                                binding.empty.visibility = View.VISIBLE
                            } else {
                                binding.empty.visibility = View.GONE
                            }
                        }
                        loading.stopLoading()
                    }else{
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<Posts?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
//                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    loading.stopLoading()
                }
            })
    }

    private fun markPostSeen(lastMsgIds: List<Int>) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.markPostSeen(MarkSeenPostRequest(MainHome.USER_ID.toInt(), lastMsgIds))
            ?.enqueue(object : Callback<MarkSeenResponse?> {
                override fun onResponse(
                    call: Call<MarkSeenResponse?>,
                    response: Response<MarkSeenResponse?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                    }
                }

                override fun onFailure(call: Call<MarkSeenResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun getGreeting(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    companion object {

    }
}



