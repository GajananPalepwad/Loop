package com.gn4k.loop.ui.home.loopmeeting

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
import com.gn4k.loop.adapters.MeetingListAdapter
import com.gn4k.loop.adapters.MessageAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentMeetListsBinding
import com.gn4k.loop.models.response.ChattingRespnse
import com.gn4k.loop.models.response.MeetingObject
import com.gn4k.loop.models.response.MeetingResponse
import com.gn4k.loop.ui.home.MainHome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllMeetLists : Fragment() {

    lateinit var binding: FragmentMeetListsBinding
    lateinit var adapter: MeetingListAdapter
    private var sampleToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiI5MGM5NzUxOS04YmI3LTQ4MGQtOTA5Ny05OWQzOWFiYjkwMjgiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTcyMTMwNTAzMSwiZXhwIjoxNzIxOTA5ODMxfQ.NNyJp7OsUwW6zEmKroOsDK6lIbO3Zqh2DlVLpRieBC4"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMeetListsBinding.inflate(layoutInflater, container, false)

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

        apiService?.fetchAllMeetings()
            ?.enqueue(object : Callback<MeetingResponse?> {
                override fun onResponse(
                    call: Call<MeetingResponse?>,
                    response: Response<MeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        val meetingList = meetingResponse?.meetings

                        adapter = meetingList?.let { context?.let { it1 ->
                            MeetingListAdapter(it,
                                it1, token = sampleToken
                            )
                        } }!!

                        binding.recyclerView.adapter = adapter

                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<MeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    companion object {

    }
}