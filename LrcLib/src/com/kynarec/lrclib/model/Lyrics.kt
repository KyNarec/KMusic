package com.kynarec.lrclib.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Lyrics(
    @SerialName("id") val id: Int,
    @SerialName("trackName") val trackName: String? = null,
    @SerialName("artistName") val artistName: String? = null,
    @SerialName("albumName") val albumName: String? = null,
    @SerialName("duration") val duration: Double? = null,
    @SerialName("instrumental") val instrumental: Boolean,
    @SerialName("plainLyrics") val plainLyrics: String? = null,
    @SerialName("syncedLyrics") val syncedLyrics: String? = null
)
