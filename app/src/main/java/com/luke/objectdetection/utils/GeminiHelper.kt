package com.luke.objectdetection.utils


import android.graphics.Bitmap
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
            "person" -> "Describe the person in this image"
            "car" -> "Describe the car in this image"
//            else -> "Without jank words, give me the name, color, brand of this object in the image"
//            else -> "In very few words, just only information, I need to buy this main object inside the image, please send me the name, color, brand of it if you have"
            else -> "In very few words, just only information, what is the main object in this image and it's color or brand name if you can specify it, if you don't have the information, ignore it"
        }
    }

    fun callGeminiApi(bitmap: Bitmap, objectType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = generativeModel.generateContent(
                content {
                    image(image = bitmap)
                    text(getPrompt(objectType))
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