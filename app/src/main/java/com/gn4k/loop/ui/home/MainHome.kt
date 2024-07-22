package com.gn4k.loop.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.gn4k.loop.R
import com.gn4k.loop.ui.home.loopmeeting.MeetingLists

class MainHome : AppCompatActivity() {

    private lateinit var nav: View
    private lateinit var btnHome: ImageView
    private lateinit var btnChat: ImageView
    private lateinit var btnConnectLive: CardView
    private lateinit var btnExplore: ImageView
    private lateinit var btnProject: ImageView

    private var transaction = supportFragmentManager.beginTransaction()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setFragment(HomeFeed())


        nav = findViewById(R.id.nav_bar)

        btnHome = nav.findViewById(R.id.btnHome)
        btnChat = nav.findViewById(R.id.btnChat)
        btnConnectLive = nav.findViewById(R.id.btnConnectLive)
        btnExplore = nav.findViewById(R.id.btnExplore)
        btnProject = nav.findViewById(R.id.btnProject)

        btnHome.setOnClickListener {
            allLightLogo()
            btnHome.setImageResource(R.drawable.ic_home)
            setFragment(HomeFeed())
        }

        btnChat.setOnClickListener {
            allLightLogo()
            btnChat.setImageResource(R.drawable.ic_chat)
            setFragment(ChatList())
        }

        btnConnectLive.setOnClickListener {
            startActivity(Intent(this, MeetingLists::class.java))
        }

        btnExplore.setOnClickListener {
            allLightLogo()
            btnExplore.setImageResource(R.drawable.ic_explore)
            setFragment(Explore())
        }

        btnProject.setOnClickListener {
            allLightLogo()
            btnProject.setImageResource(R.drawable.ic_project)
        }
    }

    private fun setFragment(fragment: Fragment){
        transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null) // Add the transaction to the back stack if needed
        transaction.commit()
    }

    private fun allLightLogo() {
        btnHome.setImageResource(R.drawable.ic_home_light)
        btnChat.setImageResource(R.drawable.ic_chat_light)
        btnExplore.setImageResource(R.drawable.ic_explore_light)
        btnProject.setImageResource(R.drawable.ic_project_light)
    }

    companion object {
        lateinit var USER_ID : String
        lateinit var USER_EMAIL: String
        lateinit var USER_USERNAME: String
        lateinit var USER_NAME: String
        lateinit var USER_BADGES: List<String>
        lateinit var USER_ABOUT: String
        lateinit var USER_PHOTO_URL: String
        lateinit var USER_CREATED_AT: String
        lateinit var USER_UPDATED_AT: String
        lateinit var USER_LAST_LOGIN: String
        lateinit var USER_STATUS: String
        lateinit var USER_ROLE: String
        lateinit var USER_LOCATION: String
        lateinit var USER_WEBSITE: String
        lateinit var USER_SKILLS: List<String>
        lateinit var USER_FOLLOWERS_COUNT: String
        lateinit var USER_FOLLOWING_COUNT: String
    }
}