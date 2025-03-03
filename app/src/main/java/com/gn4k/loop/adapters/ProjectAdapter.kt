package com.gn4k.loop.adapters

import com.gn4k.loop.api.ApiService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colormoon.readmoretextview.ReadMoreTextView
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.RepetitiveFun
import com.gn4k.loop.models.request.JoinRequest
import com.gn4k.loop.models.response.CreateMeetingResponse
import com.gn4k.loop.models.response.Project
import com.gn4k.loop.notificationModel.SaveNotificationInDB
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.projects.ProjectDetails
import com.overflowarchives.linkpreview.TelegramPreview
import com.overflowarchives.linkpreview.ViewListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProjectAdapter(
    private var projects: List<Project>,
    private val context: Context
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
        val rcTags: RecyclerView = itemView.findViewById(R.id.rcTags)
        val description: ReadMoreTextView = itemView.findViewById(R.id.tvDescription)
        val linkPreview: TelegramPreview = itemView.findViewById(R.id.link_preview)
        val rvParticipants: RecyclerView = itemView.findViewById(R.id.rvParticipants)
        val joinButton: Button = itemView.findViewById(R.id.btnJoin)
        val item: LinearLayout = itemView.findViewById(R.id.item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]

        holder.title.text = project.title
        holder.status.text = project.status

        when (project.status) {
            "Yet to start" -> holder.status.setTextColor(holder.itemView.context.getColor(R.color.green))
            "In progress" -> holder.status.setTextColor(holder.itemView.context.getColor(R.color.app_color))
            "Completed" -> holder.status.setTextColor(holder.itemView.context.getColor(R.color.red))
            "On hold" -> holder.status.setTextColor(holder.itemView.context.getColor(R.color.white))
        }

//        holder.description.text = project.description
        RepetitiveFun().makeLinksClickable(holder.description, project.description)

        holder.description.setCollapsedTextColor(R.color.app_color)
        holder.description.setExpandedTextColor(R.color.app_color)

        val formattedUrl = formatUrl(project.link_preview)

        holder.linkPreview.loadUrl(formattedUrl, object : ViewListener {
            override fun onPreviewSuccess(status: Boolean) {}
            override fun onFailedToLoad(e: Exception?) {}
        })

        if (project.joined_persons.any { it.id == MainHome.USER_ID.toInt() }){
            holder.joinButton.visibility = View.GONE
        } else if (project.requested_people.any { it.id == MainHome.USER_ID.toInt() }) {
            holder.joinButton.text = "Requested"
        }

        val badgeAdapter = ProfileBadgeAdapter(project.tags, project.author_id, project.title, context)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        holder.rcTags.layoutManager = layoutManager
        holder.rcTags.adapter = badgeAdapter


        val imageAdapter = ImageAdapter(project.joined_persons, context, project.author_id)
        holder.rvParticipants.adapter = imageAdapter
        holder.rvParticipants.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Handle join button click
        holder.joinButton.setOnClickListener {
            sendRequestToJoinProject(project.project_id)
            if (holder.joinButton.text == "Request to Join"){
                holder.joinButton.text = "Requested"
                SaveNotificationInDB().save(
                    context,
                    MainHome.USER_ID.toInt(),
                    project.author_id,
                    project.project_id,
                    "projects",
                    "${MainHome.USER_NAME} wants to join your project"
                )
            }else{
                holder.joinButton.text = "Request to Join"
            }
        }

        holder.item.setOnClickListener {
            val intent = Intent(context, ProjectDetails::class.java)
            intent.putExtra("joinedPersons", ArrayList(project.joined_persons))
            intent.putExtra("requestPersons", ArrayList(project.requested_people))
            intent.putExtra("projectId", project.project_id.toString())
            intent.putExtra("authorId", project.author_id.toString())
            intent.putExtra("title", project.title)
            intent.putExtra("description", project.description)
            intent.putExtra("status", project.status)
            intent.putExtra("link", project.link_preview)
            intent.putExtra("tags", ArrayList(project.tags))
            context.startActivity(intent)
        }


    }

    private fun sendRequestToJoinProject(projectId: Int) {
        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.sendProjectJoinRequest(JoinRequest(MainHome.USER_ID.toInt(), projectId, "join"))
            ?.enqueue(object : Callback<CreateMeetingResponse?> {
                override fun onResponse(
                    call: Call<CreateMeetingResponse?>,
                    response: Response<CreateMeetingResponse?>
                ) {
                    if (response.isSuccessful) {
                        val meetingResponse = response.body()
                        Toast.makeText(context, meetingResponse?.message, Toast.LENGTH_SHORT).show()

                    } else {
//                    handleErrorResponse(response)
                    }
                }

                override fun onFailure(call: Call<CreateMeetingResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun formatUrl(url: String): String {
        return when {
            url.startsWith("http://") -> url.replace("http://", "https://")
            url.startsWith("https://") -> url
            else -> "https://$url"
        }
    }

    override fun getItemCount(): Int {
        return projects.size
    }

    fun updateList(newList: List<Project>) {
        projects = newList
        notifyDataSetChanged()
    }
}
