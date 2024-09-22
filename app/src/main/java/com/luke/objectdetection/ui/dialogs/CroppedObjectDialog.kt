package com.luke.objectdetection.ui.dialogs

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.luke.objectdetection.BuildConfig
import com.luke.objectdetection.R
import com.luke.objectdetection.databinding.DialogFragmentCroppedObjectBinding
import com.luke.objectdetection.utils.GeminiHelper
import com.luke.objectdetection.utils.Utils

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
        Log.d("CroppedObjectDialog", "onGeminiResponse: $labels")
    }

    fun setObjectBitmap(bitmap: Bitmap) {
        objectBitmap = bitmap
    }

    fun setObjectType(type: String) {
        objectType = type
    }

    private fun bindComponent() {
        objectBitmap?.let {
            binding.ivCroppedObject.setImageBitmap(it)
            googleGeminiHelper.callGeminiApi(it, objectType)
        }
    }
}