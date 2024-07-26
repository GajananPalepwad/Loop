package com.gn4k.loop.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.models.response.ParticipantList
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile

class AcceptedUserAdapter(
    private val imageUrls: List<ParticipantList>,
    private val context: Context
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

        holder.acceptButton.visibility = View.GONE

        holder.photoImageView.setOnClickListener {
            if(imageUrl.id == MainHome.USER_ID.toInt()){
                val intent = Intent(context, Profile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", imageUrl.id.toString())
                context.startActivity(intent)
            }else {
                val intent = Intent(context, OthersProfile::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", imageUrl.id.toString())
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}
