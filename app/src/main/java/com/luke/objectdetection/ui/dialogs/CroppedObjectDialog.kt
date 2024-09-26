package com.luke.objectdetection.ui.dialogs

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luke.objectdetection.R
import com.luke.objectdetection.data.SearchResponse
import com.luke.objectdetection.databinding.DialogFragmentCroppedObjectBinding
import com.luke.objectdetection.network.ApiService
import com.luke.objectdetection.network.RetrofitClient
import com.luke.objectdetection.ui.adapter.SearchResultAdapter
import com.luke.objectdetection.utils.GeminiHelper
import com.luke.objectdetection.utils.RecyclerItemClickListener
import com.luke.objectdetection.utils.SearchItemAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CroppedObjectDialog : DialogFragment(), GeminiHelper.GeminiListener {
    companion object {
        fun newInstance(bitmap: Bitmap, objectType: String): CroppedObjectDialog {
            return CroppedObjectDialog().apply {
                setObjectBitmap(bitmap)
                setObjectType(objectType.lowercase())
            }
        }
    }

    private var objectBitmap: Bitmap? = null
    private var objectType: String = ""

    private var _binding: DialogFragmentCroppedObjectBinding? = null
    private val binding get() = _binding!!

    //region network
    private val apiService by lazy {
        RetrofitClient.instance.create(ApiService::class.java)
    }

    //region adapter
    private val searchAdapter by lazy {
        SearchResultAdapter().apply { setItemClickListener(recyclerItemClickListener) }
    }

    private val recyclerItemClickListener = object : RecyclerItemClickListener {
        override fun onItemClickListener(
            searchItem: SearchResponse.SearchItemResponse,
            position: Int,
            viewId: Int
        ) {
            openBrowser(searchItem.prodUrl)
        }
    }

    private val googleGeminiHelper by lazy {
        GeminiHelper().apply {
            setListener(this@CroppedObjectDialog)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentCroppedObjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.run {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindComponent()
    }

    override fun onGeminiResponse(labels: List<String>) {
        if (labels.isNotEmpty()) {
            val label = labels[0]
            Log.d("CroppedObjectDialog", "label: $label")
            label.replace("[", "").replace("]", "").let {
                Log.d("CroppedObjectDialog", "new label: $it")
                searchProductBaseOnGemini(it)
                animateTextLetterByLetter(it)
            }
        }
    }

    //region animate view
    private fun animateObjectView() {
        animateGuideline(binding.glImage, binding.root)
    }

    private fun animateGuideline(guideline: Guideline, parent: ConstraintLayout) {
        val startPercent = 0.3f
        val endPercent = 0.2f
        val duration = 300L

        val animator = ValueAnimator.ofFloat(startPercent, endPercent).apply {
            this.duration = duration
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                val constraintSet = ConstraintSet()
                constraintSet.clone(parent)
                constraintSet.setGuidelinePercent(guideline.id, animatedValue)
                constraintSet.applyTo(parent)
            }
        }
        animator.start()
    }

//    private fun pushImageViewUpToShowTextView() {
//        val startPercent = 0.5f
//        val endPercent = 0.3f
//        val duration = 300L
//
//        val animator = ValueAnimator.ofFloat(startPercent, endPercent).apply {
//            this.duration = duration
//            addUpdateListener { animation ->
//                val animatedValue = animation.animatedValue as Float
//                val constraintSet = ConstraintSet()
//                constraintSet.clone(binding.root)
//                constraintSet.setGuidelinePercent(binding.glImage, animatedValue)
//                constraintSet.applyTo(binding.root)
//            }
//        }
//        animator.start()
//    }

    private fun animateTextLetterByLetter(text: String) {
        val duration = 100L
        val textLength = text.length
        val stringBuilder = StringBuilder()
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 0 until textLength) {
                stringBuilder.append(text[i])
                launch(Dispatchers.Main) {
                    binding.tvLabel.text = stringBuilder.toString()
                }
                delay(duration)
            }
        }
    }

    fun setObjectBitmap(bitmap: Bitmap) {
        objectBitmap = bitmap
    }

    fun setObjectType(type: String) {
        objectType = type
    }

    private fun bindComponent() {
        objectBitmap?.let {
            showLoading()
            binding.ivCroppedObject.setImageBitmap(it)
            googleGeminiHelper.callGeminiApi(it, objectType)
        }

        binding.rvSearchResult.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            itemAnimator = SearchItemAnimator()
        }
    }

    private fun showLoading() {
        binding.pbLoading.visibility = View.VISIBLE
        binding.rvSearchResult.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.pbLoading.visibility = View.GONE
        binding.rvSearchResult.visibility = View.VISIBLE
    }

    private fun openBrowser(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context?.startActivity(browserIntent)
        } catch (e: Exception) {
            Log.e("Error", "openWebBrowser error")
        }
    }

    //region calling api
    private fun searchProductBaseOnGemini(query: String) {
        apiService.search(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                hideLoading()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("YourActivity", "Response: $responseBody")
                    searchAdapter.updateSearchResults(responseBody?.results ?: emptyList())
                    animateObjectView()

                } else {
                    Log.e("YourActivity", "Request failed with code: ${response.code()}")
                    Toast.makeText(
                        context,
                        "Request failed with code: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("YourActivity", "Request failed: ${t.message}")
            }
        })

    }
}