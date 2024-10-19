package com.gn4k.loop.adapters


import com.gn4k.loop.api.ApiService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.FollowUnfollowRequest
import com.gn4k.loop.models.response.FollowUnfollowResponse
import com.gn4k.loop.models.response.Follower
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.followLists.FollowList
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FollowerAdapter(
    private val context: Context,
    private var followers: MutableList<Follower>
) : RecyclerView.Adapter<FollowerAdapter.FollowerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_follower_user, parent, false)
        return FollowerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        val follower = followers[position]
        holder.bind(follower, position)
    }

    override fun getItemCount(): Int = followers.size

    inner class FollowerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val badges: RecyclerView = itemView.findViewById(R.id.badges)
        private val btnRemove: AppCompatButton = itemView.findViewById(R.id.btnRemove)
        private val item: LinearLayout = itemView.findViewById(R.id.item)


        fun bind(follower: Follower, position: Int) {
            usernameTextView.text = follower.name

            val badgeAdapter = follower.badges?.let { ProfileBadgeAdapter(it, follower.id, follower.name, context) }
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            badges.layoutManager = layoutManager
            badges.adapter = badgeAdapter

            // Load profile image using Glide
            Glide.with(context)
                .load(follower.photo_url)
                .placeholder(R.drawable.ic_profile) // Placeholder image if needed
                .into(photoImageView)

            if(FollowList.userId!=MainHome.USER_ID.toInt()){
                btnRemove.visibility = View.GONE
            }


            item.setOnClickListener {
                if(follower.id== MainHome.USER_ID.toInt()){
                    val intent = Intent(context, Profile::class.java)
                    intent.putExtra("userId", follower.id.toString())
                    context.startActivity(intent)
                }else {
                    val intent = Intent(context, OthersProfile::class.java)
                    intent.putExtra("userId", follower.id.toString())
                    context.startActivity(intent)
                }
            }

            btnRemove.setOnClickListener {
                removeFollow(follower.id, "unfollow", position)
            }
        }
    }

    fun removeFollow(userId: Int, action: String, position: Int) {
        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.followUnfollow(FollowUnfollowRequest(userId, MainHome.USER_ID.toInt(), action))
            ?.enqueue(object : Callback<FollowUnfollowResponse?> {
                override fun onResponse(
                    call: Call<FollowUnfollowResponse?>,
                    response: Response<FollowUnfollowResponse?>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()

                        followers.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, followers.size)

                    } else {
//                    handleErrorResponse(response)
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<FollowUnfollowResponse?>, t: Throwable) {
                    Log.d("Reg", "Network Error: ${t.message}")
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    fun updateList(newList: MutableList<Follower>) {
        followers = newList
        notifyDataSetChanged()
    }
}