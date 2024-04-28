package com.example.playlistmaker.search.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.models.Track

class SearchHistoryAdapter(
    private val clickListener: TrackClickListener
) : RecyclerView.Adapter<SearchViewHolder>() {

    var tracksHistory: MutableList<Track> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return SearchViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tracksHistory.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(tracksHistory[position])
        holder.itemView.setOnClickListener{ clickListener.onTrackClick(tracksHistory[position])}
    }

    fun interface TrackClickListener {
        fun onTrackClick(track: Track)
    }
}