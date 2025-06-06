package com.kynarec.kmusic.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.entities.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope


class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView = itemView.findViewById<TextView>(R.id.song_title)
        private val artistTextView = itemView.findViewById<TextView>(R.id.song_artist)
        private val durationTextView = itemView.findViewById<TextView>(R.id.song_duration)
        private val thumbnailImageView = itemView.findViewById<ImageView>(R.id.imageView)

        private var marqueeRunnable: Runnable? = null

        fun bind(song: Song) {
            titleTextView.text = song.title
            titleTextView.isSelected = false

            // Remove any pending marquee start
            marqueeRunnable?.let { titleTextView.removeCallbacks(it) }

            // Create new runnable
            marqueeRunnable = Runnable {
                titleTextView.isSelected = true
            }

            // Post with delay
            titleTextView.postDelayed(marqueeRunnable, MARQUEE_DELAY)

            artistTextView.text = song.artist
            artistTextView.isSelected = false

            // Remove any pending marquee start
            marqueeRunnable?.let { artistTextView.removeCallbacks(it) }

            // Create new runnable
            marqueeRunnable = Runnable {
                artistTextView.isSelected = true
            }

            // Post with delay
            artistTextView.postDelayed(marqueeRunnable, MARQUEE_DELAY)

            durationTextView.text = song.duration

            Glide.with(itemView.context)
                .load(song.thumbnail)
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(THUMBNAIL_ROUNDNESS)))
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
