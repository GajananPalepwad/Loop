package com.gn4k.loop.adapters

import ApiService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.colormoon.readmoretextview.ReadMoreTextView
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.JoinLeaveMeetRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.models.response.MeetingObject
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.home.loopmeeting.VideoCallScreen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MeetingListInterestedAdapter(
    private var meetings: List<MeetingObject>,
    private val context: Context,
    private val token: String
) : RecyclerView.Adapter<MeetingListInterestedAdapter.MeetingViewHolder>() {

    private var filteredMeetings: List<MeetingObject> = meetings

    inner class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvInterest: TextView = itemView.findViewById(R.id.interestedCount)
        val tvWatching: TextView = itemView.findViewById(R.id.watchCount)
        val tvDescription: ReadMoreTextView = itemView.findViewById(R.id.tvDescription)
        val btnJoin: AppCompatButton = itemView.findViewById(R.id.btnJoin)
        val btnInterested: AppCompatButton = itemView.findViewById(R.id.btnInterested)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_interested_meeting, parent, false)
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = filteredMeetings[position]
        holder.tvTitle.text = meeting.title
        holder.tvTime.text = formatDateTime(meeting.start_time)
        holder.tvDescription.text = meeting.description
        holder.tvInterest.text = "${meeting.interested_count} Interest ❤️"
        holder.tvWatching.text = "${meeting.joined_count} Join 🙋‍♂️"

        holder.tvDescription.setCollapsedTextColor(R.color.app_color)
        holder.tvDescription.setExpandedTextColor(R.color.app_color)

        val currentTime = System.currentTimeMillis()
        val meetingStartTime = parseDateTime(meeting.start_time)

        if (currentTime >= meetingStartTime.time) {
            holder.btnJoin.isEnabled = true
            holder.btnJoin.text = "Join"
            holder.btnJoin.alpha = 1.0f // Fully visible
        } else {
            holder.btnJoin.isEnabled = false
            holder.btnJoin.text = "Not Yet Started"
            holder.btnJoin.alpha = 0.5f // Semi-transparent to indicate it's disabled
        }

        holder.btnJoin.setOnClickListener {
            val intent = Intent(context, VideoCallScreen::class.java)
            intent.putExtra("token", token)
            intent.putExtra("meetingId", meeting.meeting_id)
            context.startActivity(intent)
        }

        holder.btnInterested.setOnClickListener {
            showInterestMeeting(meeting.meeting_id, "remove")
        }
    }

    private fun parseDateTime(dateTime: String): Date {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return inputFormat.parse(dateTime) ?: Date()
    }


    private fun showInterestMeeting(meeting_id: String, action: String) {
        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.showInterestMeet(JoinLeaveMeetRequest(meeting_id, MainHome.USER_ID.toInt(), action))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        Toast.makeText(context, meetingResponse?.message, Toast.LENGTH_SHORT).show()
                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<CreateMeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    override fun getItemCount(): Int = filteredMeetings.size

    fun filter(query: String) {
        filteredMeetings = if (query.isEmpty()) {
            meetings
        } else {
            meetings.filter {
                it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    fun formatDateTime(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val date: Date = inputFormat.parse(dateTime) ?: return ""
        return outputFormat.format(date)
    }

}
