package com.gn4k.loop.ui.profile.self.badges

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gn4k.loop.R
import com.gn4k.loop.adapters.QuestionAdapter
import com.gn4k.loop.databinding.ActivityExamPageBinding
import com.gn4k.loop.models.request.Question
import com.gn4k.loop.ui.animation.CustomLoading
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ExamPage : AppCompatActivity() {

    lateinit var binding: ActivityExamPageBinding
    lateinit var badge: String
    private lateinit var countDownTimer: CountDownTimer
    lateinit var loading: CustomLoading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = CustomLoading(this)

        badge = intent.getStringExtra("badge").toString()

        binding.tvBadge.text = "Test for $badge Badge"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = QuestionAdapter(question) {
            updateQuestionIndicators()
        }
        binding.recyclerView.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            loading.startLoading()
            val json = toJson()
//            Log.d("JSON", json)
            lifecycleScope.launch {
                geminiGiveScore(json)
            }
        }

        startTimer()
    }

    private suspend fun geminiGiveScore(prompt: String) {
        val generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
            modelName = getString(R.string.gemini_model),
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = getString(R.string.gemini_key)
        )

        val response =
            generativeModel.generateContent("Please any how provide only a numerical total score out of 100 for the following test response: $prompt")
        extractFirstInteger(response.text.toString())
    }

    private fun extractFirstInteger(input: String) {
        loading.stopLoading()
        val regex = "\\d+".toRegex()
        val matchResult = regex.find(input)
        val score = matchResult?.value?.toInt() ?: -1
        val intent = Intent(this, ExamScore::class.java)
        intent.putExtra("score", score.toString())
        intent.putExtra("badge", badge)
        startActivity(intent)
        finish()
    }


    private fun updateQuestionIndicators() {
        val indicators = listOf(
            binding.q1,
            binding.q2,
            binding.q3,
            binding.q4,
            binding.q5,
            binding.q6,
            binding.q7,
            binding.q8,
            binding.q9,
            binding.q10
        )
        for (i in indicators.indices) {
            if (i < question.size && question[i].answer.isNotEmpty()) {
                indicators[i].setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            } else {
                indicators[i].setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            }
        }
    }

    private fun toJson(): String {
        val gson = Gson()
        return gson.toJson(question)
    }

    private fun startTimer() {
        val totalTime = 30 * 60 * 1000L // 30 minutes in milliseconds
        countDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvTime.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTime.text = "00:00"
                // Handle the end of the timer here
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

    companion object{
        var question: MutableList<Question> = mutableListOf()
    }

}