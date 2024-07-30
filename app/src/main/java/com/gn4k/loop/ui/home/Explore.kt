package com.gn4k.loop.ui.home

import ApiService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ExploreAdapter
import com.gn4k.loop.adapters.SearchUserAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentExploreBinding
import com.gn4k.loop.models.StaticVariables.Companion.posts
import com.gn4k.loop.models.response.ExploreResponse
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.models.response.SearchUser
import com.gn4k.loop.models.response.SearchUserResponse
import com.gn4k.loop.ui.animation.CustomLoading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Explore : Fragment() {

    lateinit var binding: FragmentExploreBinding
    lateinit var userList: MutableList<SearchUser>
    private var debounceJob: Job? = null

    lateinit var loading: CustomLoading

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExploreBinding.inflate(layoutInflater, container, false)
        loading = CustomLoading(activity)

        if(MainHome.isSearch){
            binding.searchBox.requestFocus()
            MainHome.isSearch = false
        }

        loading.startLoading()
        posts = mutableListOf()

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(binding.searchBox.text.isEmpty()){
                    binding.searchRecyclerView.visibility = View.GONE
                    binding.exploreRecyclerView.visibility = View.VISIBLE
                }else{
                    binding.searchRecyclerView.visibility = View.VISIBLE
                    binding.exploreRecyclerView.visibility = View.GONE
                }

                debounceJob?.cancel()
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300) // Debounce delay
                    fetchSearchList(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.nest.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (binding.nest.getChildAt(0).bottom <= scrollY + binding.nest.height) {
                fetchExploreList()
                loading.startLoading()
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            posts.clear()
            fetchExploreList()
        }

        fetchExploreList()
        return binding.root
    }


    private fun fetchExploreList() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchExplore(MainHome.USER_ID.toInt())
            ?.enqueue(object : Callback<ExploreResponse?> {
                override fun onResponse(
                    call: Call<ExploreResponse?>,
                    response: Response<ExploreResponse?>
                ) {
                    if (response.isSuccessful) {
                        val exploreResponse = response.body()

                        if (exploreResponse != null) {
                            posts.addAll(exploreResponse.posts as MutableList<Post>)
                        }
//                        posts = exploreResponse?.posts as MutableList<Post>

                        val adapter = context?.let { ExploreAdapter(posts, it) }
                        binding.exploreRecyclerView.layoutManager = GridLayoutManager(context, 2)
                        binding.exploreRecyclerView.adapter = adapter
                        binding.refreshLayout.isRefreshing = false

                        loading.stopLoading()

                    }
                }

                override fun onFailure(call: Call<ExploreResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loading.stopLoading()
                }
            })
    }


    private fun fetchSearchList(q: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)
        apiService?.searchUser(q)
            ?.enqueue(object : Callback<SearchUserResponse?> {
                override fun onResponse(
                    call: Call<SearchUserResponse?>,
                    response: Response<SearchUserResponse?>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        userList = userResponse?.results?.toMutableList()!!
                        val adapter = context?.let { SearchUserAdapter(userList, it) }
                        binding.searchRecyclerView.layoutManager = LinearLayoutManager(context)
                        binding.searchRecyclerView.adapter = adapter
                    }
                }

                override fun onFailure(call: Call<SearchUserResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    companion object {

    }
}