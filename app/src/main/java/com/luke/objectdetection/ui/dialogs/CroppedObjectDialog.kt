package com.luke.objectdetection.ui.dialogs

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.luke.objectdetection.R
import com.luke.objectdetection.databinding.DialogFragmentCroppedObjectBinding

class CroppedObjectDialog : DialogFragment() {

    companion object {
        fun newInstance(bitmap: Bitmap): CroppedObjectDialog {
            return CroppedObjectDialog().apply {
                setObjectBitmap(bitmap)
            }
        }
    }

    private var objectBitmap: Bitmap? = null

    private var _binding: DialogFragmentCroppedObjectBinding? = null
    private val binding get() = _binding!!


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

    fun setObjectBitmap(bitmap: Bitmap) {
        objectBitmap = bitmap
    }

    private fun bindComponent() {
        objectBitmap?.let {
            binding.ivCroppedObject.setImageBitmap(it)
        }
    }
}