package com.luke.objectdetection

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Surface
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
import com.luke.objectdetection.databinding.ActivityPlayerBinding
import com.luke.objectdetection.ui.dialogs.ObjectDetectionDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val exoPlayer by lazy { buildPlayer() }

    private var isPausedByUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindComponent()
    }

    override fun onStart() {
        super.onStart()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun buildPlayer(): ExoPlayer {
        return ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (!isPlaying) {
                        if(isPausedByUser) {
                            onPlayerPaused()
                        }
                        isPausedByUser = false
                    }
                }
            })
        }
    }

    private fun bindComponent() {
        binding.textureView.apply {
            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    exoPlayer.setVideoSurface(Surface(surface))
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                }
            }
        }
//        binding.vPlayer.apply {
//            player = exoPlayer
//        }

        binding.btnPlayPause.setOnClickListener {

            if (exoPlayer.isPlaying) {
                isPausedByUser = true
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        }

        binding.btnSeekBackward.setOnClickListener {
            exoPlayer.seekBack()
        }

        binding.btnSeekForward.setOnClickListener {
            exoPlayer.seekForward()
        }

        val hlsUrl =
            "https://vod03-cdn.fptplay.net/POVOD/encoded/2024/08/15/clubfridayseason16neverwrong-2024-th-001-1723701116/H264/master.m3u8?st=-JKLeMhnZlKPDi0uz_DTuw&expires=1726406014"
        val mediaItem = MediaItem.fromUri(hlsUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun onPlayerPaused() {
        captureView(binding.textureView)?.let { bitmap: Bitmap ->
            saveBitmapToFile(bitmap)?.let { imageUri ->
                val dialog = ObjectDetectionDialog()
                Log.d("PlayerActivity", "bitmap: $bitmap")
                dialog.setImageUri(imageUri)
//                dialog.setBitmap(bitmap)
                dialog.show(supportFragmentManager, "ObjectDetectionDialog")
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun captureFrame(): Bitmap? {
        binding.vPlayer.videoSurfaceView?.let { videoSurfaceView ->
            if (videoSurfaceView is TextureView) {
                val currentFrameBitmap: Bitmap? = videoSurfaceView.bitmap
                return currentFrameBitmap
            } else {

                val bitmap = Bitmap.createBitmap(
                    videoSurfaceView.width,
                    videoSurfaceView.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                videoSurfaceView.draw(canvas)
                return bitmap
            }
        }
        return null
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        val filename = "captured_frame_${System.currentTimeMillis()}.png"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, filename)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun captureView(view: View): Bitmap? {
        try {
            if (view is TextureView) {
                Log.d("PlayerActivity", "view is TextureView")
                return view.bitmap
            } else {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                return bitmap
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}