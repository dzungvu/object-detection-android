package com.luke.objectdetection.utils


import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Candidate
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.luke.objectdetection.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class GeminiHelper {
    val tag = "GeminiHelper"

    private var listener: GeminiListener? = null
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GGK
    )

    fun setListener(listener: GeminiListener) {
        this.listener = listener
    }

    fun getPrompt(objectType: String): String {
        return when(objectType) {
            "" -> "Describe this image"
            "person" -> "Describe clothing in this image with format: [clothing] [color] [brand]. Note that you can remove the color and brand if you don't know"
//            else -> "Without jank words, give me the name, color, brand of this object in the image"
//            else -> "In very few words, just only information, I need to buy this main object inside the image, please send me the name, color, brand of it if you have"
//            else -> "In very few words, just only information, what is the main object in this image and it's color or brand name if you can specify it, if you don't have the information, ignore it"
            else -> "This look like $objectType but I'm not sure, answer the question with format: [object name] [color] [brand]. Note that you can remove the color and brand if you don't know"
        }
    }

    fun callGeminiApi(bitmap: Bitmap, objectType: String) {
        val prompt = getPrompt(objectType)
        Log.d(tag, "Prompt: $prompt")
        CoroutineScope(Dispatchers.IO).launch {
            val response = generativeModel.generateContent(
                content {
                    image(image = bitmap)
                    text(prompt)
                }
            )

            val listCandidateReturn = arrayListOf<String>()
            response.candidates.forEach { candidate: Candidate ->
                candidate.content.parts.forEach { candidatePart ->
                    listCandidateReturn.add(candidatePart.asTextOrNull() ?: "")
                }
            }
            listener?.onGeminiResponse(listCandidateReturn)
        }
    }

    interface GeminiListener {
        fun onGeminiResponse(labels: List<String>)
    }
}