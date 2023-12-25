package com.example.playlistmaker

import SearchAdapter
import Track
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnBackImageView.setOnClickListener {
            finish()
        }

        val track = intent.getParcelableExtra<Track>("track")
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
            binding.trackTimeValueTextView.text =
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
            binding.collectionNameValueTextView.text = track.collectionName
            binding.releaseDateValueTextView.text = track.releaseDate
            binding.primaryGenreNameValueTextView.text = track.primaryGenreName
            binding.countryValueTextView.text = track.country
        }
    }
}