package com.gn4k.loop.ui.profile.self.badges

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gn4k.loop.R
import com.gn4k.loop.databinding.ActivityExamRulesAndInfoBinding
import com.gn4k.loop.models.request.Question
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class ExamRulesAndInfo : AppCompatActivity() {

    private lateinit var binding: ActivityExamRulesAndInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamRulesAndInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val badge = intent.getStringExtra("badge")

        lifecycleScope.launch {
            question = geminiCreateQuestions("Give me 10 $badge questions with hard level but every question must have \"?\" mark at the end")
        }

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, ExamPage::class.java)
            intent.putExtra("badge", badge)
            startActivity(intent)
        }

    }

    private suspend fun geminiCreateQuestions(prompt: String): MutableList<Question> {
        val generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
            modelName = getString(R.string.gemini_model),
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = getString(R.string.gemini_key)
        )

        val response = generativeModel.generateContent(prompt)
        return makeQuestionList(response.text.toString())
    }


    private fun makeQuestionList(response: String): MutableList<Question> {
        val questions = response.trimIndent()
            .split("\n")
            .map { it.trim() }
            .filter { it.endsWith("?") }
            .map { Question(question = it) }
        Toast.makeText(baseContext, "Done", Toast.LENGTH_SHORT).show()
        return questions.toMutableList()
    }

    companion object{
        var question: MutableList<Question> = mutableListOf()
    }


}