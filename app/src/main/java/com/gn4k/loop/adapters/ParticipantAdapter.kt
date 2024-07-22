package com.gn4k.loop.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gn4k.loop.R
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Stream
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.listeners.MeetingEventListener
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import org.webrtc.VideoTrack

class ParticipantAdapter(private val meeting: Meeting, private val onParticipantCountChanged: () -> Unit) : RecyclerView.Adapter<ParticipantAdapter.PeerViewHolder>() {

    private val participants: MutableList<Participant> = ArrayList()

    init {
        participants.add(meeting.localParticipant)

        // adding Meeting Event listener to get the participant join/leave event in the meeting.
        meeting.addEventListener(object : MeetingEventListener() {
            override fun onParticipantJoined(participant: Participant) {
                // add participant to the list
                participants.add(participant)
                notifyDataSetChanged()
                onParticipantCountChanged()
            }

            override fun onParticipantLeft(participant: Participant) {
                val pos = participants.indexOfFirst { it.id == participant.id }
                if (pos >= 0) {
                    participants.removeAt(pos)
                    notifyDataSetChanged()
                    onParticipantCountChanged()
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        return PeerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_remote_user, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        val participant = participants[position]

        holder.tvName.text = participant.displayName

        // adding the initial video stream for the participant into the 'VideoView'
        for ((_, stream) in participant.streams) {
            if (stream.kind.equals("video", ignoreCase = true)) {
                holder.participantView.visibility = View.VISIBLE
                val videoTrack = stream.track as VideoTrack
                holder.participantView.addTrack(videoTrack)
                break
            }
        }

        // add Listener to the participant which will update start or stop the video stream of that participant
        participant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.participantView.visibility = View.VISIBLE
                    val videoTrack = stream.track as VideoTrack
                    holder.participantView.addTrack(videoTrack)
                }
            }

            override fun onStreamDisabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.participantView.removeTrack()
                    holder.participantView.visibility = View.GONE
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    class PeerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var participantView: VideoView = view.findViewById(R.id.participantView)
        var tvName: TextView = view.findViewById(R.id.tvName)
    }
}
