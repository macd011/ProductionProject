package com.example.productionproject.viewmodel
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import com.example.productionproject.R


class MainActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize VideoView
        videoView = findViewById(R.id.videoView)

        // Set video file path
        val videoPath = "android.resource://" + packageName + "/" + R.raw.loginvideo
        videoView.setVideoURI(Uri.parse(videoPath))

        // Start video playback
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            videoView.start()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources
        videoView.stopPlayback()
    }
}
