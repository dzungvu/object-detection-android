package com.luke.objectdetection.ui.dialogs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.luke.objectdetection.R
import com.luke.objectdetection.databinding.DialogFragmentObjectDetectionBinding
import com.luke.objectdetection.utils.BoundingBox
import com.luke.objectdetection.utils.Constants.LABELS_PATH
import com.luke.objectdetection.utils.Constants.MODEL_PATH
import com.luke.objectdetection.utils.Detector

class ObjectDetectionDialog : DialogFragment(), Detector.DetectorListener {

    companion object {
        fun newInstance(bitmap: Bitmap): ObjectDetectionDialog {
            return ObjectDetectionDialog().apply {
                setBitmap(bitmap)
            }
        }
    }

    private var _binding: DialogFragmentObjectDetectionBinding? = null
    private val binding get() = _binding!!

    //region image
    private var bitmap: Bitmap? = null
    private var imageUri: String? = null
    //endregion

    //region detector
    private val detector: Detector by lazy {
        Detector(this.requireContext(), MODEL_PATH, LABELS_PATH, this).apply {
            setup()
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
        _binding = DialogFragmentObjectDetectionBinding.inflate(inflater, container, false)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        detector.clear()
    }

    fun setImageUri(uri: String) {
        Log.d("ObjectDetectionDialog", "uri: $uri")
        bitmap = BitmapFactory.decodeFile(uri)
        imageUri = uri
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    private fun bindComponent() {

        binding.btConfirm.setOnClickListener {
            dismiss()
        }


        bitmap?.let { bitmap ->
            Log.d("ObjectDetectionDialog", "load bitmap: $bitmap")
            binding.ivContent.setImageBitmap(bitmap)
            detector.detect(bitmap)
        } ?: kotlin.run {
            Log.d("ObjectDetectionDialog", "load uri: $imageUri")
            imageUri?.let { uri ->
                binding.ivContent.setImageURI(Uri.parse(uri))
            }
        }

    }

    //region detector listener
    override fun onEmptyDetect() {
        binding.overlay.invalidate()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        binding.overlay.post {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }
}