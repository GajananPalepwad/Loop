package com.gn4k.loop.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.models.StaticVariables
import com.gn4k.loop.models.response.Post
import com.gn4k.loop.ui.post.ActivityPost
import com.overflowarchives.linkpreview.TelegramPreview
import com.overflowarchives.linkpreview.ViewListener
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExploreAdapter(private val posts: List<Post>, private val activity: Context) :
    RecyclerView.Adapter<ExploreAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.username.text = post.author_name
        Glide.with(activity)
            .load(activity.getString(R.string.base_url) + post.author_photo_url)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(holder.profileImage)
        holder.tvContext.text = post.context
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

        holder.item.setOnClickListener {
            openPost(post, position)
        }

        holder.codeContainer.setOnClickListener {
            openPost(post, position)
        }

        holder.tvContext.setOnClickListener {
            openPost(post, position)
        }

        holder.username.setOnClickListener {
            openPost(post, position)
        }

        holder.profileImage.setOnClickListener {
            openPost(post, position)
        }

    }

    fun openPost(post: Post, position: Int){
        StaticVariables.isExplore = true

        val intent = Intent(activity, ActivityPost::class.java)
        intent.putExtra("post_type", post.type)
        intent.putExtra("post_link", post.link)
        intent.putExtra("user_photo_url", post.author_photo_url)
        intent.putExtra("user_name", post.author_name)
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

    private fun formatUrl(url: String): String {
        return when {
            url.startsWith("http://") -> url.replace("http://", "https://")
            url.startsWith("https://") -> url
            else -> "https://$url"
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

    override fun getItemCount(): Int = posts.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvContext: TextView = itemView.findViewById(R.id.tvContext)
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val codeContainer: EditText = itemView.findViewById(R.id.codeContainer)
        val linkPreview: TelegramPreview = itemView.findViewById(R.id.link_preview)
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val item: CardView = itemView.findViewById(R.id.item)

    }
}



