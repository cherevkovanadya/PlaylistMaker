package com.example.playlistmaker

import SearchAdapter
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private val mockTrack = MockTrack
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnBackImageView.setOnClickListener {
            finish()
        }

        Glide.with(binding.trackCoverImageView.context)
            .load(mockTrack.track.artworkUrl100.replaceAfterLast('/',"512x512bb.jpg"))
            .transform(RoundedCorners(binding.trackCoverImageView.resources.getDimensionPixelSize(R.dimen.corner_radius)))
            .placeholder(R.drawable.large_placeholder)
            .into(binding.trackCoverImageView)
        binding.trackNameTextView.text = mockTrack.track.trackName
        binding.artistNameTextView.text = mockTrack.track.artistName
        binding.trackTimeValueTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mockTrack.track.trackTimeMillis)
        binding.collectionNameValueTextView.text = mockTrack.track.collectionName
        binding.releaseDateValueTextView.text = mockTrack.track.releaseDate
        binding.primaryGenreNameValueTextView.text = mockTrack.track.primaryGenreName
        binding.countryValueTextView.text = mockTrack.track.country
    }
}