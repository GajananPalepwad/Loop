package com.gn4k.loop.adapters

import ApiService
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.response.GetProjects
import com.gn4k.loop.models.response.ParticipantList
import com.gn4k.loop.notificationModel.SaveNotificationInDB
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AcceptedUserAdapter(
    private val imageUrls: MutableList<ParticipantList>,
    private val context: Context,
    private val projectId: Int,
    private val authorId: Int
) : RecyclerView.Adapter<AcceptedUserAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val acceptButton: Button = itemView.findViewById(R.id.btnAccept)
        val rejectButton: ImageButton = itemView.findViewById(R.id.btnReject)
        val main: LinearLayout = itemView.findViewById(R.id.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_user, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(context.getString(R.string.base_url) + imageUrl.photo_url)
            .placeholder(R.drawable.ic_profile)
            .into(holder.photoImageView)
        holder.usernameTextView.text = imageUrl.name

        holder.acceptButton.visibility = View.GONE

        holder.rejectButton.setOnClickListener {
            showConfirmationDialog(holder.itemView.context, imageUrl.id, imageUrl.name)
        }

        if(MainHome.USER_ID.toInt() == authorId){
            holder.rejectButton.visibility = View.GONE
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

    private fun showConfirmationDialog(context: Context, userId: Int, name: String) {
        val title = "Confirm Remove"
        val message = "Are you sure you want to remove $name as a contributor?"

        AlertDialog.Builder(context, R.style.DarkAlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                removeContributor(userId)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun removeContributor(id: Int) {
        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.removeContributor(projectId, id)
            ?.enqueue(object : Callback<GetProjects?> {
                override fun onResponse(
                    call: Call<GetProjects?>,
                    response: Response<GetProjects?>
                ) {
                    if (response.isSuccessful) {
                        val msgResponse = response.body()
                        // Remove the participant from the list and notify the adapter
                        imageUrls.removeAll { it.id == id }
                        SaveNotificationInDB().save(
                            context,
                            MainHome.USER_ID.toInt(),
                            authorId,
                            projectId,
                            "projects",
                            "${MainHome.USER_NAME} removed you as a contributor to this project"
                        )
                        notifyDataSetChanged()
                    } else {
                        // Handle error response
                    }
                }

                override fun onFailure(call: Call<GetProjects?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    fun addParticipant(participant: ParticipantList) {
        imageUrls.add(participant)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}
