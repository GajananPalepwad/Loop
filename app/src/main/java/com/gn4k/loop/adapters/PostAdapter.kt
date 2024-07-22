package com.gn4k.loop.adapters

import ApiService
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.colormoon.readmoretextview.ReadMoreTextView
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.StaticVariables
import com.gn4k.loop.ui.post.ActivityPost
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.models.request.LikeDislikeRequest
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import com.overflowarchives.linkpreview.TelegramPreview
import com.overflowarchives.linkpreview.ViewListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PostAdapter(private val postList: MutableList<Post>, private val userName: String, private val profileUrl: String, private val activity: Activity) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item: LinearLayout = itemView.findViewById(R.id.item)
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val timeAgo: TextView = itemView.findViewById(R.id.timeAgo)
        val tvContext: ReadMoreTextView = itemView.findViewById(R.id.tvContext)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val codeContainer: EditText = itemView.findViewById(R.id.codeContainer)
        val linkPreview: TelegramPreview = itemView.findViewById(R.id.link_preview)
        val likes: TextView = itemView.findViewById(R.id.likes)
        val comments: TextView = itemView.findViewById(R.id.comments)
        val header: LinearLayout = itemView.findViewById(R.id.header)
        val btnOptions: ImageView = itemView.findViewById(R.id.btnOptions)
        val btnLikes: ImageView = itemView.findViewById(R.id.btnLike)
        val btnComments: ImageView = itemView.findViewById(R.id.btnComment)
        val btnShares: ImageView = itemView.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        when (post.type) {
            "photo" -> {
                holder.postImage.visibility = View.VISIBLE
                Glide.with(activity)
                    .load(activity.getString(R.string.base_url) + post.link)
                    .centerCrop()
                    .placeholder(R.drawable.post_placeholder)
                    .into(holder.postImage)
            }
            "code_snippet" -> {
                holder.codeContainer.visibility = View.VISIBLE
                holder.codeContainer.setText(post.link)
            }
            "link" -> {
                holder.linkPreview.visibility = View.VISIBLE
                val formattedUrl = post.link?.let { formatUrl(it) }
                holder.linkPreview.loadUrl(formattedUrl, object : ViewListener {
                    override fun onPreviewSuccess(status: Boolean) {}
                    override fun onFailedToLoad(e: Exception?) {}
                })
            }
            "only_caption" -> {
                // Handle only caption
            }
        }

        Glide.with(activity)
            .load(activity.getString(R.string.base_url) + profileUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(holder.profileImage)

        holder.username.text = userName
        holder.timeAgo.text = timeAgo(post.time)
        holder.tvContext.text = post.context

        holder.tvContext.setCollapsedTextColor(R.color.app_color)
        holder.tvContext.setExpandedTextColor(R.color.app_color)

        if (post.isLiked == true) {
            holder.btnLikes.setImageResource(R.drawable.ic_red_heart)
        } else {
            holder.btnLikes.setImageResource(R.drawable.ic_heart)
        }

        holder.likes.text = post.likeCount.toString()
        holder.comments.text = post.commentCount.toString()

        holder.header.setOnClickListener {
            if(post.authorId.toInt()== MainHome.USER_ID.toInt()){
                val intent = Intent(activity, Profile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", post.authorId)
                activity.startActivity(intent)
            }else {
                val intent = Intent(activity, OthersProfile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", post.authorId)
                activity.startActivity(intent)
            }
        }

        holder.item.setOnClickListener {

            StaticVariables.isExplore = false

            val intent = Intent(activity, ActivityPost::class.java)
            intent.putExtra("post_type", post.type)
            intent.putExtra("post_link", post.link)
            intent.putExtra("user_photo_url", profileUrl)
            intent.putExtra("user_name", userName)
            intent.putExtra("post_time", timeAgo(post.time))
            intent.putExtra("post_context", post.context)
            intent.putExtra("is_liked", post.isLiked)
            intent.putExtra("like_count", post.likeCount.toInt())
            intent.putExtra("comment_count", post.commentCount.toInt())
            intent.putExtra("post_id", post.postId.toInt())
            intent.putExtra("author_id", post.authorId)
            intent.putExtra("adapter_position", position.toInt())
            activity.startActivity(intent)

        }


        holder.btnLikes.setOnClickListener {
            if (post.isLiked == true) {
                holder.btnLikes.setImageResource(R.drawable.ic_heart)
                doUnlike(post.postId.toInt(), false, holder, position)
            } else {
                holder.btnLikes.setImageResource(R.drawable.ic_red_heart)
                doLike(post.postId.toInt(), true, holder, position)
            }
        }
    }

    private fun doLike(postId: Int, isLike: Boolean, holder: PostViewHolder, position: Int) {
        val BASE_URL = activity.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeRequest(postId, MainHome.USER_ID.toInt())

        apiService?.doLike(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    val post = postList[position]

                    if (isLike) {
                        post.isLiked = true
                        post.likeCount += 1
                    } else {
                        post.isLiked = false
                        post.likeCount -= 1
                    }

                    notifyItemChanged(position)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(activity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun doUnlike(postId: Int, isLike: Boolean, holder: PostViewHolder, position: Int) {
        val BASE_URL = activity.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeRequest(postId, MainHome.USER_ID.toInt())

        apiService?.doUnlike(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    val post = postList[position]

                    if (isLike) {
                        post.isLiked = true
                        post.likeCount += 1
                    } else {
                        post.isLiked = false
                        post.likeCount -= 1
                    }

                    notifyItemChanged(position)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(activity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(activity, "Database connection failed", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.d("Reg", "Unexpected Error: ${response.message()}")
                Toast.makeText(activity, "Unexpected Error: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return postList.size
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
}
