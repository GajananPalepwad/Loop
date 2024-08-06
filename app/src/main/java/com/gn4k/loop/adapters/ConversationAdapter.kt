package com.gn4k.loop.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.models.response.Conversation
import com.gn4k.loop.ui.msg.Chatting
import com.gn4k.loop.ui.post.ActivityPost
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

class ConversationAdapter(
    private var conversations: List<Conversation>,
    private val activity: Context
) :
    RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.bind(conversation)
    }

    fun updateList(newList: List<Conversation>) {
        conversations = newList
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = conversations.size

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        private val txtUserName: TextView = itemView.findViewById(R.id.usernameTextView)
        private val txtLastMessage: TextView = itemView.findViewById(R.id.tvLastMsg)
        private val item: LinearLayout = itemView.findViewById(R.id.item)
        private val unseenDot: CardView = itemView.findViewById(R.id.unseenDot)
        private val time: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(conversation: Conversation) {
            txtUserName.text = conversation.opposite_user_name
            txtLastMessage.text = conversation.last_message

            if (!conversation.is_seen_by_user) {
                txtLastMessage.apply {
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    unseenDot.visibility = View.VISIBLE
                }
            }

            Glide.with(activity)
                .load(activity.getString(R.string.base_url) + conversation.opposite_user_photo_url)
                .centerCrop()
                .placeholder(R.drawable.ic_profile)
                .into(imgProfile)

            time.text = convertUtcToLocal(formatDateTime(conversation.last_message_sent_at))

            item.setOnClickListener {
                val intent = Intent(activity, Chatting::class.java)
                intent.putExtra("conversation_id", conversation.conversation_id.toString())
                intent.putExtra("user_id", conversation.opposite_user_id.toString())
                intent.putExtra("user_name", conversation.opposite_user_name)
                intent.putExtra("profile_link", conversation.opposite_user_photo_url)

                activity.startActivity(intent)
            }
        }
    }

    fun convertUtcToLocal(timeString: String): String {
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
