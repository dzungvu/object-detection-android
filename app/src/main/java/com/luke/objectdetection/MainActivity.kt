package com.luke.objectdetection

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luke.objectdetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindEvent()
    }

    private fun bindEvent() {
        binding.btCameraView.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.btGalleryView.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        binding.btPlayerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }

        binding.btPlayerRealtime.setOnClickListener {
            startActivity(Intent(this, PlayerRealtimeActivity::class.java))
        }
    }


}