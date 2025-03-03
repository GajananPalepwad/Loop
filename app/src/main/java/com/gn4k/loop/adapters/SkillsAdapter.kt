package com.gn4k.loop.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gn4k.loop.R
import com.gn4k.loop.models.StaticVariables

class SkillsAdapter(
    private val skills: MutableSet<String>,
    private val removeSkill: (String) -> Unit
) : RecyclerView.Adapter<SkillsAdapter.SkillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false)
        return SkillViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = skills.elementAt(position)
        holder.skillTextView.text = skill
        holder.itemView.setOnClickListener {
            removeSkill(skill)
        }

        holder.badgeIcon.setImageResource(StaticVariables.badgeDrawables[skill] ?: R.drawable.java)
    }

    override fun getItemCount(): Int {
        return skills.size
    }

    class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val skillTextView: TextView = itemView.findViewById(R.id.skillTextView)
        val badgeIcon: ImageView = itemView.findViewById(R.id.badgeIcon)
    }
}