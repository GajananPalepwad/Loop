package com.gn4k.loop.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.gn4k.loop.R
import com.gn4k.loop.notifi1.NotificationSender
import com.gn4k.loop.notifi1.NotificationSubscription
import com.gn4k.loop.ui.SplashScreen
import com.gn4k.loop.ui.home.loopMeeting.MeetingLists
import kotlin.properties.Delegates

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

        if(MainHome.USER_ID == "-1"){
            startActivity(Intent(this, SplashScreen::class.java))
            finish()
        }

        val notificationSubscription = NotificationSubscription(this)
//        notificationSubscription.subscribeToTopic("gn")


        val notificationSender = NotificationSender(this)
        notificationSender.sendNotificationToTopic("gn", "Title", "Message")


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
            setFragment(Projects())
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        if(MainHome.USER_ID == "-1"){
            startActivity(Intent(this, SplashScreen::class.java))
            finish()
        }
    }

    companion object {
        var USER_ID : String = "-1"
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
        var USER_FOLLOWERS_COUNT by Delegates.notNull<Int>()
        var USER_FOLLOWING_COUNT by Delegates.notNull<Int>()
        lateinit var GEMINI_KEY: String

        var isSearch: Boolean = false
    }
}