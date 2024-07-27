package com.gn4k.loop.ui.post

import ApiService
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.SkillsAdapter
import com.gn4k.loop.api.RetrofitClient
import com.gn4k.loop.databinding.ActivityMakePostBinding
import com.gn4k.loop.models.request.CreatePostRequestForLinkNCode
import com.gn4k.loop.models.response.CreatePostResponse
import com.gn4k.loop.models.response.Skill
import com.gn4k.loop.ui.animation.CustomLoading
import com.gn4k.loop.ui.home.MainHome
import `in`.galaxyofandroid.spinerdialog.SpinnerDialog
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

class MakePost : AppCompatActivity() {

    lateinit var binding: ActivityMakePostBinding

    private var apiService: ApiService? = null

    private var imageFile: File? = null

    var type: String = "only_caption"

    private val selectedSkills = mutableSetOf<String>()

    private lateinit var skillsAdapter: SkillsAdapter

    var tagListSpinner: List<String> = java.util.ArrayList()

    lateinit var spinnerDialog: SpinnerDialog

    lateinit var loading: CustomLoading

    private val selectImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Convert URI to File
                    contentResolver.openInputStream(uri)?.let { inputStream ->
                        val file = File(cacheDir, "selected_image")
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        imageFile = compressImageIfNeeded(file)  // Compress image if needed
                        binding.img.setImageURI(uri)
                    }
                }
            }
        }

    private val captureImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Compress image if needed
                imageFile = compressImageIfNeeded(imageFile!!)
                binding.img.setImageURI(Uri.fromFile(imageFile))  // Use imageFile to set the image
            } else {
                Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = CustomLoading(this)

        val BASE_URL = getString(R.string.base_url)
        val retrofit = RetrofitClient.getClient(BASE_URL)
        apiService = retrofit?.create(ApiService::class.java)

        binding.back.setOnClickListener {
            onBackPressed()
            finish()
        }

        binding.btnChooseType.setOnClickListener {
            animateBottomMargin(binding.btnContainer, 0)
            binding.btnClosContainer.visibility = View.VISIBLE
        }

        binding.btnClosContainer.setOnClickListener {
            animateBottomMargin(binding.btnContainer, -205)
            binding.btnClosContainer.visibility = View.GONE
        }

        binding.btnAddPhoto.setOnClickListener {
            type = "photo"
            binding.codeContainer.visibility = View.GONE
            binding.img.visibility = View.VISIBLE
            binding.tilLink.visibility = View.GONE
            animateBottomMargin(binding.btnContainer, -205)
            binding.btnClosContainer.visibility = View.GONE
            showImagePickerOptions()
        }

        binding.btnAddCode.setOnClickListener {
            type = "code_snippet"
            binding.codeContainer.visibility = View.VISIBLE
            binding.img.visibility = View.GONE
            binding.tilLink.visibility = View.GONE
            animateBottomMargin(binding.btnContainer, -205)
            binding.btnClosContainer.visibility = View.GONE
        }

        binding.btnAddLink.setOnClickListener {
            type = "link"
            binding.codeContainer.visibility = View.GONE
            binding.img.visibility = View.GONE
            binding.tilLink.visibility = View.VISIBLE
            animateBottomMargin(binding.btnContainer, -205)
            binding.btnClosContainer.visibility = View.GONE
        }

        binding.btnPost.setOnClickListener {

            loading.startLoading()

            if (type == "photo") {
                val context = binding.edContext.text.toString()
                val parentPostId = ""

                if (context.isEmpty()) {
                    Toast.makeText(this, "Context is required.", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val requestFile = imageFile?.let { file ->
                    RequestBody.create(MultipartBody.FORM, file)
                }

                val body = imageFile?.let { file ->
                    requestFile?.let { it1 ->
                        MultipartBody.Part.createFormData("image", file.name, it1)
                    }
                }

                val tags = selectedSkills.joinToString(separator = ", ")

                createPostWithImage(context, type, tags, parentPostId, body)
            } else {
                val tags = selectedSkills.joinToString(separator = "\", \"")

                createPostWithLinkAndCode(tags)
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        skillsAdapter = SkillsAdapter(selectedSkills) { skill -> removeSkill(skill) }
        binding.recyclerView.adapter = skillsAdapter

        // Initialize spinnerDialog with an empty list to avoid uninitialized property access exception
        spinnerDialog = SpinnerDialog(
            this,
            ArrayList(),
            "Select or Search Subject",
            R.style.DialogAnimations_SmileWindow,
            "Close"
        )

        fetchSkills()

        binding.btnAdd.setOnClickListener {
            spinnerDialog.showSpinerDialog()
        }
    }

    private fun fetchSkills() {


        apiService?.getSkills()?.enqueue(object : Callback<Skill> {
            override fun onResponse(call: Call<Skill>, response: Response<Skill>) {
                if (response.isSuccessful) {
                    val skillResponse = response.body()
                    val skills = skillResponse?.skills
                    if (skills != null) {
                        tagListSpinner = skills.map { it.skill }
                        spinnerDialog = SpinnerDialog(
                            this@MakePost,
                            tagListSpinner as ArrayList<String?>,
                            "Select or Search Skills",
                            R.style.DialogAnimations_SmileWindow,
                            "Close"
                        )

                        spinnerDialog.setCancellable(true)
                        spinnerDialog.setShowKeyboard(false)

                        spinnerDialog.bindOnSpinerListener { item, position ->
                            addSkill(item)
                        }
                    } else {
                        Log.e(
                            "SkillSelector",
                            "Response not successful: ${response.errorBody()?.string()}"
                        )
                        Toast.makeText(
                            baseContext,
                            "Failed to fetch skills",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        "SkillSelector",
                        "Response not successful: ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(baseContext, "Failed to fetch skills", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Skill>, t: Throwable) {
                Log.d("Reg", "Network Error: ${t.message}")
                Toast.makeText(baseContext, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun addSkill(skill: String) {
        if (selectedSkills.add(skill)) {
            skillsAdapter.notifyItemInserted(selectedSkills.size - 1)
//            Toast.makeText(this, "$skill added", Toast.LENGTH_SHORT).show()
        } else {
//            Toast.makeText(this, "$skill already selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeSkill(skill: String) {
        val position = selectedSkills.indexOf(skill)
        if (selectedSkills.remove(skill)) {
            skillsAdapter.notifyItemRemoved(position)
//            Toast.makeText(this, "$skill removed", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showImagePickerOptions() {
        val options = arrayOf("Select from Gallery", "Capture from Camera")
        AlertDialog.Builder(this)
            .setTitle("Select or Capture Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> captureImageFromCamera()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(pickPhotoIntent)
    }

    private fun captureImageFromCamera() {
        val file = File.createTempFile("image", ".jpg", cacheDir)
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        captureImageLauncher.launch(captureImageIntent)

        // Update the imageFile variable
        imageFile = file
    }

    private fun compressImageIfNeeded(file: File): File {
        // Load the image
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        // Prepare the output stream
        var outputStream = ByteArrayOutputStream()

        // Compress the bitmap to JPEG with quality decreasing until the size is below 500KB
        var quality = 100
        var compressedFile: File = file
        do {
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val compressedBytes = outputStream.toByteArray()
            val compressedFileSizeKB = compressedBytes.size / 1024  // Convert bytes to kilobytes

            // Save the compressed image back to a file
            compressedFile = File(cacheDir, "compressed_image.jpg")
            compressedFile.outputStream().use { it.write(compressedBytes) }

            quality -= 10  // Decrease quality
        } while (compressedFileSizeKB > 500 && quality > 0)  // Check if file size is under 500KB and quality is not 0

        return compressedFile
    }

    private fun createPostWithImage(context: String, type: String, tags: String?, parentPostId: String?, image: MultipartBody.Part?) {
        val userId = MainHome.USER_ID

        // Create RequestBody objects
        val authorIdPart = RequestBody.create(MultipartBody.FORM, userId)
        val contextPart = RequestBody.create(MultipartBody.FORM, context)
        val typePart = RequestBody.create(MultipartBody.FORM, type)
        val tagsPart = RequestBody.create(MultipartBody.FORM, tags ?: "")
        val parentPostIdPart = parentPostId?.let { RequestBody.create(MultipartBody.FORM, it) }

        // Call the API
        apiService?.createPost(authorIdPart, contextPart, typePart, tagsPart, parentPostIdPart, image)?.enqueue(object :
            Callback<CreatePostResponse> {
            override fun onResponse(call: Call<CreatePostResponse>, response: Response<CreatePostResponse>) {
                if (response.isSuccessful) {
                    val createPostResponse = response.body()
                    Toast.makeText(this@MakePost, createPostResponse?.message ?: "Post created successfully", Toast.LENGTH_SHORT).show()
                    loading.stopLoading()
                    onBackPressed()
                } else {
                    handleErrorResponse(response)
                    loading.stopLoading()
                }
            }

            override fun onFailure(call: Call<CreatePostResponse>, t: Throwable) {
                Log.d("CreatePostActivity", "Network Error: ${t.message}")
                loading.stopLoading()
                Toast.makeText(this@MakePost, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createPostWithLinkAndCode(tags: String) {
        val authorId = MainHome.USER_ID
        val context = binding.edContext.text.toString()
        val parentPostId = ""
        var linkOrCode = ""
        if (type == "code_snippet") {
            linkOrCode = binding.codeContainer.text.toString()
        } else if (type == "link") {
            linkOrCode = binding.edLink.text.toString()
        } else if (type == "only_caption"){
            linkOrCode = ""
        }

        if (context.isEmpty()) {
            Toast.makeText(this, "Context is required.", Toast.LENGTH_SHORT).show()
            loading.stopLoading()

            return
        }

        val postData = CreatePostRequestForLinkNCode(authorId, context, type, tags, parentPostId, linkOrCode)

        // Call the API
        apiService?.createPostWithLinkAndCode(postData)?.enqueue(object : Callback<CreatePostResponse> {
            override fun onResponse(call: Call<CreatePostResponse>, response: Response<CreatePostResponse>) {
                if (response.isSuccessful) {
                    val createPostResponse = response.body()
                    Toast.makeText(this@MakePost, createPostResponse?.message ?: "Post created successfully", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                    loading.stopLoading()
                } else {
                    loading.stopLoading()
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<CreatePostResponse>, t: Throwable) {
                Log.d("MakePostActivity", "Network Error: ${t.message}")
                Toast.makeText(this@MakePost, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                loading.stopLoading()
            }
        })
    }

    private fun handleErrorResponse(response: Response<CreatePostResponse>) {
        when (response.code()) {
            400 -> {
                Log.d("MakePostActivity", "Bad Request: ${response.message()}")
                Toast.makeText(this, "Bad Request: ${response.message()}", Toast.LENGTH_SHORT).show()
            }
            500 -> {
                Log.d("MakePostActivity", "Internal Server Error")
                Toast.makeText(this, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.d("MakePostActivity", "Unexpected Error: ${response.message()}")
                Toast.makeText(this, "Unexpected Error: ${response.message()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun animateBottomMargin(view: ViewGroup, toMargin: Int) {
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        val fromMargin = params.bottomMargin

        val animator = ValueAnimator.ofInt(fromMargin, toMargin)
        animator.addUpdateListener { animation ->
            params.bottomMargin = animation.animatedValue as Int
            view.layoutParams = params
        }
        animator.duration = 300 // duration of the animation in milliseconds
        animator.start()
    }
}
