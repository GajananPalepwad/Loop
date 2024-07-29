package com.gn4k.loop.ui.post

import ApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gn4k.loop.adapters.CommentsAdapter
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityPostBinding
import com.gn4k.loop.models.StaticVariables
import com.gn4k.loop.models.request.LikeDislikeRequest
import com.gn4k.loop.models.request.MakeCommentRequest
import com.gn4k.loop.models.response.Comment
import com.gn4k.loop.models.response.FetchCommentsResponse
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.notificationModel.SaveNotificationInDB
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import com.gn4k.loop.ui.profile.self.ProfilePost
import com.google.ai.client.generativeai.GenerativeModel
import com.overflowarchives.linkpreview.ViewListener
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class ActivityPost : AppCompatActivity() {

    lateinit var binding: ActivityPostBinding

    var isLiked by Delegates.notNull<Boolean>()
    var likeCount by Delegates.notNull<Int>()
    var commentCount by Delegates.notNull<Int>()
    var authorId: String? = null

    lateinit var loading: CustomLoading


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = CustomLoading(this)
        loading.startLoading()

        val postType = intent.getStringExtra("post_type")
        val postLink = intent.getStringExtra("post_link")
        val userPhotoUrl = intent.getStringExtra("user_photo_url")
        val userName = intent.getStringExtra("user_name")
        val postTime = intent.getStringExtra("post_time")
        val postContext = intent.getStringExtra("post_context")
        isLiked = intent.getBooleanExtra("is_liked", false)
        likeCount = intent.getIntExtra("like_count", 0)
        commentCount = intent.getIntExtra("comment_count", 0)
        val postId = intent.getIntExtra("post_id", 0)
        authorId = intent.getStringExtra("author_id")
        val position = intent.getIntExtra("adapter_position", -1)

        fetchCommentsList(postId)

        binding.back.setOnClickListener {
            onBackPressed()
            finish()
        }

        when (postType) {
            "photo" -> {
                binding.postImage.visibility = View.VISIBLE
                Glide.with(baseContext)
                    .load(getString(R.string.base_url) + postLink)
                    .centerCrop()
                    .placeholder(R.drawable.post_placeholder)
                    .into(binding.postImage)
            }

            "code_snippet" -> {
                binding.codeContainer.visibility = View.VISIBLE
                binding.codeContainer.setText(postLink)
            }

            "link" -> {
                binding.linkPreview.visibility = View.VISIBLE
                val formattedUrl = postLink?.let { formatUrl(it) }
                binding.linkPreview.loadUrl(formattedUrl, object : ViewListener {
                    override fun onPreviewSuccess(status: Boolean) {}
                    override fun onFailedToLoad(e: Exception?) {}
                })
            }

            "only_caption" -> {
                // Handle only caption
            }
        }

        Glide.with(baseContext)
            .load(getString(R.string.base_url) + userPhotoUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(binding.profileImage)

        binding.username.text = userName
        binding.timeAgo.text = postTime
        binding.tvContext.text = postContext

        if (isLiked) {
            binding.btnLike.setImageResource(R.drawable.ic_red_heart)
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_heart)
        }

        binding.likes.text = likeCount.toString()
        binding.comments.text = commentCount.toString()

        binding.header.setOnClickListener {
            if (authorId != null) {
                if (authorId!!.toInt() == MainHome.USER_ID.toInt()) {
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

        binding.postImage.setOnClickListener {
            val intent = Intent(this, ViewImageInFull::class.java)
            intent.putExtra("image_url", postLink)
            startActivity(intent)
        }


        binding.btnLike.setOnClickListener {
            if (isLiked) {
                binding.btnLike.setImageResource(R.drawable.ic_heart)
                doUnlike(postId, true, position)
                isLiked = false
            } else {
                binding.btnLike.setImageResource(R.drawable.ic_red_heart)
                doLike(postId, false, position)
                isLiked = true
                SaveNotificationInDB().save(
                    baseContext,
                    MainHome.USER_ID.toInt(),
                    authorId!!.toInt(),
                    postId,
                    "likes",
                    "${MainHome.USER_NAME} liked your post"
                )
            }
        }

        binding.btnComment.setOnClickListener {
            if (binding.edComment.text.isNotEmpty()) {
                lifecycleScope.launch {
                    loading.startLoading()
                    geminiCheckComment(postId, binding.edComment.text.toString(), position)
                    binding.edComment.setText("")
                }
            }
        }

        binding.btnShare.setOnClickListener {
            sharePostLink(postId)
        }

        binding.imgComment.setOnClickListener {
            openKeyboard(this, binding.edComment)
        }


    }

    private fun sharePostLink(postId: Int) {
        val shareableLink = "https://loop.42web.io?post=$postId"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this post from Loop: $shareableLink")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }

    private suspend fun geminiCheckComment(postId: Int, comment: String, position: Int) {
        val generativeModel = GenerativeModel(
            modelName = getString(R.string.gemini_model),
            apiKey = MainHome.GEMINI_KEY
        )

        try {
            val response = generativeModel.generateContent(
                "Does the following text contain only appropriate content, free of misleading information, bad words, vulgarity, hate speech, or any other inappropriate content? Respond with only \"true\" if it is appropriate and \"false\" if it is not.\n" +
                        "\n" +
                        "\"$comment\"\n"
            )

            if (response.text.toString().lowercase().contains("true")) {
                doComment(postId, comment, position)
            } else {
                Toast.makeText(baseContext, "Inappropriate Content", Toast.LENGTH_SHORT).show()
                loading.stopLoading()
            }
        } catch (e: Exception) {
            Toast.makeText(baseContext, "Inappropriate Content", Toast.LENGTH_SHORT).show()
            loading.stopLoading()
        }

    }

    private fun openKeyboard(context: Context, editText: EditText) {
        editText.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun fetchCommentsList(postId: Int) {

        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.fetchComments(LikeDislikeRequest(postId, MainHome.USER_ID.toInt()))
            ?.enqueue(object : Callback<FetchCommentsResponse?> {
                override fun onResponse(
                    call: Call<FetchCommentsResponse?>,
                    response: Response<FetchCommentsResponse?>
                ) {
                    if (response.isSuccessful) {
                        val commentsResponse = response.body()
                        commentList = commentsResponse?.comments as MutableList<Comment>

                        adapter = commentList?.let { CommentsAdapter(it, baseContext) }!!
                        binding.commentsRecyclerView.layoutManager =
                            LinearLayoutManager(baseContext)
                        binding.commentsRecyclerView.adapter = adapter
                        loading.stopLoading()
                    } else {
//                    handleErrorResponse(response)
                        loading.stopLoading()
                    }
                }

                override fun onFailure(call: Call<FetchCommentsResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    loading.stopLoading()
                }
            })
    }

    private fun doComment(postId: Int, comment: String, position: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.doComment(MakeCommentRequest(postId, MainHome.USER_ID.toInt(), comment))
            ?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(
                    call: Call<UserResponse?>,
                    response: Response<UserResponse?>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()
                        if (!StaticVariables.isExplore) {
                            StaticVariables.postAdapter.notifyItemChanged(position)
                        }
                        val post = StaticVariables.posts[position]
                        post.commentCount += 1
                        commentCount++
                        binding.comments.text = commentCount.toString()
                        binding.edComment.setText("")
                        fetchCommentsList(postId)
                        SaveNotificationInDB().save(
                            baseContext,
                            MainHome.USER_ID.toInt(),
                            authorId!!.toInt(),
                            postId,
                            "comments",
                            "${MainHome.USER_NAME} commented on your post"
                        )
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

    private fun doLike(postId: Int, isLike: Boolean, position: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeRequest(postId, MainHome.USER_ID.toInt())

        apiService?.doLike(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    val post = StaticVariables.posts[position]

                    post.isLiked = true
                    post.likeCount += 1
                    likeCount++
                    binding.likes.text = likeCount.toString()


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

    private fun doUnlike(postId: Int, isLike: Boolean, position: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeRequest(postId, MainHome.USER_ID.toInt())

        apiService?.doUnlike(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    val post = StaticVariables.posts[position]

                    post.isLiked = false
                    post.likeCount -= 1
                    likeCount--
                    binding.likes.text = likeCount.toString()


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

    private fun formatUrl(url: String): String {
        return when {
            url.startsWith("http://") -> url.replace("http://", "https://")
            url.startsWith("https://") -> url
            else -> "https://$url"
        }
    }

    companion object {
        var commentList: MutableList<Comment> = mutableListOf()
        lateinit var adapter: CommentsAdapter
    }


}
