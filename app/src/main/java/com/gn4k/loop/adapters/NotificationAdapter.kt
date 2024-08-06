package com.gn4k.loop.adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.models.response.NotificationResponse
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.post.DeepLinkPost
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class NotificationAdapter(private val notifications: List<NotificationResponse>, private val context: Context) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val item: LinearLayout = itemView.findViewById(R.id.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(context.getString(R.string.base_url)+notification.sender_profile_url)
            .placeholder(R.drawable.ic_profile)
            .into(holder.photoImageView)

        holder.item.setOnClickListener {
            if(notification.type.equals("comments")){

                val intent = Intent(context, DeepLinkPost::class.java)
                intent.putExtra("postId", notification.post_id)
                context.startActivity(intent)

            }else if(notification.type.equals("follows")){

                val intent = Intent(context, OthersProfile::class.java)
                intent.putExtra("userId", notification.sender_user_id.toString())
                context.startActivity(intent)

            }else if(notification.type.equals("likes")){

                val intent = Intent(context, DeepLinkPost::class.java)
                intent.putExtra("postId", notification.post_id)
                context.startActivity(intent)

            }else if(notification.type.equals("projects")){

                val intent = Intent(context, Profile::class.java)
                intent.putExtra("userId", MainHome.USER_ID)
                intent.putExtra("fragment", "projects")
                context.startActivity(intent)

            }

        }


        holder.tvMessage.text = notification.message
        holder.tvTime.text = convertUtcToLocal(formatDateTime(notification.created_at))
    }

    override fun getItemCount() = notifications.size

    private fun convertUtcToLocal(timeString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val outputFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val localTime = LocalTime.parse(timeString, inputFormatter).plusHours(-11)
        val currentDate = LocalDate.now()
        val dateTime = LocalDateTime.of(currentDate, localTime)
        val utcZonedDateTime = dateTime.atZone(ZoneId.of("UTC"))
        val localZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
        return localZonedDateTime.format(outputFormatter)
    }


    fun formatDateTime(dateTimeString: String): String {
        // Define the input and output date formats
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Parse the input datetime string
        val date = inputFormat.parse(dateTimeString) ?: return ""

        // Get the current time and calculate the difference
        val currentTime = Calendar.getInstance().time
        val diffInMillis = currentTime.time - date.time

        // Calculate the difference in days
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            diffInDays < 1 -> timeFormat.format(date) // Less than 1 day ago
            diffInDays < 7 -> dayFormat.format(date) // Less than 1 week ago
            else -> dateFormat.format(date) // More than 1 week ago
        }
    }
}
