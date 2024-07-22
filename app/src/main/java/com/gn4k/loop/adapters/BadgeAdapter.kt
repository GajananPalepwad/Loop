package com.gn4k.loop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gn4k.loop.R
import com.gn4k.loop.models.StaticVariables

class BadgeAdapter(
    private val context: Context,
    private val badges: List<String>
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_badges, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        holder.badgeTextView.text = badge
        // You can set the icon dynamically if needed, for example:

        StaticVariables.badgeDrawables.get(badge)?.let { holder.iconImageView.setImageResource(it) }
    }

    override fun getItemCount(): Int {
        return badges.size
    }

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val badgeTextView: TextView = itemView.findViewById(R.id.badgeTextView)
        val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)
    }
}