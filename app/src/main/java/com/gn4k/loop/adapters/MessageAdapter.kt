package com.gn4k.loop.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gn4k.loop.R
import com.gn4k.loop.models.response.Msg
import com.gn4k.loop.ui.home.MainHome
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageAdapter(private val messages: List<Msg>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        holder.messageTextView.text = message.message
        holder.sentAtTextView.text = getFormatedDateTime(message.sent_at)

        // Set chat bubble background color and alignment
        val isCurrentUser = message.sender_id == MainHome.USER_ID.toInt()
        val background = if (isCurrentUser) {
            holder.itemView.context.getDrawable(R.drawable.chat_bubble_right)
        } else {
            holder.itemView.context.getDrawable(R.drawable.chat_bubble_left)
        }
        holder.messageContainer.background = background

        // Align chat bubbles
        val params = holder.messageContainer.layoutParams as LinearLayout.LayoutParams
        if (isCurrentUser) {
            params.gravity = Gravity.END
        } else {
            params.gravity = Gravity.START
        }
        holder.messageContainer.layoutParams = params
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    private fun getFormatedDateTime(inputDateString: String): String{
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM HH:mm")

        val dateTime = LocalDateTime.parse(inputDateString, inputFormatter)
        return dateTime.format(outputFormatter)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val sentAtTextView: TextView = itemView.findViewById(R.id.sentAtTextView)
        val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)
    }
}
