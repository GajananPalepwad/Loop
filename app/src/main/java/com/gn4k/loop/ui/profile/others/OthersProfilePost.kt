package com.gn4k.loop.ui.profile.others

import com.gn4k.loop.api.ApiService
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.PostAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentPostBinding
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.models.StaticVariables
import com.gn4k.loop.models.StaticVariables.Companion.posts
import com.gn4k.loop.models.request.GetPostsByAuthorRequest
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OthersProfilePost: Fragment() {

    lateinit var binding: FragmentPostBinding

    private var apiService: ApiService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(inflater, container, false)

        OthersProfile.loading.startLoading()

        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        getPostsByAuthor(OthersProfile.userId.toString())

        return binding.root
    }

    private fun getPostsByAuthor(authorId: String) {
        val request = GetPostsByAuthorRequest(author_id = authorId, MainHome.USER_ID)
        apiService?.getPostsByAuthor(request)?.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    posts = (response.body() as MutableList<Post>?)!!
                    if (posts != null) {

                        StaticVariables.postAdapter = activity?.let { PostAdapter(posts, OthersProfile.userName, OthersProfile.userPhotoUrl, it) }!!
                        binding.recyclerView.adapter = StaticVariables.postAdapter

                        if (posts!!.isEmpty()) {
                            binding.imgEmpty.visibility = View.VISIBLE
                        } else {
                            binding.imgEmpty.visibility = View.GONE
                        }
                        OthersProfile.loading.stopLoading()
                    } else {
                        Toast.makeText(activity, "No posts found", Toast.LENGTH_SHORT).show()
                        OthersProfile.loading.stopLoading()

                    }
                } else {
                    handleErrorResponse(response)
                    OthersProfile.loading.stopLoading()
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                Log.d("YourActivity", "Network Error: ${t.message}")
                Toast.makeText(activity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                OthersProfile.loading.stopLoading()
            }
        })
    }

    private fun handleErrorResponse(response: Response<List<Post>>) {
        when (response.code()) {
            400 -> {
                Log.d("YourActivity", "Bad Request: ${response.message()}")
                Toast.makeText(activity, "Bad Request: ${response.message()}", Toast.LENGTH_SHORT)
                    .show()
            }

            500 -> {
                Log.d("YourActivity", "Internal Server Error")
                Toast.makeText(activity, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Log.d("YourActivity", "Unexpected Error: ${response.message()}")
                Toast.makeText(
                    activity,
                    "Unexpected Error: ${response.message()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}