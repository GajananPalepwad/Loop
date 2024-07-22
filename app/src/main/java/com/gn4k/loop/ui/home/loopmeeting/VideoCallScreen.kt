package com.gn4k.loop.ui.home.loopmeeting

import ApiService
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ParticipantAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityVideoCallScreenBinding
import com.gn4k.loop.models.request.CreateMeetRequest
import com.gn4k.loop.models.request.JoinLeaveMeetRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.ui.home.MainHome
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoCallScreen : AppCompatActivity() {
    private lateinit var binding: ActivityVideoCallScreenBinding
    private var meeting: Meeting? = null
    private var micEnabled = false
    private var webcamEnabled = false
    lateinit var meetingId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val token = intent.getStringExtra("token")
        meetingId = intent.getStringExtra("meetingId").toString()
        val participantName = MainHome.USER_NAME

        VideoSDK.config(token)

        meeting = VideoSDK.initMeeting(
            this, meetingId, participantName,
            micEnabled, webcamEnabled, null, null, false, null, null
        )

        meeting!!.addEventListener(meetingEventListener)

        meeting!!.join()

        val participantAdapter = ParticipantAdapter(meeting!!) {
            adjustGridSpanCount()
        }
        binding.rvParticipants.apply {
            layoutManager = GridLayoutManager(this@VideoCallScreen, 1, GridLayoutManager.HORIZONTAL, false)
            adapter = participantAdapter
        }

        setActionListeners()
        if (meetingId != null) {
            joinLeaveMeeting(meetingId, "join")
        }
    }

    private fun joinLeaveMeeting(meeting_id: String, action: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.joinLeaveMeet(JoinLeaveMeetRequest(meeting_id, MainHome.USER_ID.toInt(), action))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
//                        Toast.makeText(baseContext, meetingResponse?.message, Toast.LENGTH_SHORT).show()
                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<CreateMeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    // Adjust the span count of the grid layout based on the number of participants
    private fun adjustGridSpanCount() {
        val participantCount = (binding.rvParticipants.adapter as ParticipantAdapter).itemCount
        val spanCount = when {
            participantCount == 1 -> 1
            participantCount == 2 -> 2
            participantCount % 2 == 0 -> 2
            else -> 3
        }
        (binding.rvParticipants.layoutManager as GridLayoutManager).spanCount = spanCount
    }

    // Creating the MeetingEventListener
    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()")
        }

        override fun onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()")
            meeting = null
            if (!isDestroyed) finish()
        }

        override fun onParticipantJoined(participant: Participant) {
            Toast.makeText(
                this@VideoCallScreen, "${participant.displayName} joined",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onParticipantLeft(participant: Participant) {
            Toast.makeText(
                this@VideoCallScreen, "${participant.displayName} left",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Set action listeners for mic, webcam, and leave buttons
    private fun setActionListeners() {
        // Toggle mic
        binding.btnMic.setOnClickListener {
            if (micEnabled) {
                meeting!!.muteMic()
                binding.btnMic.setImageResource(R.drawable.ic_mic_off)
                Toast.makeText(this, "Mic Muted", Toast.LENGTH_SHORT).show()
            } else {
                meeting!!.unmuteMic()
                binding.btnMic.setImageResource(R.drawable.ic_mic_on)
                Toast.makeText(this, "Mic Enabled", Toast.LENGTH_SHORT).show()
            }
            micEnabled = !micEnabled
        }

        // Toggle webcam
        binding.btnWebcam.setOnClickListener {
            if (webcamEnabled) {
                meeting!!.disableWebcam()
                binding.btnWebcam.setImageResource(R.drawable.ic_cam_off)
                Toast.makeText(this, "Webcam Disabled", Toast.LENGTH_SHORT).show()
            } else {
                meeting!!.enableWebcam()
                binding.btnWebcam.setImageResource(R.drawable.ic_cam_on)
                Toast.makeText(this, "Webcam Enabled", Toast.LENGTH_SHORT).show()
            }
            webcamEnabled = !webcamEnabled
        }

        // Leave meeting
        binding.btnLeave.setOnClickListener {
            meeting!!.leave()
            joinLeaveMeeting(meetingId, "leave")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        meeting!!.leave()
        joinLeaveMeeting(meetingId, "leave")
        meeting=null
    }

    override fun onDestroy() {
        super.onDestroy()
        if(meeting!=null) {
            meeting!!.leave()
            joinLeaveMeeting(meetingId, "leave")
        }
    }

}
