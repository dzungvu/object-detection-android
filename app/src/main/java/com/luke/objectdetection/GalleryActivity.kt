package com.luke.objectdetection

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luke.objectdetection.databinding.ActivityGalleryBinding
import com.luke.object_detection.Detector
import com.luke.object_detection.utils.BoundingBox
import com.luke.object_detection.utils.Constants.LABELS_PATH
import com.luke.object_detection.utils.Constants.MODEL_PATH
import com.luke.object_detection.utils.OverlayView

class GalleryActivity : AppCompatActivity(), Detector.DetectorListener {

    private lateinit var binding: ActivityGalleryBinding

    //region detector
    private val detector: Detector by lazy {
        Detector(this, MODEL_PATH, LABELS_PATH, this).apply {
            setup()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindComponent()
        bindEvent()
    }

    private fun bindComponent() {
        binding.btLoadImage.setOnClickListener {
            openGallery()
        }

    }

    private fun bindEvent() {

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri: Uri? = result.data!!.data

                selectedImageUri?.let {
                    binding.ivContent.setImageURI(it)
                    loadBitmapFromUri(it)?.let { bitmap: Bitmap ->

                        reLayoutBoundingBox(bitmap)
                        classifyImage(bitmap)
                    }
                }
            }
        }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun reLayoutBoundingBox(bitmap: Bitmap) {
        val aspectRatio = calculateAspectRatio(bitmap)
        val layoutParams = binding.overlay.layoutParams

        // Assuming the width of the overlay should match the width of the ImageView
        val overlayWidth = binding.ivContent.width
        val overlayHeight = (overlayWidth / aspectRatio).toInt()

        layoutParams.width = overlayWidth
        layoutParams.height = overlayHeight

        binding.overlay.layoutParams = layoutParams
        binding.overlay.requestLayout()
    }

    private fun calculateAspectRatio(bitmap: Bitmap): Float {
        return bitmap.width.toFloat() / bitmap.height.toFloat()
    }

    private fun classifyImage(bitmap: Bitmap) {
        detector.detect(bitmap)
    }

    override fun onEmptyDetect() {
        binding.overlay.invalidate()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            binding.inferenceTime.text = "${inferenceTime}ms"
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }
}