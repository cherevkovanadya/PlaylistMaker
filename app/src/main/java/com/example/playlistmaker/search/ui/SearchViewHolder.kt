package com.example.playlistmaker.search.ui

import com.example.playlistmaker.search.domain.models.Track
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R

class SearchViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val trackNameTextView = itemView.findViewById<TextView>(R.id.trackNameTextView)
    private val artistNameTextView = itemView.findViewById<TextView>(R.id.artistNameTextView)
    private val trackTimeTextView = itemView.findViewById<TextView>(R.id.trackTimeTextView)
    private val trackCoverImageView = itemView.findViewById<ImageView>(R.id.trackCoverImageView)

    fun bind(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        trackTimeTextView.text = track.trackTimeString
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.small_corner_radius)))
            .placeholder(R.drawable.placeholder)
            .into(trackCoverImageView)
    }
}