package com.example.playlistmaker

import Track
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import layout.SearchViewHolder

class SearchHistoryAdapter(
    var tracksHistory: MutableList<Track>,
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<SearchViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return SearchViewHolder(view, onItemClicked)
    }

    override fun getItemCount(): Int {
        return tracksHistory.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(tracksHistory[position])
    }
}