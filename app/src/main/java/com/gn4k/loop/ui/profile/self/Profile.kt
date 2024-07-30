package com.gn4k.loop.ui.profile.self

import ApiService
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gn4k.loop.R
import com.gn4k.loop.adapters.ProfileBadgeAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityProfileBinding
import com.gn4k.loop.models.response.UserResponse
import com.gn4k.loop.ui.SplashScreen
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import com.gn4k.loop.ui.home.loopmeeting.MeetingLists
import com.gn4k.loop.ui.post.MakePost
import com.gn4k.loop.ui.profile.followLists.FollowList
import com.gn4k.loop.ui.profile.others.OthersProfile.Companion.userId
import com.gn4k.loop.ui.profile.others.OthersProfile.Companion.userName
import com.gn4k.loop.ui.profile.self.badges.ManageBadges
import com.gn4k.loop.ui.profile.self.drawer.Recent
import com.gn4k.loop.ui.profile.self.drawer.Settings
import com.gn4k.loop.ui.projects.MakeProject
import com.google.android.material.navigation.NavigationView
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class Profile : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityProfileBinding

    private var transaction = supportFragmentManager.beginTransaction()
    private var apiService: ApiService? = null

    private var imageUri: Uri? = null
    private var croppedImageUri: Uri? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        lateinit var loading: CustomLoading
    }

    private val selectImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    startCrop(uri)
                }
            }
        }

    private val captureImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri?.let { uri ->
                    startCrop(uri)
                }
            }
        }

    private val cropImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    UCrop.getOutput(it)?.let { uri ->
                        croppedImageUri = uri
                        binding.imgProfile.setImageURI(uri)
                        // Directly upload the cropped image to the API
                        uploadProfilePhoto(uri)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = CustomLoading(this)

        var intentMake = Intent(this, MakePost::class.java)

        // Set up DrawerLayout and NavigationView
        drawerLayout = binding.drawerLayout
        navView = binding.navView

        binding.back.setOnClickListener {
            onBackPressed()
        }


        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener(this)

        binding.tvName.text = MainHome.USER_NAME
        Glide
            .with(baseContext)
            .load(getString(R.string.base_url) + MainHome.USER_PHOTO_URL)
            .centerCrop()
            .placeholder(R.drawable.ic_profile)
            .into(binding.imgProfile)

        binding.tvFollowersC.text = MainHome.USER_FOLLOWERS_COUNT
        binding.tvFollowingC.text = MainHome.USER_FOLLOWING_COUNT
        binding.tvAbout.text = MainHome.USER_ABOUT
        binding.tvLocation.text = MainHome.USER_LOCATION
        binding.tvWebsite.text = MainHome.USER_WEBSITE

        val badgeAdapter = ProfileBadgeAdapter(MainHome.USER_BADGES, this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.badges.layoutManager = layoutManager
        binding.badges.adapter = badgeAdapter

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileEditing::class.java)
            startActivity(intent)
        }

        setFragment(ProfilePost())

        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)

        binding.addProfile.setOnClickListener {
            pickImageFromGallery()
        }

        binding.btnOptions.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        binding.btnPost.setOnClickListener {
            intentMake = Intent(this, MakePost::class.java)
            binding.btnPost.setTextColor(getColor(R.color.app_color))
            binding.btnProjects.setTextColor(getColor(R.color.white))
            setFragment(ProfilePost())
        }

        binding.btnProjects.setOnClickListener {
            intentMake = Intent(this, MakeProject::class.java)
            binding.btnProjects.setTextColor(getColor(R.color.app_color))
            binding.btnPost.setTextColor(getColor(R.color.white))
            setFragment(ProfileProjects())
        }

        binding.btnFollowings.setOnClickListener {
            val intent = Intent(this, FollowList::class.java)
            intent.putExtra("type", "following")
            intent.putExtra("userId", MainHome.USER_ID)
            intent.putExtra("name", MainHome.USER_NAME)
            startActivity(intent)
        }

        binding.btnFollower.setOnClickListener {
            val intent = Intent(this, FollowList::class.java)
            intent.putExtra("type", "follower")
            intent.putExtra("userId", MainHome.USER_ID)
            intent.putExtra("name", MainHome.USER_NAME)
            startActivity(intent)
        }

        binding.btnMakePost.setOnClickListener {
            startActivity(intentMake)
        }

        binding.badges.setOnClickListener {
            val intent = Intent(this, ManageBadges::class.java)
            startActivity(intent)
        }

        binding.tvWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binding.tvWebsite.text.toString()))
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        binding.tvName.text = MainHome.USER_NAME
        binding.tvFollowersC.text = MainHome.USER_FOLLOWERS_COUNT
        binding.tvFollowingC.text = MainHome.USER_FOLLOWING_COUNT
        binding.tvAbout.text = MainHome.USER_ABOUT
        binding.tvLocation.text = MainHome.USER_LOCATION
        binding.tvWebsite.text = MainHome.USER_WEBSITE
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Select from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select or Capture Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val pickPhotoIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(pickPhotoIntent)
    }

    private fun captureImageFromCamera() {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = createImageFile()
        imageFile?.let {
            imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", it)
            captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            captureImageLauncher.launch(captureImageIntent)
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(createImageFile())
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(100)
        }
        val uCrop = UCrop.of(uri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(500, 500)
        cropImageLauncher.launch(uCrop.getIntent(this))
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val userId = MainHome.USER_ID

        // Convert URI to File
        val file = File(uri.path!!)
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("profile_photo", file.name, requestFile)
        val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)

        apiService?.uploadProfilePhoto(userIdPart, body)?.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Profile photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(baseContext, SplashScreen::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Profile photo upload failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun createImageFile(): File? {
        return try {
            val fileName = "JPEG_${System.currentTimeMillis()}_"
            val storageDir = getExternalFilesDir(null)
            File.createTempFile(fileName, ".jpg", storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView)
        } else {
            super.onBackPressed()
        }
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.nav_share -> {
                shareProfileLink()
            }
            R.id.nav_badges_manager -> {
                startActivity(Intent(this, ManageBadges::class.java))
            }
            R.id.nav_recently_commented -> {
                val intent = Intent(this, Recent::class.java)
                intent.putExtra("type", "commented")
                startActivity(intent)
            }
            R.id.nav_recently_liked -> {
                val intent = Intent(this, Recent::class.java)
                intent.putExtra("type", "liked")
                startActivity(intent)
            }
            R.id.nav_loop_meet -> {
                startActivity(Intent(this, MeetingLists::class.java))
            }

            R.id.nav_settings -> {
                startActivity(Intent(this, Settings::class.java))
            }

            R.id.nav_logout -> {
                showLogoutConfirmationDialog()
            }
        }
        drawerLayout.closeDrawer(navView)
        return true
    }


    private fun shareProfileLink() {
        val shareableLink = getString(R.string.deep_link)+"user=${MainHome.USER_ID}"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "See my profile on Loop: $shareableLink")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share post via"))
    }


    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this, R.style.DarkAlertDialogTheme)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        val sharedPref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("user_id", "-1")
        editor.apply()
        startActivity(Intent(this, SplashScreen::class.java))
        finish()
    }

    private fun setFragment(fragment: Fragment) {
        transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null) // Add the transaction to the back stack if needed
        transaction.commit()
    }
}
