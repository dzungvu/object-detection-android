package com.luke.objectdetection

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.luke.objectdetection.databinding.ActivityPlayerRealtimeBinding
import com.luke.objectdetection.ui.dialogs.CroppedObjectDialog
import com.luke.objectdetection.utils.BoundingBox
import com.luke.objectdetection.utils.Constants.LABELS_PATH
import com.luke.objectdetection.utils.Constants.MODEL_PATH
import com.luke.objectdetection.utils.Detector
import com.luke.objectdetection.utils.OverlayView

class PlayerRealtimeActivity : AppCompatActivity(), Detector.DetectorListener,
    OverlayView.OnChooseBoxListener {

    private lateinit var binding: ActivityPlayerRealtimeBinding

    private lateinit var detector: Detector

    private val exoPlayer by lazy { buildPlayer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerRealtimeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this)
        detector.setup()

        bindComponent()
    }

    override fun onStart() {
        super.onStart()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun buildPlayer(): ExoPlayer {
        return ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    if (!isRunningDetection) {
                        startIntervalFrameCapture()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying) {
                        detectFrameOnPause()
                    } else {
                        binding.grOverlay.visibility = View.GONE
                    }
                }
            })


        }
    }

    private fun bindComponent() {
        binding.vPlayer.apply {
            player = exoPlayer
        }

        binding.overlay.setOnChooseBoxListener(this)
        binding.ivClose.setOnClickListener {
            binding.grOverlay.visibility = View.GONE
        }

        val hlsUrl =
            "https://vod06-cdn.fptplay.net/POVOD/encoded/2024/01/08/radioromance-2018-kr-001-1704702567/master.m3u8?st=H1mgSgOHdnRhVkOIwDHAPA&expires=1726938540"
        val mediaItem = MediaItem.fromUri(hlsUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun captureFrame(): Bitmap? {
        if (binding.vPlayer.videoSurfaceView is TextureView) {
            return (binding.vPlayer.videoSurfaceView as TextureView).bitmap
        }
        return null
    }

    private val handler = Handler(Looper.getMainLooper())
    private val frameCaptureRunnable = object : Runnable {
        override fun run() {
            detectFrame()
            handler.postDelayed(this, 500) // Schedule next execution in 1 second
        }
    }
    private var isRunningDetection = false

    private fun startIntervalFrameCapture() {
//        isRunningDetection = true
//        handler.post(frameCaptureRunnable)
    }

    private fun detectFrameOnPause() {
        Log.d("PlayerRealtimeActivity", "detectFrame")
        captureFrame()?.let { bitmap: Bitmap ->
            Log.d("PlayerRealtimeActivity", "detectFrame: $bitmap")
            binding.grOverlay.visibility = View.VISIBLE
            detector.detectWithCoroutine(bitmap)
        }
    }

    private fun detectFrame() {
        if (exoPlayer.isPlaying) {
            Log.d("PlayerRealtimeActivity", "detectFrame")
            captureFrame()?.let { bitmap: Bitmap ->
                Log.d("PlayerRealtimeActivity", "detectFrame: $bitmap")
                detector.detectWithCoroutine(bitmap)
            }
        }
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            binding.overlay.clear()
            binding.overlay.invalidate()
        }
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

    override fun onChooseBox(box: BoundingBox) {
        val bitmap = captureFrame()
        val cropArea = cropArea(box, bitmap)
        cropArea?.let {
            val dialog = CroppedObjectDialog.newInstance(it, box.clsName)
            dialog.show(supportFragmentManager, "ObjectDetectionDialog")
        }
    }

    private fun cropArea(box: BoundingBox, bitmap: Bitmap?): Bitmap? {
        bitmap?.let {
            val left = box.x1 * it.width
            val top = box.y1 * it.height
            val right = box.x2 * it.width
            val bottom = box.y2 * it.height
            return Bitmap.createBitmap(
                it,
                left.toInt(),
                top.toInt(),
                right.toInt() - left.toInt(),
                bottom.toInt() - top.toInt()
            )
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        handler.removeCallbacks(frameCaptureRunnable)
    }
}