package com.gn4k.loop.ui.home.loopmeeting

import ApiService
import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.gn4k.loop.R
import com.gn4k.loop.adapters.MeetingListAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityMeetingListsBinding
import com.gn4k.loop.models.request.CreateMeetRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.models.response.CreatePostResponse
import com.gn4k.loop.models.response.MeetingResponse
import com.gn4k.loop.ui.home.MainHome
import live.videosdk.rtc.android.VideoSDK
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MeetingLists : AppCompatActivity() {

    private var transaction = supportFragmentManager.beginTransaction()
    lateinit var binding: ActivityMeetingListsBinding

    private var sampleToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiIxNjU0OTU4OC1jMzBiLTQwMTktODg4MS1jNzI0ODZmNzUzZmMiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTcyMTk5MjI0NiwiZXhwIjoxODc5NzgwMjQ2fQ.LJbbgQ22NfM-jFl3aPMUyC9rvciUmPj90hRx7COtu_Y"

    private var selectedDate: String? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for necessary permissions before initializing
        if (checkPermissions()) {
            initializeVideoSDK()
        }

        switchToAllMeetings()

        binding.btnCreate.setOnClickListener {
            createMeetingPopup()
        }

        binding.btnAllMeeting.setOnClickListener {
            switchToAllMeetings()
        }

        binding.btnIneterestedMeeting.setOnClickListener {
            switchToInterestedMeetings()
        }
    }

    private fun initializeVideoSDK() {
        VideoSDK.initialize(applicationContext)
    }

    lateinit var dialog: Dialog

    private fun createMeetingPopup() {
        dialog = Dialog(this, R.style.AppBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_create_meeting_dialog)
        dialog.setCancelable(true)

        val editTextTitle: EditText = dialog.findViewById(R.id.editTextTitle)
        val editTextDescription: EditText = dialog.findViewById(R.id.editTextDescription)
        val buttonSelectDate: EditText = dialog.findViewById(R.id.editTextDate)
        val buttonSelectTime: EditText = dialog.findViewById(R.id.editTextTime)
        val buttonCreateMeeting: Button = dialog.findViewById(R.id.buttonCreateMeeting)

        buttonSelectDate.setOnClickListener {
            showDatePicker(buttonSelectDate)
        }

        buttonSelectTime.setOnClickListener {
            showTimePicker(buttonSelectTime)
        }

        buttonCreateMeeting.setOnClickListener {
            if(editTextTitle.text.isNotEmpty() && editTextDescription.text.isNotEmpty()) {
                createMeeting(sampleToken, editTextTitle.text.toString(), editTextDescription.text.toString(), dateTime)
            }
        }

        dialog.show()
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                updateDateTimeEditText()
                editText.setText(selectedDate)

            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                selectedTime = String.format("%02d:%02d:%02d", selectedHour, selectedMinute, 0)
                updateDateTimeEditText()
                editText.setText(selectedTime)
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }

    var dateTime = ""

    private fun updateDateTimeEditText() {
        if (selectedDate != null && selectedTime != null) {
            dateTime = "$selectedDate $selectedTime"
//            editText.setText(dateTime)
        }
    }

    private fun storeMeeting(meeting_id: String, tilte: String, description: String, start_time: String) {
        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.createMeeting(CreateMeetRequest(meeting_id, tilte, description, start_time, MainHome.USER_ID.toInt()))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        dialog.cancel()
                        Toast.makeText(baseContext, meetingResponse?.message, Toast.LENGTH_SHORT).show()
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

    private fun createMeeting(token: String, title: String, description: String, dateTime: String) {
        AndroidNetworking.post("https://api.videosdk.live/v2/rooms")
            .addHeaders("Authorization", token)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val meetingId = response.getString("roomId")

                        storeMeeting(meetingId, title, description, dateTime)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onError(anError: ANError) {
                    anError.printStackTrace()
                    Toast.makeText(
                        this@MeetingLists, anError.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun switchToAllMeetings() {
        binding.btnIneterestedMeeting.setTextColor(getColor(R.color.white))
        binding.btnAllMeeting.setTextColor(getColor(R.color.app_color))
        setFragment(AllMeetLists())
    }

    private fun switchToInterestedMeetings() {
        binding.btnAllMeeting.setTextColor(getColor(R.color.white))
        binding.btnIneterestedMeeting.setTextColor(getColor(R.color.app_color))
        setFragment(InterestedMeetLists())
    }

    private fun setFragment(fragment: Fragment) {
        transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun checkPermissions(): Boolean {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID) &&
                checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID)
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeVideoSDK()
            } else {
                Toast.makeText(this, "Permissions required to use this app", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        private const val PERMISSION_REQ_ID = 22
    }
}
