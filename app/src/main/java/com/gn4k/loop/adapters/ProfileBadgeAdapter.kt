package com.gn4k.loop.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gn4k.loop.R
import com.gn4k.loop.models.StaticVariables
import com.gn4k.loop.ui.profile.self.badges.ManageBadges

class ProfileBadgeAdapter(private val badges: List<String>, private val context: Context) :
    RecyclerView.Adapter<ProfileBadgeAdapter.BadgeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        StaticVariables.badgeDrawables.get(badge)?.let { holder.badgeIcon.setImageResource(it) }

        holder.badgeIcon.setOnClickListener {
            val intent = Intent(context, ManageBadges::class.java)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return badges.size
    }

    inner class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val badgeIcon: ImageView = itemView.findViewById(R.id.badgeIcon)
    }
}