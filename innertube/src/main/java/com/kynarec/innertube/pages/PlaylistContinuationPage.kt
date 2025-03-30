package com.kynarec.innertube.pages

import com.kynarec.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
