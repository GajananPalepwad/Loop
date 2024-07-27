package com.gn4k.loop.ui.profile.followLists

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.FollowerAdapter
import com.gn4k.loop.adapters.FollowingAdapter
import com.gn4k.loop.adapters.SearchUserAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentFollowingListBinding
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.Follower
import com.gn4k.loop.models.response.FollowerResponse
import com.gn4k.loop.models.response.SearchUserResponse
import com.gn4k.loop.ui.animation.CustomLoading
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowerList : Fragment() {


    lateinit var binding: FragmentFollowingListBinding

    lateinit var followerList: MutableList<Follower>
    lateinit var adapter: FollowerAdapter
    lateinit var loading: CustomLoading

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowingListBinding.inflate(layoutInflater, container, false)
        loading = CustomLoading(activity)
        loading.startLoading()

        fetchList()

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterList(s.toString())
            }
        })

        return binding.root
    }

    private fun filterList(query: String) {
        val filteredList = if (query.isNotEmpty()) {
            followerList.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        } else {
            followerList
        }
        adapter.updateList(filteredList)
    }

    private fun
            fetchList() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)
        apiService?.getFollowersList(UserRequest(FollowList.userId, FollowList.userId))
            ?.enqueue(object : Callback<FollowerResponse?> {
                override fun onResponse(
                    call: Call<FollowerResponse?>,
                    response: Response<FollowerResponse?>
                ) {
                    if (response.isSuccessful) {
                        val followResponse = response.body()
                        followerList = followResponse?.followers?.toMutableList()!!
                        adapter = context?.let { FollowerAdapter(it, followerList) }!!
                        binding.recyclerView.layoutManager = LinearLayoutManager(context)
                        binding.recyclerView.adapter = adapter

                        binding.imgEmpty.visibility = if (followerList.isEmpty()) View.VISIBLE else View.GONE

                        loading.stopLoading()
                    } else {
                        // handleErrorResponse(response)
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<FollowerResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loading.stopLoading()
                }
            })
    }


    companion object {

    }
}