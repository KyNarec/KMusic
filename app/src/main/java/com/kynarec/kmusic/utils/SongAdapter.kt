package com.kynarec.kmusic.utils

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
import com.kynarec.kmusic.R
import com.kynarec.kmusic.models.Song
import com.kynarec.kmusic.service.PlayerService

class SongAdapter(private val songs: ArrayList<Song>) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val artistTextView: TextView = itemView.findViewById(R.id.song_artist)
        val durationTextView: TextView = itemView.findViewById(R.id.song_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        // Set the song data
        holder.titleTextView.text = song.title
        holder.artistTextView.text = song.artist
        holder.durationTextView.text = song.duration

        // Load thumbnail using Glide
        Glide.with(holder.itemView.context)
            .load(song.thumbnail)
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
            .into(holder.thumbnailImageView)

        // Set click listener to handle selection
        holder.itemView.setOnClickListener {
            // Handle click - you can navigate to detailed view or play the song
            Log.i("Song Adapter", "Song ${song.title} by ${song.artist} was played")
            val intent = Intent(holder.itemView.context, PlayerService::class.java)
            intent.action = "ACTION_PLAY"
            intent.putExtra("SONG_ID", song.id) // Passing string song ID
            holder.itemView.context.startService(intent)
        }
    }

    override fun getItemCount() = songs.size
}
