package com.kynarec.kmusic.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

enum class SortBy(val title: String) {
    Position("Position"),
    Title("Title"),
    Artist("Artist"),
    Album("Album"),
    Duration("Duration"),
    DateFavorited("Date Favorited");

    @Composable
    fun getIcon() {
        when (this) {
            Position -> Icon(Icons.Rounded.FormatListNumbered, contentDescription = null)
            Title -> Icon(Icons.Rounded.SortByAlpha, contentDescription = null)
            Artist -> Icon(Icons.Default.Group, contentDescription = null)
            Album -> Icon(Icons.Rounded.Album, contentDescription = null)
            Duration -> Icon(Icons.Rounded.AccessTime, contentDescription = null)
            DateFavorited -> Icon(Icons.Rounded.Favorite, contentDescription = null)
        }
    }

}