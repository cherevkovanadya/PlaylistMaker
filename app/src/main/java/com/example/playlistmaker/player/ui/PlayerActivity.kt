package com.example.playlistmaker.player.ui

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.util.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private companion object {
        const val EMPTY = ""
    }

    private lateinit var binding: ActivityPlayerBinding
    private val handler = Handler(Looper.getMainLooper())
    private val mediaPlayer = Creator.providePlayerInteractor()
    private var trackUrl: String? = EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnBackImageView.setOnClickListener {
            finish()
        }

        @Suppress("DEPRECATION") val track = intent.getParcelableExtra<Track>("track")
        if (track != null) {
            Glide.with(binding.trackCoverImageView.context)
                .load(track.artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg"))
                .transform(
                    RoundedCorners(
                        binding.trackCoverImageView.resources.getDimensionPixelSize(
                            R.dimen.corner_radius
                        )
                    )
                )
                .placeholder(R.drawable.large_placeholder)
                .into(binding.trackCoverImageView)
            binding.trackNameTextView.text = track.trackName
            binding.artistNameTextView.text = track.artistName
            binding.trackTimeValueTextView.text = track.trackTimeString.toString()
            binding.collectionNameValueTextView.text = track.collectionName
            binding.releaseDateValueTextView.text = track.releaseDate
            binding.primaryGenreNameValueTextView.text = track.primaryGenreName
            binding.countryValueTextView.text = track.country
            trackUrl = track.previewUrl
        }

        preparePlayer()

        binding.resumeButton.setOnClickListener {
            startPlayer()
        }

        binding.pauseButton.setOnClickListener {
            pausePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
        handler.removeCallbacks(trackTimePlaying)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(trackTimePlaying)
        mediaPlayer.release()
    }

    private fun preparePlayer() {
        mediaPlayer.setDataSource(trackUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener(param = {
            binding.resumeButton.isEnabled = true
            binding.resumeButton.isVisible = true
            binding.pauseButton.isVisible = false
        })
        mediaPlayer.setOnCompletionListener(param = {
            binding.resumeButton.isVisible = true
            binding.pauseButton.isVisible = false
            handler.removeCallbacks(trackTimePlaying)
        })
    }

    private fun startPlayer() {
        mediaPlayer.start()
        binding.resumeButton.isVisible = false
        binding.pauseButton.isVisible = true
        timer()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        binding.resumeButton.isVisible = true
        binding.pauseButton.isVisible = false
        handler.removeCallbacks(trackTimePlaying)
    }

    private fun timer() {
        handler.post(trackTimePlaying)
    }

    private var trackTimePlaying = object : Runnable {
        override fun run() {
            binding.trackTimePlayedTextView.text = SimpleDateFormat(
                "mm:ss",
                Locale.getDefault()
            ).format(mediaPlayer.currentPosition().toLong())
            handler.postDelayed(this, 500)
        }
    }
}