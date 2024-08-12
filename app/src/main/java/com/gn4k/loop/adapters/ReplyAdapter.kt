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
import com.gn4k.loop.databinding.ActivityCommentReplyBinding
import com.gn4k.loop.models.RepetitiveFun
import com.gn4k.loop.models.request.LikeDislikeReplyRequest
import com.gn4k.loop.models.response.Reply
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.notificationModel.SaveNotificationInDB
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.post.CommentReply
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReplyAdapter(private val commentsList: MutableList<Reply>, private val activity: Context, private val binding: ActivityCommentReplyBinding, private val postId: Int) : RecyclerView.Adapter<ReplyAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reply, parent, false)
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
            doLikeAndUnlike(comment.id, holder, position, comment.author_id)
        }

        holder.username.text = comment.author_name
        holder.timeAgo.text = timeAgo(comment.created_at)
//        holder.replyText.text = comment.reply_text
        RepetitiveFun().makeLinksClickable(holder.replyText, comment.reply_text)

        holder.likeCount.text = RepetitiveFun().formatCount(comment.like_count).toString()

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

        holder.btnSubReply.setOnClickListener {
            CommentReply.openKeyboard(activity, binding.edReply)
            val replyText = "@${comment.author_name} "
            binding.edReply.setText(replyText)
            binding.edReply.setSelection(replyText.length)
        }

    }

    private fun doLikeAndUnlike(replyId: Int, holder: CommentViewHolder, position: Int, authorId: Int) {
        val BASE_URL = activity.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        val likeDislikeRequest = LikeDislikeReplyRequest(replyId, MainHome.USER_ID.toInt())

        apiService?.likeDislikeReply(likeDislikeRequest)?.enqueue(object : Callback<UserResponse?> {
            override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    val comment = commentsList[position]

                    if (comment.liked ) {
                        comment.liked = false
                        comment.like_count -= 1
                        holder.btnLike.setImageResource(R.drawable.ic_red_heart)
                    } else {
                        comment.liked = true
                        comment.like_count += 1
                        holder.btnLike.setImageResource(R.drawable.ic_heart)
                        SaveNotificationInDB().save(
                            activity,
                            MainHome.USER_ID.toInt(),
                            authorId.toInt(),
                            postId,
                            "likes",
                            "${MainHome.USER_NAME} liked your comment"
                        )
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
        val replyText: TextView = itemView.findViewById(R.id.tvReply)
        val likeCount: TextView = itemView.findViewById(R.id.likes)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val btnSubReply: TextView = itemView.findViewById(R.id.btnSubReply)
        val item: LinearLayout = itemView.findViewById(R.id.item)
        val header: LinearLayout = itemView.findViewById(R.id.header)


    }
}
