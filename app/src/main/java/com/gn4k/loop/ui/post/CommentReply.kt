package com.gn4k.loop.ui.post

import ApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ReplyAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityCommentReplyBinding
import com.gn4k.loop.models.StaticVariables
import com.gn4k.loop.models.request.LikeDislikeCommentRequest
import com.gn4k.loop.models.request.Question
import com.gn4k.loop.models.request.ReplyComment
import com.gn4k.loop.models.response.Reply
import com.gn4k.loop.models.response.ReplyListResponse
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import com.gn4k.loop.ui.profile.self.ProfilePost
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class CommentReply : AppCompatActivity() {

    lateinit var binding: ActivityCommentReplyBinding

    var isLiked by Delegates.notNull<Boolean>()
    var likeCount by Delegates.notNull<Int>()
    var commentCount by Delegates.notNull<Int>()
    lateinit var replyList: MutableList<Reply>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentReplyBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val userPhotoUrl = intent.getStringExtra("user_photo_url")
        val postTime = intent.getStringExtra("post_time")
        val postContext = intent.getStringExtra("post_context")
        isLiked = intent.getBooleanExtra("is_liked", false)
        likeCount = intent.getIntExtra("like_count", 0)
        commentCount = intent.getIntExtra("comment_count", 0)
        val commentId = intent.getIntExtra("comment_id", 0)
        val authorId = intent.getStringExtra("author_id")
        val position = intent.getIntExtra("adapter_position", -1)

        fetchReplyList(commentId)

        Glide.with(baseContext)
            .load(getString(R.string.base_url) + userPhotoUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(binding.profileImage)

        binding.username.text = MainHome.USER_NAME
        binding.timeAgo.text = postTime
        binding.tvReply.text = postContext

        binding.header.setOnClickListener {
            if (authorId != null) {
                if (authorId.toInt() == MainHome.USER_ID.toInt()) {
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("userId", authorId)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, OthersProfile::class.java)
                    intent.putExtra("userId", authorId)
                    startActivity(intent)
                }
            }
        }

        if (isLiked == true) {
            binding.btnLike.setImageResource(R.drawable.ic_red_heart)
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_heart)
        }

        binding.likes.text = likeCount.toString()
        binding.comments.text = commentCount.toString()

        binding.btnReply.setOnClickListener {
            if (binding.edReply.text.isNotEmpty()) {
                lifecycleScope.launch {
                    geminiCheckReply(commentId, binding.edReply.text.toString(), position)
                }
                binding.edReply.setText("")
            }
        }

        binding.btnLike.setOnClickListener {
            doLikeAndUnlikeComment(commentId, position)
        }

        binding.btnFocus.setOnClickListener {
            openKeyboard(this, binding.edReply)
        }

    }

    private suspend fun geminiCheckReply(commentId: Int, reply: String, position: Int) {
        val generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
            modelName = getString(R.string.gemini_model),
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = getString(R.string.gemini_key)
        )

        try {
            val response = generativeModel.generateContent(
                "Does the following text contain only appropriate content, free of misleading information, bad words, vulgarity, hate speech, or any other inappropriate content? Respond with only \"true\" if it is appropriate and \"false\" if it is not.\n" +
                        "\n" +
                        "\"$reply\"\n"
            )

            if (response.text.toString().lowercase().contains("true")) {
                doReply(commentId, reply, position)
            } else {
                Toast.makeText(baseContext, "Inappropriate Content", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception) {
            Toast.makeText(baseContext, "Inappropriate Content", Toast.LENGTH_SHORT).show()
        }

    }


    private fun fetchReplyList(commentId: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchReply(commentId, MainHome.USER_ID.toInt())
            ?.enqueue(object : Callback<ReplyListResponse?> {
                override fun onResponse(
                    call: Call<ReplyListResponse?>,
                    response: Response<ReplyListResponse?>
                ) {
                    if (response.isSuccessful) {
                        val commentsResponse = response.body()
                        replyList = commentsResponse?.replies as MutableList<Reply>

                        val adapter = replyList?.let { ReplyAdapter(it, baseContext, binding) }
                        binding.ReplyRecyclerView.layoutManager = LinearLayoutManager(baseContext)
                        binding.ReplyRecyclerView.adapter = adapter

                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<ReplyListResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    private fun doLikeAndUnlikeComment(commentId: Int, position: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeCommentRequest(commentId, MainHome.USER_ID.toInt())

        apiService?.likeDislikeComment(likeDislikeRequest)
            ?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(
                    call: Call<UserResponse?>,
                    response: Response<UserResponse?>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        val comment = ActivityPost.commentList[position]

                        if (!comment.liked) {
                            comment.liked = true
                            comment.like_count += 1
                            binding.likes.text = comment.like_count.toString()
                            binding.btnLike.setImageResource(R.drawable.ic_red_heart)
                        } else {
                            comment.liked = false
                            comment.like_count -= 1
                            binding.likes.text = comment.like_count.toString()
                            binding.btnLike.setImageResource(R.drawable.ic_heart)
                        }

                        ActivityPost.adapter.notifyItemChanged(position)
                    } else {
                        handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    private fun doReply(commentId: Int, reply: String, position: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.doReply(ReplyComment(commentId, MainHome.USER_ID.toInt(), reply))
            ?.enqueue(object :
                Callback<UserResponse?> {
                override fun onResponse(
                    call: Call<UserResponse?>,
                    response: Response<UserResponse?>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        val comment = ActivityPost.commentList[position]

                        comment.comment_count += 1
                        commentCount++
                        binding.comments.text = commentCount.toString()
                        binding.edReply.setText("")
                        fetchReplyList(commentId)

                        if (!StaticVariables.isExplore) {
                            StaticVariables.postAdapter.notifyItemChanged(position)
                        }
                    } else {
                        handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun handleErrorResponse(response: Response<UserResponse?>) {
        when (response.code()) {
            405 -> {
                Log.d("Reg", "Invalid request method")
            }

            500 -> {
                Log.d("Reg", "Database connection failed")
                Toast.makeText(baseContext, "Database connection failed", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Log.d("Reg", "Unexpected Error: ${response.message()}")
                Toast.makeText(
                    baseContext,
                    "Unexpected Error: ${response.code()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        fun openKeyboard(context: Context, editText: EditText) {
            editText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

