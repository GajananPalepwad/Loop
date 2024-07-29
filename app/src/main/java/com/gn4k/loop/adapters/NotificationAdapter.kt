package com.gn4k.loop.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.models.response.NotificationResponse
import java.text.SimpleDateFormat
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

        holder.tvMessage.text = notification.message
        holder.tvTime.text = formatDateTime(notification.created_at)
    }

    override fun getItemCount() = notifications.size

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
