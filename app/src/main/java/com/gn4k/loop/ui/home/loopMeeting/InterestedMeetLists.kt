package com.gn4k.loop.ui.home.loopMeeting

import com.gn4k.loop.api.ApiService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.MeetingListInterestedAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentMeetListsBinding
import com.gn4k.loop.models.request.UserIdRequest
import com.gn4k.loop.models.response.MeetingResponse
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InterestedMeetLists: Fragment() {

    lateinit var binding: FragmentMeetListsBinding
    lateinit var adapter: MeetingListInterestedAdapter
    lateinit var loading: CustomLoading

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMeetListsBinding.inflate(layoutInflater, container, false)
        loading = CustomLoading(activity)
        loading.startLoading()

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fetchAllMeetings()
        return binding.root

    }


    private fun fetchAllMeetings() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchInterestedMeetings(UserIdRequest(MainHome.USER_ID.toInt()))
            ?.enqueue(object : Callback<MeetingResponse?> {
                override fun onResponse(
                    call: Call<MeetingResponse?>,
                    response: Response<MeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        val meetingList = meetingResponse?.meetings

                        adapter = meetingList?.let { context?.let { it1 ->
                            activity?.let { it2 ->
                                MeetingListInterestedAdapter(it,
                                    it1, it2.getString(R.string.videocall_key)
                                )
                            }
                        } }!!

                        binding.recyclerView.adapter = adapter

                        if(adapter.itemCount == 0){
                            binding.imgEmpty.visibility = View.VISIBLE
                        }
                        loading.stopLoading()

                    } else {
                        loading.stopLoading()
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<MeetingResponse?>, t: Throwable) {
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