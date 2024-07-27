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
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ConversationAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.FragmentChatListBinding
import com.gn4k.loop.models.StaticVariables.Companion.posts
import com.gn4k.loop.models.response.Conversation
import com.gn4k.loop.models.response.ConversationsResponse
import com.gn4k.loop.ui.animation.CustomLoading
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatList : Fragment() {

    lateinit var binding: FragmentChatListBinding
    private lateinit var conversationsList: MutableList<Conversation>
    private lateinit var adapter: ConversationAdapter
    lateinit var loading: CustomLoading

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatListBinding.inflate(layoutInflater, container, false)
        loading = CustomLoading(activity)
        loading.startLoading()
        setupRecyclerView()
        setupSearchBox()
        fetchConversationsList()

        binding.refreshLayout.setOnRefreshListener {
            fetchConversationsList()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchConversationsList()  // Fetch conversations when the fragment is resumed
    }

    private fun setupRecyclerView() {
        conversationsList = mutableListOf()
        adapter = context?.let { ConversationAdapter(conversationsList, it) }!!
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchBox() {
        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterConversations(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterConversations(query: String) {
        val filteredList = conversationsList.filter { conversation ->
            conversation.opposite_user_name.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredList)
    }

    private fun fetchConversationsList() {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchConversation(MainHome.USER_ID.toInt())
            ?.enqueue(object : Callback<ConversationsResponse?> {
                override fun onResponse(
                    call: Call<ConversationsResponse?>,
                    response: Response<ConversationsResponse?>
                ) {
                    if (response.isSuccessful) {
                        val conversationsResponse = response.body()
                        conversationsList = conversationsResponse?.conversations?.toMutableList() ?: mutableListOf()
                        binding.recyclerView.adapter = adapter
                        adapter.updateList(conversationsList)
                        binding.refreshLayout.isRefreshing = false

                        binding.imgEmpty.visibility = if (conversationsList.isEmpty()) View.VISIBLE else View.GONE
                        loading.stopLoading()

                    } else {
                        showErrorToast("Failed to fetch conversations")
                        binding.refreshLayout.isRefreshing = false
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<ConversationsResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    showErrorToast("Network Error: ${t.message}")
                    binding.refreshLayout.isRefreshing = false
                    loading.stopLoading()
                }
            })
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
