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
import com.gn4k.loop.models.request.GetPostByIdRequest
import com.gn4k.loop.models.request.LikeDislikeRequest
import com.gn4k.loop.models.request.MakeCommentRequest
import com.gn4k.loop.models.response.Comment
import com.gn4k.loop.models.response.FetchCommentsResponse
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.notificationModel.SaveNotificationInDB
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import com.google.ai.client.generativeai.GenerativeModel
import com.overflowarchives.linkpreview.ViewListener
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates

class DeepLinkPost : AppCompatActivity() {

    lateinit var binding: ActivityPostBinding

    var isLiked by Delegates.notNull<Boolean>()
    var likeCount by Delegates.notNull<Int>()
    var commentCount by Delegates.notNull<Int>()

    lateinit var postType: String
    lateinit var postLink: String
    lateinit var userPhotoUrl: String
    lateinit var userName: String
    lateinit var postTime: String
    lateinit var postContext: String
    lateinit var authorId: String

//    var postId by Delegates.notNull<Int>()

    lateinit var loading: CustomLoading


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = CustomLoading(this)
        loading.startLoading()

        val postId = intent.getIntExtra("postId", 0)

        getPostsByAuthor(postId)

        binding.back.setOnClickListener {
            onBackPressed()
            finish()
        }


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
        binding.postImage.setOnClickListener {
            val intent = Intent(this, ViewImageInFull::class.java)
            intent.putExtra("image_url", postLink)
            startActivity(intent)
        }

        binding.btnLike.setOnClickListener {
            if (isLiked) {
                binding.btnLike.setImageResource(R.drawable.ic_heart)
                doUnlike(postId)
                isLiked = false
            } else {
                binding.btnLike.setImageResource(R.drawable.ic_red_heart)
                doLike(postId)
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
                    geminiCheckComment(postId, binding.edComment.text.toString())
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
        val shareableLink = getString(R.string.deep_link)+"post=$postId"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this post from Loop: $shareableLink")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }


    private fun getPostsByAuthor(postId: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val request = GetPostByIdRequest(post_id = postId, MainHome.USER_ID.toInt())
        apiService?.getPostById(request)?.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    val post = response.body()
                    if (post != null) {

                        postType = post.type
                        postLink = post.link.toString()
                        userPhotoUrl = post.author_photo_url.toString()
                        userName = post.author_name
                        postTime = post.time
                        postContext = post.context
                        isLiked = post.isLiked == false
                        likeCount = post.likeCount
                        commentCount = post.commentCount
                        authorId = post.authorId


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
                        binding.timeAgo.text = timeAgo(postTime)
                        binding.tvContext.text = postContext

                        if (isLiked) {
                            binding.btnLike.setImageResource(R.drawable.ic_red_heart)
                        } else {
                            binding.btnLike.setImageResource(R.drawable.ic_heart)
                        }

                        binding.likes.text = likeCount.toString()
                        binding.comments.text = commentCount.toString()

                        fetchCommentsList(postId)


                    } else {
                        Toast.makeText(baseContext, "No posts found", Toast.LENGTH_SHORT).show()
                    }
                } else {
//                    handleErrorResponse(response)

                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                Log.d("YourActivity", "Network Error: ${t.message}")
                Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()

            }
        })
    }

    private suspend fun
            geminiCheckComment(postId: Int, comment: String) {
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
                doComment(postId, comment)
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

    private fun doComment(postId: Int, comment: String) {
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

    private fun doLike(postId: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeRequest(postId, MainHome.USER_ID.toInt())

        apiService?.doLike(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    likeCount++
                    binding.likes.text = likeCount.toString()

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

    private fun doUnlike(postId: Int) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeRequest(postId, MainHome.USER_ID.toInt())

        apiService?.doUnlike(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    likeCount--
                    binding.likes.text = likeCount.toString()

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

    private fun timeAgo(dateTimeStr: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateTimeStr, formatter)
        val now = LocalDateTime.now()
        val duration = Duration.between(dateTime, now)

        return when {
            duration.seconds < 60 -> "${duration.seconds} seconds ago"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            duration.toDays() < 30 -> "${duration.toDays() / 7} weeks ago"
            duration.toDays() < 365 -> "${duration.toDays() / 30} months ago"
            else -> "${duration.toDays() / 365} years ago"
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
        lateinit var commentList: MutableList<Comment>
        lateinit var adapter: CommentsAdapter
    }


}
