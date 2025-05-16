package com.kynarec.kmusic.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kynarec.kmusic.MainActivity
import com.kynarec.kmusic.MyApp
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView = itemView.findViewById<TextView>(R.id.song_title)
        private val artistTextView = itemView.findViewById<TextView>(R.id.song_artist)
        private val durationTextView = itemView.findViewById<TextView>(R.id.song_duration)
        private val thumbnailImageView = itemView.findViewById<ImageView>(R.id.imageView)

        fun bind(song: Song) {
            titleTextView.text = song.title
            artistTextView.text = song.artist
            durationTextView.text = song.duration

            Glide.with(itemView.context)
                .load(song.thumbnail)
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                .into(thumbnailImageView)

            itemView.setOnClickListener {
                onSongClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size
}
