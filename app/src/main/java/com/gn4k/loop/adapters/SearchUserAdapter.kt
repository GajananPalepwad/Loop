package com.gn4k.loop.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.models.response.SearchUser
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.post.ActivityPost
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile

class SearchUserAdapter(private val userList: List<SearchUser>, private val activity: Context) : RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder>() {

    inner class SearchUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val badges: RecyclerView = itemView.findViewById(R.id.badges)
        val item: LinearLayout = itemView.findViewById(R.id.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false)
        return SearchUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchUserViewHolder, position: Int) {
        val user = userList[position]

        holder.usernameTextView.text = user.name

        val badgeAdapter = user.badges?.let { ProfileBadgeAdapter(it, user.id, user.name, activity) }
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        holder.badges.layoutManager = layoutManager
        holder.badges.adapter = badgeAdapter

        Glide.with(holder.itemView.context)
            .load(activity.getString(R.string.base_url) + user.photo_url)
            .placeholder(R.drawable.ic_profile) // Optional placeholder
            .into(holder.photoImageView)

        holder.item.setOnClickListener {
            goToProfile(user.id)
        }

        holder.badges.setOnClickListener {
            goToProfile(user.id)
        }
    }

    private fun goToProfile(userId: Int){
        if(userId==MainHome.USER_ID.toInt()){
            val intent = Intent(activity, Profile::class.java)
            intent.putExtra("userId", userId.toString())
            activity.startActivity(intent)
        }else {
            val intent = Intent(activity, OthersProfile::class.java)
            intent.putExtra("userId", userId.toString())
            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size
}
