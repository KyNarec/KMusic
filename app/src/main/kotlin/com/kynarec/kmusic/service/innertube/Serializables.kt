package com.kynarec.kmusic.service.innertube

import kotlinx.serialization.Serializable

@Serializable
data class FullResponse(
    // We only need the path to the thumbnails
    val videoDetails: VideoDetails? = null
    // You can safely ignore other fields like playerAds, playbackTracking, etc.
)

// 2. The container for the video metadata
@Serializable
data class VideoDetails(
    // The key that holds the thumbnail list
    val thumbnail: ThumbnailContainerBetter? = null,
    val title: String? = null,
    val author: String? = null
    // ... other video details
)

@Serializable
data class ThumbnailContainerBetter(
    // Maps to "thumbnails"
    val thumbnails: List<ThumbnailBetter> = emptyList()
)

@Serializable
data class ThumbnailBetter(
    val url: String,
    val width: Int,
    val height: Int
)

// ========================================== searchSuggestions

@Serializable
data class SearchSuggestionsResponse(
    val contents: List<SuggestionsSection> = emptyList()
)

@Serializable
data class SuggestionsSection(
    val searchSuggestionsSectionRenderer: SearchSuggestionsSectionRenderer? = null
)

@Serializable
data class SearchSuggestionsSectionRenderer(
    val contents: List<SuggestionItem> = emptyList()
)

@Serializable
data class SuggestionItem(
    val searchSuggestionRenderer: SearchSuggestionRenderer? = null
)

@Serializable
data class SearchSuggestionRenderer(
    val suggestion: SuggestionText? = null
)

@Serializable
data class SuggestionText(
    val runs: List<SuggestionRun> = emptyList()
)

@Serializable
data class SuggestionRun(
    val text: String? = null
)

// ========================================== searchSongsFlow
@Serializable
data class SearchResponse(
    val contents: SearchContents? = null
)

@Serializable
data class SearchContents(
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null
)

@Serializable
data class TabbedSearchResultsRenderer(
    val tabs: List<Tab>? = null
)

@Serializable
data class Tab(
    val tabRenderer: TabRenderer? = null
)

@Serializable
data class TabRenderer(
    val content: TabContent? = null
)

@Serializable
data class TabContent(
    val sectionListRenderer: SectionListRenderer? = null
)

@Serializable
data class SectionListRenderer(
    val contents: List<SectionContent>? = null
)

@Serializable
data class SectionContent(
    val musicShelfRenderer: MusicShelfRenderer? = null
)

@Serializable
data class MusicShelfRenderer(
    val contents: List<MusicItem>? = null
)

@Serializable
data class MusicItem(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null
)

@Serializable
data class MusicResponsiveListItemRenderer(
    val flexColumns: List<FlexColumn>? = null,
    val thumbnail: ThumbnailContainer? = null
)

@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null
)

@Serializable
data class FlexColumnRenderer(
    val text: TextRenderer? = null
)

@Serializable
data class TextRenderer(
    val runs: List<TextRun>? = null
)

@Serializable
data class TextRun(
    val text: String? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class NavigationEndpoint(
    val browseEndpoint: BrowseEndpoint? = null,
    val watchEndpoint: WatchEndpoint? = null
)

@Serializable
data class BrowseEndpoint(
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class BrowseEndpointContextSupportedConfigs(
    val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig? = null
)

@Serializable
data class BrowseEndpointContextMusicConfig(
    val pageType: String? = null
)


@Serializable
data class WatchEndpoint(
    val videoId: String? = null
)

@Serializable
data class ThumbnailContainer(
    val musicThumbnailRenderer: MusicThumbnailRenderer? = null
)

@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: Thumbnail? = null
)

@Serializable
data class Thumbnail(
    val thumbnails: List<ThumbnailItem>? = null
)

@Serializable
data class ThumbnailItem(
    val url: String? = null,
    val width: Int,
    val height: Int
)
// ========================================== playSongByIdWithBestBitrate
@Serializable
data class PlayerResponse(
    val streamingData: StreamingData? = null
)

@Serializable
data class StreamingData(
    val adaptiveFormats: List<AdaptiveFormat>? = null
)

@Serializable
data class AdaptiveFormat(
    val averageBitrate: Int? = null,
    val url: String? = null,
    val audioQuality: String? = null
)

// ========================================== betterGetRadio
@Serializable
data class NextResponse(
    val contents: Contents? = null
)

@Serializable
data class Contents(
    val singleColumnWatchNextResults: SingleColumnWatchNextResults? = null
)

@Serializable
data class SingleColumnWatchNextResults(
    val playlist: InnerPlaylistWrapper? = null
)

@Serializable
data class InnerPlaylistWrapper(
    val playlist: Playlist? = null
)

@Serializable
data class Playlist(
    val title: String? = null,
    val contents: List<PlaylistContent>? = null
)

@Serializable
data class PlaylistContent(
    val playlistPanelVideoRenderer: PanelVideoRenderer? = null
)

@Serializable
data class PanelVideoRenderer(
    val videoId: String? = null,
    val title: TextRuns? = null,
    val shortBylineText: TextRuns? = null,
    val lengthText: TextRuns? = null,
    val thumbnail: Thumbnail? = null
)

@Serializable
data class TextRuns(
    val runs: List<Run>? = null
)

@Serializable
data class Run(
    val text: String? = null
)