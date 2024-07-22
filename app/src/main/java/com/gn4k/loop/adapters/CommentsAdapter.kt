package com.gn4k.loop.adapters

import ApiService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.LikeDislikeCommentRequest
import com.gn4k.loop.models.response.Comment
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.post.ActivityPost
import com.gn4k.loop.ui.post.CommentReply
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CommentsAdapter(private val commentsList: List<Comment>, private val activity: Context) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentsList[position]
        Glide.with(activity)
            .load(activity.getString(R.string.base_url) + comment.author_photo_url)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(holder.profileImage)

        if (comment.liked) {
            holder.btnLike.setImageResource(R.drawable.ic_red_heart)
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_heart)
        }

        holder.btnLike.setOnClickListener {
            doLikeAndUnlike(comment.id, holder, position)
        }

        holder.username.text = comment.author_name
        holder.timeAgo.text = timeAgo(comment.created_at)
        holder.commentText.text = comment.comment_text
        holder.likeCount.text = comment.like_count.toString()
        holder.commentCount.text = comment.comment_count.toString()

        holder.header.setOnClickListener {
            if(comment.author_id== MainHome.USER_ID.toInt()){
                val intent = Intent(activity, Profile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", comment.author_id.toString())
                activity.startActivity(intent)
            }else {
                val intent = Intent(activity, OthersProfile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", comment.author_id.toString())
                activity.startActivity(intent)
            }
        }

        holder.item.setOnClickListener {
            val intent = Intent(activity, CommentReply::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("user_photo_url", comment.author_photo_url)
            intent.putExtra("post_time", timeAgo(comment.created_at))
            intent.putExtra("post_context", comment.comment_text)
            intent.putExtra("is_liked", comment.liked)
            intent.putExtra("like_count", comment.like_count.toInt())
            intent.putExtra("comment_count", comment.comment_count.toInt())
            intent.putExtra("comment_id", comment.id.toInt())
            intent.putExtra("author_id", comment.author_name)
            intent.putExtra("adapter_position", position.toInt())
            activity.startActivity(intent)
        }

    }

    private fun doLikeAndUnlike(commentId: Int, holder: CommentViewHolder, position: Int) {
        val BASE_URL = activity.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeCommentRequest(commentId, MainHome.USER_ID.toInt())

        apiService?.likeDislikeComment(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    val comment = commentsList[position]

                    if (!comment.liked) {
                        comment.liked = true
                        comment.like_count += 1
                        holder.btnLike.setImageResource(R.drawable.ic_red_heart)
                    } else {
                        comment.liked = false
                        comment.like_count -= 1
                        holder.btnLike.setImageResource(R.drawable.ic_heart)
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


    fun timeAgo(dateTimeStr: String): String {
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

    override fun getItemCount(): Int {
        return commentsList.size
    }

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val timeAgo: TextView = itemView.findViewById(R.id.timeAgo)
        val commentText: TextView = itemView.findViewById(R.id.tvComments)
        val commentCount: TextView = itemView.findViewById(R.id.comments)
        val likeCount: TextView = itemView.findViewById(R.id.likes)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val item: LinearLayout = itemView.findViewById(R.id.item)
        val header: LinearLayout = itemView.findViewById(R.id.header)

    }
}
