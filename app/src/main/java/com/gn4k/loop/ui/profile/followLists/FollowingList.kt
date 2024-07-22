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
import com.gn4k.loop.adapters.FollowingAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentFollowingListBinding
import com.gn4k.loop.models.request.UserRequest
import com.gn4k.loop.models.response.Following
import com.gn4k.loop.models.response.FollowingResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowingList : Fragment() {

    lateinit var binding: FragmentFollowingListBinding
    lateinit var followingList: List<Following>
    lateinit var adapter: FollowingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowingListBinding.inflate(layoutInflater, container, false)

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

    private fun fetchList() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)
        apiService?.getFollowingList(UserRequest(FollowList.userId, FollowList.userId))
            ?.enqueue(object : Callback<FollowingResponse?> {
                override fun onResponse(
                    call: Call<FollowingResponse?>,
                    response: Response<FollowingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val followResponse = response.body()
                        followingList = followResponse?.following!!
                        adapter = context?.let { FollowingAdapter(it, followingList) }!!
                        binding.recyclerView.layoutManager = LinearLayoutManager(context)
                        binding.recyclerView.adapter = adapter
                    }
                }

                override fun onFailure(call: Call<FollowingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun filterList(query: String) {
        val filteredList = if (query.isNotEmpty()) {
            followingList.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        } else {
            followingList
        }
        adapter.updateList(filteredList)
    }

    companion object {

    }
}