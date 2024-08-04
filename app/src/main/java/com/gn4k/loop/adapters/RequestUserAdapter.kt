package com.gn4k.loop.adapters

import ApiService
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.JoinRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.models.response.ParticipantList
import com.gn4k.loop.notificationModel.SaveNotificationInDB
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestUserAdapter(
    private val imageUrls: MutableList<ParticipantList>,
    private val context: Context,
    private val projectId: Int,
    private val acceptedUserAdapter: AcceptedUserAdapter,
    private val authorId: Int
) : RecyclerView.Adapter<RequestUserAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val acceptButton: Button = itemView.findViewById(R.id.btnAccept)
        val rejectButton: ImageButton = itemView.findViewById(R.id.btnReject)
        val main: LinearLayout = itemView.findViewById(R.id.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project_user, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]

        holder.usernameTextView.text = imageUrl.name

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(context.getString(R.string.base_url) + imageUrl.photo_url)
            .placeholder(R.drawable.ic_profile)
            .into(holder.photoImageView)

        if (MainHome.USER_ID.toInt() != authorId) {
            holder.acceptButton.visibility = View.GONE
            holder.rejectButton.visibility = View.GONE
        }

        holder.acceptButton.setOnClickListener {
            showConfirmationDialog(holder.itemView.context, imageUrl.id, "accept", holder.adapterPosition)
            SaveNotificationInDB().save(
                context,
                MainHome.USER_ID.toInt(),
                imageUrl.id,
                projectId,
                "projects",
                "${MainHome.USER_NAME} accepted your request to join this project"
            )
        }

        holder.rejectButton.setOnClickListener {
            showConfirmationDialog(holder.itemView.context, imageUrl.id, "reject", holder.adapterPosition)
            SaveNotificationInDB().save(
                context,
                MainHome.USER_ID.toInt(),
                imageUrl.id,
                projectId,
                "projects",
                "${MainHome.USER_NAME} rejected your request to join this project"
            )
        }

        holder.main.setOnClickListener {
            if (imageUrl.id == MainHome.USER_ID.toInt()) {
                val intent = Intent(context, Profile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("userId", imageUrl.id.toString())
                context.startActivity(intent)
            } else {
                val intent = Intent(context, OthersProfile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("userId", imageUrl.id.toString())
                context.startActivity(intent)
            }
        }
    }

    private fun showConfirmationDialog(context: Context, userId: Int, action: String, position: Int) {
        val title = if (action == "accept") "Confirm Accept" else "Confirm Reject"
        val message = if (action == "accept") "Are you sure you want to accept this request?" else "Are you sure you want to reject this request?"

        AlertDialog.Builder(context, R.style.DarkAlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                acceptRejectProjectRequest(userId, action, position)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun acceptRejectProjectRequest(userId: Int, action: String, position: Int) {
        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.acceptRejectProjectRequest(JoinRequest(userId, projectId, action))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        Toast.makeText(context, meetingResponse?.message, Toast.LENGTH_SHORT).show()
                        // Update the lists based on the action
                        if (action == "accept") {
                            val acceptedParticipant = imageUrls[position]
                            imageUrls.removeAt(position)
                            notifyDataSetChanged()
                            acceptedUserAdapter.addParticipant(acceptedParticipant)
                        } else if (action == "reject") {
                            imageUrls.removeAt(position)
                            notifyDataSetChanged()
                        }
                    } else {
                        // Handle error response
                    }
                }

                override fun onFailure(call: Call<CreateMeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    // Add a method to add a participant to the list
    fun addParticipant(participant: ParticipantList) {
        imageUrls.add(participant)
        notifyDataSetChanged()
    }
}
