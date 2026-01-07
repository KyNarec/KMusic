package com.kynarec.kmusic.service.innertube.responses
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchCommunityPlaylistsResponse(
    @SerialName("contents")
    val searchCommunityPlaylistsContents: SearchCommunityPlaylistsContents? = null
)

@Serializable
data class SearchCommunityPlaylistsContents(
    @SerialName("tabbedSearchResultsRenderer")
    val searchCommunityPlaylistsTabbedSearchResultsRenderer: SearchCommunityPlaylistsTabbedSearchResultsRenderer? = null
)

@Serializable
data class SearchCommunityPlaylistsTabbedSearchResultsRenderer(
    @SerialName("tabs")
    val searchCommunityPlaylistsTabs: List<SearchCommunityPlaylistsTab>? = null
)

@Serializable
data class SearchCommunityPlaylistsTab(
    @SerialName("tabRenderer")
    val searchCommunityPlaylistsTabRenderer: SearchCommunityPlaylistsTabRenderer? = null
)

@Serializable
data class SearchCommunityPlaylistsTabRenderer(
    @SerialName("content")
    val searchCommunityPlaylistsContent: SearchCommunityPlaylistsContent? = null
)

@Serializable
data class SearchCommunityPlaylistsContent(
    @SerialName("sectionListRenderer")
    val searchCommunityPlaylistsSectionListRenderer: SearchCommunityPlaylistsSectionListRenderer? = null
)

@Serializable
data class SearchCommunityPlaylistsSectionListRenderer(
    @SerialName("contents")
    val searchCommunityPlaylistsContents: List<SearchCommunityPlaylistsContentX>? = null
)

@Serializable
data class SearchCommunityPlaylistsContentX(
    @SerialName("musicShelfRenderer")
    val searchCommunityPlaylistsMusicShelfRenderer: SearchCommunityPlaylistsMusicShelfRenderer? = null
)

@Serializable
data class SearchCommunityPlaylistsMusicShelfRenderer(
    @SerialName("contents")
    val searchCommunityPlaylistsContents: List<SearchCommunityPlaylistsContentXX>? = null,
    @SerialName("continuations")
    val searchCommunityPlaylistsContinuations: List<SearchCommunityPlaylistsContinuation>? = null
)

@Serializable
data class SearchCommunityPlaylistsContentXX(
    @SerialName("musicResponsiveListItemRenderer")
    val searchCommunityPlaylistsMusicResponsiveListItemRenderer: SearchCommunityPlaylistsMusicResponsiveListItemRenderer? = null
)

@Serializable
data class SearchCommunityPlaylistsContinuation(
    @SerialName("nextContinuationData")
    val searchCommunityPlaylistsNextContinuationData: SearchCommunityPlaylistsNextContinuationData? = null
)

@Serializable
data class SearchCommunityPlaylistsMusicResponsiveListItemRenderer(
    @SerialName("thumbnail")
    val searchCommunityPlaylistsThumbnail: SearchCommunityPlaylistsThumbnail? = null,
    @SerialName("flexColumns")
    val searchCommunityPlaylistsFlexColumns: List<SearchCommunityPlaylistsFlexColumn>? = null,
    @SerialName("navigationEndpoint")
    val searchCommunityPlaylistsNavigationEndpoint: SearchCommunityPlaylistsNavigationEndpointX? = null
)

@Serializable
data class SearchCommunityPlaylistsThumbnail(
    @SerialName("musicThumbnailRenderer")
    val searchCommunityPlaylistsMusicThumbnailRenderer: SearchCommunityPlaylistsMusicThumbnailRenderer? = null
)

@Serializable
data class SearchCommunityPlaylistsFlexColumn(
    @SerialName("musicResponsiveListItemFlexColumnRenderer")
    val searchCommunityPlaylistsMusicResponsiveListItemFlexColumnRenderer: SearchCommunityPlaylistsMusicResponsiveListItemFlexColumnRenderer? = null
)

@Serializable
data class SearchCommunityPlaylistsNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val searchCommunityPlaylistsClickTrackingParams: String? = null,
    @SerialName("browseEndpoint")
    val searchCommunityPlaylistsBrowseEndpoint: SearchCommunityPlaylistsBrowseEndpoint? = null
)

@Serializable
data class SearchCommunityPlaylistsMusicThumbnailRenderer(
    @SerialName("thumbnail")
    val searchCommunityPlaylistsThumbnail: SearchCommunityPlaylistsThumbnailX? = null,
    @SerialName("thumbnailCrop")
    val searchCommunityPlaylistsThumbnailCrop: String? = null,
    @SerialName("thumbnailScale")
    val searchCommunityPlaylistsThumbnailScale: String? = null,
    @SerialName("trackingParams")
    val searchCommunityPlaylistsTrackingParams: String? = null
)

@Serializable
data class SearchCommunityPlaylistsThumbnailX(
    @SerialName("thumbnails")
    val searchCommunityPlaylistsThumbnails: List<SearchCommunityPlaylistsThumbnailXX>? = null
)

@Serializable
data class SearchCommunityPlaylistsThumbnailXX(
    @SerialName("url")
    val searchCommunityPlaylistsUrl: String,
    @SerialName("width")
    val searchCommunityPlaylistsWidth: Int,
    @SerialName("height")
    val searchCommunityPlaylistsHeight: Int

)

@Serializable
data class SearchCommunityPlaylistsMusicResponsiveListItemFlexColumnRenderer(
    @SerialName("text")
    val searchCommunityPlaylistsText: SearchCommunityPlaylistsText? = null,
    @SerialName("displayPriority")
    val searchCommunityPlaylistsDisplayPriority: String?  = null
)

@Serializable
data class SearchCommunityPlaylistsText(
    @SerialName("runs")
    val searchCommunityPlaylistsRuns: List<SearchCommunityPlaylistsRun>? = null
)

@Serializable
data class SearchCommunityPlaylistsRun(
    @SerialName("text")
    val searchCommunityPlaylistsText: String?,
    @SerialName("navigationEndpoint")
    val searchCommunityPlaylistsNavigationEndpoint: SearchCommunityPlaylistsNavigationEndpointX? = null
)

@Serializable
data class SearchCommunityPlaylistsBrowseEndpoint(
    @SerialName("browseId")
    val searchCommunityPlaylistsBrowseId: String?,
    @SerialName("browseEndpointContextSupportedConfigs")
    val searchCommunityPlaylistsBrowseEndpointContextSupportedConfigs: SearchCommunityPlaylistsBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class SearchCommunityPlaylistsBrowseEndpointContextSupportedConfigs(
    @SerialName("browseEndpointContextMusicConfig")
    val searchCommunityPlaylistsBrowseEndpointContextMusicConfig: SearchCommunityPlaylistsBrowseEndpointContextMusicConfig? = null
)

@Serializable
data class SearchCommunityPlaylistsBrowseEndpointContextMusicConfig(
    @SerialName("pageType")
    val searchCommunityPlaylistsPageType: String? = null
)

@Serializable
data class SearchCommunityPlaylistsNextContinuationData(
    @SerialName("continuation")
    val searchCommunityPlaylistsContinuation: String? = null,
    @SerialName("clickTrackingParams")
    val searchCommunityPlaylistsClickTrackingParams: String? = null
)