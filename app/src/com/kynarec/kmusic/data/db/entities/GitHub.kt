package com.kynarec.kmusic.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "GithubRelease")
@Serializable
data class GitHubRelease(
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("tag_name") val tagName: String,
    @PrimaryKey val id: Int,
    val name: String? = null,
    val draft: Boolean = false,
    @SerialName("prerelease") val preRelease: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("published_at") val publishedAt: String,
    val assets: List<GitHubAsset> = emptyList(),
    val body: String = ""
)

@Serializable
data class GitHubAsset(
    val name: String,
    @SerialName("content_type") val contentType: String,
    val size: Long,
    @SerialName("download_count") val downloadCount: Int,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)