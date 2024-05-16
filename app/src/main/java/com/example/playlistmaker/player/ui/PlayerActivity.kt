package com.example.playlistmaker.player.ui

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.models.Track
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.player.ui.states.PlayerState
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private companion object {
        const val EMPTY = ""
        const val START_TIMER = 0
        const val GET_STRING = "track"
    }

    private lateinit var track: Track

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
    }

    private lateinit var binding: ActivityPlayerBinding

    private var trackUrl: String? = EMPTY

    private val formatTimeTrack: SimpleDateFormat by lazy {
        SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnBackImageView.setOnClickListener {
            finish()
        }

        track = intent.getParcelableExtra(GET_STRING)!!
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

        binding.resumeButton.setOnClickListener {
            viewModel.startPlayer()
        }

        binding.pauseButton.setOnClickListener {
            viewModel.pausePlayer()
        }

        viewModel.getPlayerStateLiveData().observe(this) {

            when (it) {
                is PlayerState.Start -> {
                    binding.resumeButton.isVisible = true
                    binding.pauseButton.isVisible = false
                    binding.resumeButton.isEnabled = true

                }

                is PlayerState.Play -> {
                    binding.resumeButton.isVisible = false
                    binding.pauseButton.isVisible = true
                    binding.trackTimePlayedTextView.text = formatTimeTrack.format(it.trackTimePlaying)

                }

                is PlayerState.Pause -> {
                    binding.resumeButton.isVisible = true
                    binding.resumeButton.isEnabled = true
                    binding.pauseButton.isVisible = false

                }

                is PlayerState.Finish -> {
                    binding.resumeButton.isVisible = true
                    binding.pauseButton.isVisible = false
                    binding.trackTimePlayedTextView.text = formatTimeTrack.format(START_TIMER)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

}