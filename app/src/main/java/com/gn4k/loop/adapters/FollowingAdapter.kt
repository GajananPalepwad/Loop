package com.gn4k.loop.adapters

import ApiService
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.models.request.FollowUnfollowRequest
import com.gn4k.loop.models.response.FollowUnfollowResponse
import com.gn4k.loop.models.response.Following
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.profile.followLists.FollowList
import com.gn4k.loop.ui.profile.others.OthersProfile
import com.gn4k.loop.ui.profile.self.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowingAdapter(
    private val context: Context,
    private var followings: List<Following>
) : RecyclerView.Adapter<FollowingAdapter.FollowingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_following_user, parent, false)
        return FollowingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val following = followings[position]
        holder.bind(following)
    }

    override fun getItemCount(): Int = followings.size

    inner class FollowingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val badgesTextView: TextView = itemView.findViewById(R.id.badges)
        private val btnUnfollow: AppCompatButton = itemView.findViewById(R.id.btnUnfollow)
        private val item: LinearLayout = itemView.findViewById(R.id.item)


        var isFollowing = true

        fun bind(following: Following) {
            usernameTextView.text = following.name
            badgesTextView.text = if (following.badges.isNotEmpty()) {
                following.badges.joinToString(", ")
            } else {
                "No Badge Earned"
            }

            // Load profile image using Glide
            Glide.with(context)
                .load(context.getString(R.string.base_url) + following.photo_url)
                .placeholder(R.drawable.ic_profile) // Placeholder image if needed
                .into(photoImageView)

            btnUnfollow.setOnClickListener {
                if(isFollowing) {
                    followUnfollow(following.id, "unfollow")
                    isFollowing = false
                    btnUnfollow.text = "Follow"
                    btnUnfollow.backgroundTintList = ContextCompat.getColorStateList(context, R.color.app_color)
                }else{
                    followUnfollow(following.id, "follow")
                    isFollowing = true
                    btnUnfollow.text = "UnFollow"
                    btnUnfollow.backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
                }
            }

            if(FollowList.userId!=MainHome.USER_ID.toInt()){
                btnUnfollow.visibility = View.GONE
            }

            item.setOnClickListener {
                if(following.id==MainHome.USER_ID.toInt()){
                    val intent = Intent(context, Profile::class.java)
                    intent.putExtra("userId", following.id.toString())
                    context.startActivity(intent)
                }else {
                    val intent = Intent(context, OthersProfile::class.java)
                    intent.putExtra("userId", following.id.toString())
                    context.startActivity(intent)
                }
            }

        }
    }

    fun followUnfollow(userId: Int, action: String) {
        val BASE_URL = context.getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        val apiService = retrofit?.create(ApiService::class.java)

        apiService?.followUnfollow(FollowUnfollowRequest(MainHome.USER_ID.toInt(), userId, action))
            ?.enqueue(object : Callback<FollowUnfollowResponse?> {
                override fun onResponse(
                    call: Call<FollowUnfollowResponse?>,
                    response: Response<FollowUnfollowResponse?>
                ) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()

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

    fun updateList(newList: List<Following>) {
        followings = newList
        notifyDataSetChanged()
    }
}