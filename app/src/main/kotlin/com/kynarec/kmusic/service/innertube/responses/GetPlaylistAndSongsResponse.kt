package com.kynarec.kmusic.service.innertube.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPlaylistAndSongsResponse(
    @SerialName("responseContext")
    val getPlaylistAndSongsResponseContext: GetPlaylistAndSongsResponseContext? = null,
    @SerialName("contents")
    val getPlaylistAndSongsContents: GetPlaylistAndSongsContents? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("microformat")
    val getPlaylistAndSongsMicroformat: GetPlaylistAndSongsMicroformat? = null,
    @SerialName("background")
    val getPlaylistAndSongsBackground: GetPlaylistAndSongsBackgroundX? = null
)

@Serializable
data class GetPlaylistAndSongsResponseContext(
    @SerialName("visitorData")
    val getPlaylistAndSongsVisitorData: String? = null,
    @SerialName("serviceTrackingParams")
    val getPlaylistAndSongsServiceTrackingParams: List<GetPlaylistAndSongsServiceTrackingParam?>? = null
)

@Serializable
data class GetPlaylistAndSongsContents(
    @SerialName("twoColumnBrowseResultsRenderer")
    val getPlaylistAndSongsTwoColumnBrowseResultsRenderer: GetPlaylistAndSongsTwoColumnBrowseResultsRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsMicroformat(
    @SerialName("microformatDataRenderer")
    val getPlaylistAndSongsMicroformatDataRenderer: GetPlaylistAndSongsMicroformatDataRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsBackgroundX(
    @SerialName("musicThumbnailRenderer")
    val getPlaylistAndSongsMusicThumbnailRenderer: GetPlaylistAndSongsMusicThumbnailRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsServiceTrackingParam(
    @SerialName("service")
    val getPlaylistAndSongsService: String? = null,
    @SerialName("params")
    val getPlaylistAndSongsParams: List<GetPlaylistAndSongsParam?>? = null
)

@Serializable
data class GetPlaylistAndSongsParam(
    @SerialName("key")
    val getPlaylistAndSongsKey: String? = null,
    @SerialName("value")
    val getPlaylistAndSongsValue: String? = null
)

@Serializable
data class GetPlaylistAndSongsTwoColumnBrowseResultsRenderer(
    @SerialName("secondaryContents")
    val getPlaylistAndSongsSecondaryContents: GetPlaylistAndSongsSecondaryContents? = null,
    @SerialName("tabs")
    val getPlaylistAndSongsTabs: List<GetPlaylistAndSongsTab>? = null
)

@Serializable
data class GetPlaylistAndSongsSecondaryContents(
    @SerialName("sectionListRenderer")
    val getPlaylistAndSongsSectionListRenderer: GetPlaylistAndSongsSectionListRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsTab(
    @SerialName("tabRenderer")
    val getPlaylistAndSongsTabRenderer: GetPlaylistAndSongsTabRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsSectionListRenderer(
    @SerialName("contents")
    val getPlaylistAndSongsContents: List<GetPlaylistAndSongsContent>? = null,
    @SerialName("continuations")
    val getPlaylistAndSongsContinuations: List<GetPlaylistAndSongsContinuation>? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsContent(
    @SerialName("musicPlaylistShelfRenderer")
    val getPlaylistAndSongsMusicPlaylistShelfRenderer: GetPlaylistAndSongsMusicPlaylistShelfRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsContinuation(
    @SerialName("nextContinuationData")
    val getPlaylistAndSongsNextContinuationData: GetPlaylistAndSongsNextContinuationData? = null
)

@Serializable
data class GetPlaylistAndSongsMusicPlaylistShelfRenderer(
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null,
    @SerialName("header")
    val getPlaylistAndSongsHeader: GetPlaylistAndSongsHeader? = null,
    @SerialName("contents")
    val getPlaylistAndSongsContents: List<GetPlaylistAndSongsContentX>? = null,
    @SerialName("collapsedItemCount")
    val getPlaylistAndSongsCollapsedItemCount: Int? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("contentsMultiSelectable")
    val getPlaylistAndSongsContentsMultiSelectable: Boolean? = null,
    @SerialName("targetId")
    val getPlaylistAndSongsTargetId: String? = null
)

@Serializable
data class GetPlaylistAndSongsHeader(
    @SerialName("musicSideAlignedItemRenderer")
    val getPlaylistAndSongsMusicSideAlignedItemRenderer: GetPlaylistAndSongsMusicSideAlignedItemRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsContentX(
    @SerialName("musicResponsiveListItemRenderer")
    val getPlaylistAndSongsMusicResponsiveListItemRenderer: GetPlaylistAndSongsMusicResponsiveListItemRenderer? = null,
    @SerialName("continuationItemRenderer")
    val getPlaylistAndSongsContinuationItemRenderer: GetPlaylistAndSongsContinuationItemRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsMusicSideAlignedItemRenderer(
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsMusicResponsiveListItemRenderer(
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("thumbnail")
    val getPlaylistAndSongsThumbnail: GetPlaylistAndSongsThumbnail? = null,
    @SerialName("overlay")
    val getPlaylistAndSongsOverlay: GetPlaylistAndSongsOverlay? = null,
    @SerialName("flexColumns")
    val getPlaylistAndSongsFlexColumns: List<GetPlaylistAndSongsFlexColumn>? = null,
    @SerialName("fixedColumns")
    val getPlaylistAndSongsFixedColumns: List<GetPlaylistAndSongsFixedColumn>? = null,
    @SerialName("menu")
    val getPlaylistAndSongsMenu: GetPlaylistAndSongsMenu? = null,
    @SerialName("playlistItemData")
    val getPlaylistAndSongsPlaylistItemData: GetPlaylistAndSongsPlaylistItemData? = null,
    @SerialName("multiSelectCheckbox")
    val getPlaylistAndSongsMultiSelectCheckbox: GetPlaylistAndSongsMultiSelectCheckbox? = null
)

@Serializable
data class GetPlaylistAndSongsContinuationItemRenderer(
    @SerialName("trigger")
    val getPlaylistAndSongsTrigger: String? = null,
    @SerialName("continuationEndpoint")
    val getPlaylistAndSongsContinuationEndpoint: GetPlaylistAndSongsContinuationEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsThumbnail(
    @SerialName("musicThumbnailRenderer")
    val getPlaylistAndSongsMusicThumbnailRenderer: GetPlaylistAndSongsMusicThumbnailRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsOverlay(
    @SerialName("musicItemThumbnailOverlayRenderer")
    val getPlaylistAndSongsMusicItemThumbnailOverlayRenderer: GetPlaylistAndSongsMusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsFlexColumn(
    @SerialName("musicResponsiveListItemFlexColumnRenderer")
    val getPlaylistAndSongsMusicResponsiveListItemFlexColumnRenderer: GetPlaylistAndSongsMusicResponsiveListItemFlexColumnRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsFixedColumn(
    @SerialName("musicResponsiveListItemFixedColumnRenderer")
    val getPlaylistAndSongsMusicResponsiveListItemFixedColumnRenderer: GetPlaylistAndSongsMusicResponsiveListItemFixedColumnRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsMenu(
    @SerialName("menuRenderer")
    val getPlaylistAndSongsMenuRenderer: GetPlaylistAndSongsMenuRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsPlaylistItemData(
    @SerialName("playlistSetVideoId")
    val getPlaylistAndSongsPlaylistSetVideoId: String? = null,
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("voteSortValue")
    val getPlaylistAndSongsVoteSortValue: Int? = null
)

@Serializable
data class GetPlaylistAndSongsMultiSelectCheckbox(
    @SerialName("checkboxRenderer")
    val getPlaylistAndSongsCheckboxRenderer: GetPlaylistAndSongsCheckboxRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsMusicThumbnailRenderer(
    @SerialName("thumbnail")
    val getPlaylistAndSongsThumbnail: GetPlaylistAndSongsThumbnailX? = null,
    @SerialName("thumbnailCrop")
    val getPlaylistAndSongsThumbnailCrop: String? = null,
    @SerialName("thumbnailScale")
    val getPlaylistAndSongsThumbnailScale: String? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsThumbnailX(
    @SerialName("thumbnails")
    val getPlaylistAndSongsThumbnails: List<GetPlaylistAndSongsThumbnailXX>? = null
)

@Serializable
data class GetPlaylistAndSongsThumbnailXX(
    @SerialName("url")
    val getPlaylistAndSongsUrl: String,
    @SerialName("width")
    val getPlaylistAndSongsWidth: Int,
    @SerialName("height")
    val getPlaylistAndSongsHeight: Int
)

@Serializable
data class GetPlaylistAndSongsMusicItemThumbnailOverlayRenderer(
    @SerialName("background")
    val getPlaylistAndSongsBackground: GetPlaylistAndSongsBackground? = null,
    @SerialName("content")
    val getPlaylistAndSongsContent: GetPlaylistAndSongsContentXX? = null,
    @SerialName("contentPosition")
    val getPlaylistAndSongsContentPosition: String? = null,
    @SerialName("displayStyle")
    val getPlaylistAndSongsDisplayStyle: String? = null
)

@Serializable
data class GetPlaylistAndSongsBackground(
    @SerialName("verticalGradient")
    val getPlaylistAndSongsVerticalGradient: GetPlaylistAndSongsVerticalGradient? = null
)

@Serializable
data class GetPlaylistAndSongsContentXX(
    @SerialName("musicPlayButtonRenderer")
    val getPlaylistAndSongsMusicPlayButtonRenderer: GetPlaylistAndSongsMusicPlayButtonRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsVerticalGradient(
    @SerialName("gradientLayerColors")
    val getPlaylistAndSongsGradientLayerColors: List<String?>? = null
)

@Serializable
data class GetPlaylistAndSongsMusicPlayButtonRenderer(
    @SerialName("playNavigationEndpoint")
    val getPlaylistAndSongsPlayNavigationEndpoint: GetPlaylistAndSongsPlayNavigationEndpoint? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("playIcon")
    val getPlaylistAndSongsPlayIcon: GetPlaylistAndSongsPlayIcon? = null,
    @SerialName("pauseIcon")
    val getPlaylistAndSongsPauseIcon: GetPlaylistAndSongsPauseIcon? = null,
    @SerialName("iconColor")
    val getPlaylistAndSongsIconColor: Long? = null,
    @SerialName("backgroundColor")
    val getPlaylistAndSongsBackgroundColor: Int? = null,
    @SerialName("activeBackgroundColor")
    val getPlaylistAndSongsActiveBackgroundColor: Int? = null,
    @SerialName("loadingIndicatorColor")
    val getPlaylistAndSongsLoadingIndicatorColor: Int? = null,
    @SerialName("playingIcon")
    val getPlaylistAndSongsPlayingIcon: GetPlaylistAndSongsPlayingIcon? = null,
    @SerialName("iconLoadingColor")
    val getPlaylistAndSongsIconLoadingColor: Int? = null,
    @SerialName("activeScaleFactor")
    val getPlaylistAndSongsActiveScaleFactor: Int? = null,
    @SerialName("buttonSize")
    val getPlaylistAndSongsButtonSize: String? = null,
    @SerialName("rippleTarget")
    val getPlaylistAndSongsRippleTarget: String? = null,
    @SerialName("accessibilityPlayData")
    val getPlaylistAndSongsAccessibilityPlayData: GetPlaylistAndSongsAccessibilityPlayData? = null,
    @SerialName("accessibilityPauseData")
    val getPlaylistAndSongsAccessibilityPauseData: GetPlaylistAndSongsAccessibilityPauseData? = null
)

@Serializable
data class GetPlaylistAndSongsPlayNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val getPlaylistAndSongsWatchEndpoint: GetPlaylistAndSongsWatchEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsPlayIcon(
    @SerialName("iconType")
    val getPlaylistAndSongsIconType: String? = null
)

@Serializable
data class GetPlaylistAndSongsPauseIcon(
    @SerialName("iconType")
    val getPlaylistAndSongsIconType: String? = null
)

@Serializable
data class GetPlaylistAndSongsPlayingIcon(
    @SerialName("iconType")
    val getPlaylistAndSongsIconType: String? = null
)

@Serializable
data class GetPlaylistAndSongsAccessibilityPlayData(
    @SerialName("accessibilityData")
    val getPlaylistAndSongsAccessibilityData: GetPlaylistAndSongsAccessibilityData? = null
)

@Serializable
data class GetPlaylistAndSongsAccessibilityPauseData(
    @SerialName("accessibilityData")
    val getPlaylistAndSongsAccessibilityData: GetPlaylistAndSongsAccessibilityData? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpoint(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null,
    @SerialName("playerParams")
    val getPlaylistAndSongsPlayerParams: String? = null,
    @SerialName("playlistSetVideoId")
    val getPlaylistAndSongsPlaylistSetVideoId: String? = null,
    @SerialName("loggingContext")
    val getPlaylistAndSongsLoggingContext: GetPlaylistAndSongsLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val getPlaylistAndSongsWatchEndpointMusicSupportedConfigs: GetPlaylistAndSongsWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class GetPlaylistAndSongsLoggingContext(
    @SerialName("vssLoggingContext")
    val getPlaylistAndSongsVssLoggingContext: GetPlaylistAndSongsVssLoggingContext? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpointMusicSupportedConfigs(
    @SerialName("watchEndpointMusicConfig")
    val getPlaylistAndSongsWatchEndpointMusicConfig: GetPlaylistAndSongsWatchEndpointMusicConfig? = null
)

@Serializable
data class GetPlaylistAndSongsVssLoggingContext(
    @SerialName("serializedContextData")
    val getPlaylistAndSongsSerializedContextData: String? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpointMusicConfig(
    @SerialName("musicVideoType")
    val getPlaylistAndSongsMusicVideoType: String? = null
)

@Serializable
data class GetPlaylistAndSongsAccessibilityData(
    @SerialName("label")
    val getPlaylistAndSongsLabel: String? = null
)

@Serializable
data class GetPlaylistAndSongsMusicResponsiveListItemFlexColumnRenderer(
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsText? = null,
    @SerialName("displayPriority")
    val getPlaylistAndSongsDisplayPriority: String? = null
)

@Serializable
data class GetPlaylistAndSongsText(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRun>? = null
)

@Serializable
data class GetPlaylistAndSongsRun(
    @SerialName("text")
    val getPlaylistAndSongsText: String? = null,
    @SerialName("navigationEndpoint")
    val getPlaylistAndSongsNavigationEndpoint: GetPlaylistAndSongsNavigationEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val getPlaylistAndSongsWatchEndpoint: GetPlaylistAndSongsWatchEndpointX? = null,
    @SerialName("browseEndpoint")
    val getPlaylistAndSongsBrowseEndpoint: GetPlaylistAndSongsBrowseEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpointX(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null,
    @SerialName("playerParams")
    val getPlaylistAndSongsPlayerParams: String? = null,
    @SerialName("loggingContext")
    val getPlaylistAndSongsLoggingContext: GetPlaylistAndSongsLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val getPlaylistAndSongsWatchEndpointMusicSupportedConfigs: GetPlaylistAndSongsWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class GetPlaylistAndSongsBrowseEndpoint(
    @SerialName("browseId")
    val getPlaylistAndSongsBrowseId: String? = null,
    @SerialName("browseEndpointContextSupportedConfigs")
    val getPlaylistAndSongsBrowseEndpointContextSupportedConfigs: GetPlaylistAndSongsBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class GetPlaylistAndSongsBrowseEndpointContextSupportedConfigs(
    @SerialName("browseEndpointContextMusicConfig")
    val getPlaylistAndSongsBrowseEndpointContextMusicConfig: GetPlaylistAndSongsBrowseEndpointContextMusicConfig? = null
)

@Serializable
data class GetPlaylistAndSongsBrowseEndpointContextMusicConfig(
    @SerialName("pageType")
    val getPlaylistAndSongsPageType: String? = null
)

@Serializable
data class GetPlaylistAndSongsMusicResponsiveListItemFixedColumnRenderer(
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsTextX? = null,
    @SerialName("displayPriority")
    val getPlaylistAndSongsDisplayPriority: String? = null,
    @SerialName("size")
    val getPlaylistAndSongsSize: String? = null
)

@Serializable
data class GetPlaylistAndSongsTextX(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRunX>? = null,
    @SerialName("accessibility")
    val getPlaylistAndSongsAccessibility: GetPlaylistAndSongsAccessibility? = null
)

@Serializable
data class GetPlaylistAndSongsRunX(
    @SerialName("text")
    val getPlaylistAndSongsText: String? = null
)

@Serializable
data class GetPlaylistAndSongsAccessibility(
    @SerialName("accessibilityData")
    val getPlaylistAndSongsAccessibilityData: GetPlaylistAndSongsAccessibilityData? = null
)

@Serializable
data class GetPlaylistAndSongsMenuRenderer(
    @SerialName("items")
    val getPlaylistAndSongsItems: List<GetPlaylistAndSongsItem>? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("topLevelButtons")
    val getPlaylistAndSongsTopLevelButtons: List<GetPlaylistAndSongsTopLevelButton>? = null,
    @SerialName("accessibility")
    val getPlaylistAndSongsAccessibility: GetPlaylistAndSongsAccessibility? = null
)

@Serializable
data class GetPlaylistAndSongsItem(
    @SerialName("menuNavigationItemRenderer")
    val getPlaylistAndSongsMenuNavigationItemRenderer: GetPlaylistAndSongsMenuNavigationItemRenderer? = null,
    @SerialName("menuServiceItemRenderer")
    val getPlaylistAndSongsMenuServiceItemRenderer: GetPlaylistAndSongsMenuServiceItemRenderer? = null,
    @SerialName("menuServiceItemDownloadRenderer")
    val getPlaylistAndSongsMenuServiceItemDownloadRenderer: GetPlaylistAndSongsMenuServiceItemDownloadRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsTopLevelButton(
    @SerialName("likeButtonRenderer")
    val getPlaylistAndSongsLikeButtonRenderer: GetPlaylistAndSongsLikeButtonRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsMenuNavigationItemRenderer(
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsTextXX? = null,
    @SerialName("icon")
    val getPlaylistAndSongsIcon: GetPlaylistAndSongsIcon? = null,
    @SerialName("navigationEndpoint")
    val getPlaylistAndSongsNavigationEndpoint: GetPlaylistAndSongsNavigationEndpointX? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsMenuServiceItemRenderer(
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsTextXX? = null,
    @SerialName("icon")
    val getPlaylistAndSongsIcon: GetPlaylistAndSongsIcon? = null,
    @SerialName("serviceEndpoint")
    val getPlaylistAndSongsServiceEndpoint: GetPlaylistAndSongsServiceEndpoint? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsMenuServiceItemDownloadRenderer(
    @SerialName("serviceEndpoint")
    val getPlaylistAndSongsServiceEndpoint: GetPlaylistAndSongsServiceEndpointX? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("badgeIcon")
    val getPlaylistAndSongsBadgeIcon: GetPlaylistAndSongsBadgeIcon? = null
)

@Serializable
data class GetPlaylistAndSongsTextXX(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRunX>? = null
)

@Serializable
data class GetPlaylistAndSongsIcon(
    @SerialName("iconType")
    val getPlaylistAndSongsIconType: String? = null
)

@Serializable
data class GetPlaylistAndSongsNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val getPlaylistAndSongsWatchEndpoint: GetPlaylistAndSongsWatchEndpointXX? = null,
    @SerialName("modalEndpoint")
    val getPlaylistAndSongsModalEndpoint: GetPlaylistAndSongsModalEndpoint? = null,
    @SerialName("shareEntityEndpoint")
    val getPlaylistAndSongsShareEntityEndpoint: GetPlaylistAndSongsShareEntityEndpoint? = null,
    @SerialName("browseEndpoint")
    val getPlaylistAndSongsBrowseEndpoint: GetPlaylistAndSongsBrowseEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpointXX(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null,
    @SerialName("params")
    val getPlaylistAndSongsParams: String? = null,
    @SerialName("loggingContext")
    val getPlaylistAndSongsLoggingContext: GetPlaylistAndSongsLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val getPlaylistAndSongsWatchEndpointMusicSupportedConfigs: GetPlaylistAndSongsWatchEndpointMusicSupportedConfigs? = null,
    @SerialName("playerParams")
    val getPlaylistAndSongsPlayerParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsModalEndpoint(
    @SerialName("modal")
    val getPlaylistAndSongsModal: GetPlaylistAndSongsModal? = null
)

@Serializable
data class GetPlaylistAndSongsShareEntityEndpoint(
    @SerialName("serializedShareEntity")
    val getPlaylistAndSongsSerializedShareEntity: String? = null,
    @SerialName("sharePanelType")
    val getPlaylistAndSongsSharePanelType: String? = null
)

@Serializable
data class GetPlaylistAndSongsModal(
    @SerialName("modalWithTitleAndButtonRenderer")
    val getPlaylistAndSongsModalWithTitleAndButtonRenderer: GetPlaylistAndSongsModalWithTitleAndButtonRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsModalWithTitleAndButtonRenderer(
    @SerialName("title")
    val getPlaylistAndSongsTitle: GetPlaylistAndSongsTitle? = null,
    @SerialName("content")
    val getPlaylistAndSongsContent: GetPlaylistAndSongsContentXXX? = null,
    @SerialName("button")
    val getPlaylistAndSongsButton: GetPlaylistAndSongsButton? = null
)

@Serializable
data class GetPlaylistAndSongsTitle(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRunX>? = null
)

@Serializable
data class GetPlaylistAndSongsContentXXX(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRunX>? = null
)

@Serializable
data class GetPlaylistAndSongsButton(
    @SerialName("buttonRenderer")
    val getPlaylistAndSongsButtonRenderer: GetPlaylistAndSongsButtonRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsButtonRenderer(
    @SerialName("style")
    val getPlaylistAndSongsStyle: String? = null,
    @SerialName("isDisabled")
    val getPlaylistAndSongsIsDisabled: Boolean? = null,
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsTextXX? = null,
    @SerialName("navigationEndpoint")
    val getPlaylistAndSongsNavigationEndpoint: GetPlaylistAndSongsNavigationEndpointXX? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsNavigationEndpointXX(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("signInEndpoint")
    val getPlaylistAndSongsSignInEndpoint: GetPlaylistAndSongsSignInEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsSignInEndpoint(
    @SerialName("hack")
    val getPlaylistAndSongsHack: Boolean? = null
)

@Serializable
data class GetPlaylistAndSongsServiceEndpoint(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("queueAddEndpoint")
    val getPlaylistAndSongsQueueAddEndpoint: GetPlaylistAndSongsQueueAddEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsQueueAddEndpoint(
    @SerialName("queueTarget")
    val getPlaylistAndSongsQueueTarget: GetPlaylistAndSongsQueueTarget? = null,
    @SerialName("queueInsertPosition")
    val getPlaylistAndSongsQueueInsertPosition: String? = null,
    @SerialName("commands")
    val getPlaylistAndSongsCommands: List<GetPlaylistAndSongsCommand>? = null
)

@Serializable
data class GetPlaylistAndSongsQueueTarget(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("onEmptyQueue")
    val getPlaylistAndSongsOnEmptyQueue: GetPlaylistAndSongsOnEmptyQueue? = null
)

@Serializable
data class GetPlaylistAndSongsCommand(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("addToToastAction")
    val getPlaylistAndSongsAddToToastAction: GetPlaylistAndSongsAddToToastAction? = null
)

@Serializable
data class GetPlaylistAndSongsOnEmptyQueue(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val getPlaylistAndSongsWatchEndpoint: GetPlaylistAndSongsWatchEndpointXXX? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpointXXX(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null
)

@Serializable
data class GetPlaylistAndSongsAddToToastAction(
    @SerialName("item")
    val getPlaylistAndSongsItem: GetPlaylistAndSongsItemX? = null
)

@Serializable
data class GetPlaylistAndSongsItemX(
    @SerialName("notificationTextRenderer")
    val getPlaylistAndSongsNotificationTextRenderer: GetPlaylistAndSongsNotificationTextRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsNotificationTextRenderer(
    @SerialName("successResponseText")
    val getPlaylistAndSongsSuccessResponseText: GetPlaylistAndSongsSuccessResponseText? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsSuccessResponseText(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRunX>? = null
)

@Serializable
data class GetPlaylistAndSongsServiceEndpointX(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("offlineVideoEndpoint")
    val getPlaylistAndSongsOfflineVideoEndpoint: GetPlaylistAndSongsOfflineVideoEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsBadgeIcon(
    @SerialName("iconType")
    val getPlaylistAndSongsIconType: String? = null
)

@Serializable
data class GetPlaylistAndSongsOfflineVideoEndpoint(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("onAddCommand")
    val getPlaylistAndSongsOnAddCommand: GetPlaylistAndSongsOnAddCommand? = null
)

@Serializable
data class GetPlaylistAndSongsOnAddCommand(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("getDownloadActionCommand")
    val getPlaylistAndSongsGetDownloadActionCommand: GetPlaylistAndSongsGetDownloadActionCommand? = null
)

@Serializable
data class GetPlaylistAndSongsGetDownloadActionCommand(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("params")
    val getPlaylistAndSongsParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsLikeButtonRenderer(
    @SerialName("target")
    val getPlaylistAndSongsTarget: GetPlaylistAndSongsTarget? = null,
    @SerialName("likeStatus")
    val getPlaylistAndSongsLikeStatus: String? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("likesAllowed")
    val getPlaylistAndSongsLikesAllowed: Boolean? = null,
    @SerialName("dislikeNavigationEndpoint")
    val getPlaylistAndSongsDislikeNavigationEndpoint: GetPlaylistAndSongsDislikeNavigationEndpoint? = null,
    @SerialName("likeCommand")
    val getPlaylistAndSongsLikeCommand: GetPlaylistAndSongsLikeCommand? = null
)

@Serializable
data class GetPlaylistAndSongsTarget(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null
)

@Serializable
data class GetPlaylistAndSongsDislikeNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val getPlaylistAndSongsModalEndpoint: GetPlaylistAndSongsModalEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsLikeCommand(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val getPlaylistAndSongsModalEndpoint: GetPlaylistAndSongsModalEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsCheckboxRenderer(
    @SerialName("onSelectionChangeCommand")
    val getPlaylistAndSongsOnSelectionChangeCommand: GetPlaylistAndSongsOnSelectionChangeCommand? = null,
    @SerialName("checkedState")
    val getPlaylistAndSongsCheckedState: String? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsOnSelectionChangeCommand(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("updateMultiSelectStateCommand")
    val getPlaylistAndSongsUpdateMultiSelectStateCommand: GetPlaylistAndSongsUpdateMultiSelectStateCommand? = null
)

@Serializable
data class GetPlaylistAndSongsUpdateMultiSelectStateCommand(
    @SerialName("multiSelectParams")
    val getPlaylistAndSongsMultiSelectParams: String? = null,
    @SerialName("multiSelectItem")
    val getPlaylistAndSongsMultiSelectItem: String? = null
)

@Serializable
data class GetPlaylistAndSongsContinuationEndpoint(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("continuationCommand")
    val getPlaylistAndSongsContinuationCommand: GetPlaylistAndSongsContinuationCommand? = null
)

@Serializable
data class GetPlaylistAndSongsContinuationCommand(
    @SerialName("token")
    val getPlaylistAndSongsToken: String? = null,
    @SerialName("request")
    val getPlaylistAndSongsRequest: String? = null
)

@Serializable
data class GetPlaylistAndSongsNextContinuationData(
    @SerialName("continuation")
    val getPlaylistAndSongsContinuation: String? = null,
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsTabRenderer(
    @SerialName("content")
    val getPlaylistAndSongsContent: GetPlaylistAndSongsContentXXXXXX? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsContentXXXXXX(
    @SerialName("sectionListRenderer")
    val getPlaylistAndSongsSectionListRenderer: GetPlaylistAndSongsSectionListRendererX? = null
)

@Serializable
data class GetPlaylistAndSongsSectionListRendererX(
    @SerialName("contents")
    val getPlaylistAndSongsContents: List<GetPlaylistAndSongsContentXXXXXXX>? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsContentXXXXXXX(
    @SerialName("musicResponsiveHeaderRenderer")
    val getPlaylistAndSongsMusicResponsiveHeaderRenderer: GetPlaylistAndSongsMusicResponsiveHeaderRenderer? = null
)

@Serializable
data class GetPlaylistAndSongsMusicResponsiveHeaderRenderer(
    @SerialName("thumbnail")
    val getPlaylistAndSongsThumbnail: GetPlaylistAndSongsThumbnail? = null,
    @SerialName("buttons")
    val getPlaylistAndSongsButtons: List<GetPlaylistAndSongsButtonXXX>? = null,
    @SerialName("title")
    val getPlaylistAndSongsTitle: GetPlaylistAndSongsTitle? = null,
    @SerialName("subtitle")
    val getPlaylistAndSongsSubtitle: GetPlaylistAndSongsSubtitle? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("secondSubtitle")
    val getPlaylistAndSongsSecondSubtitle: GetPlaylistAndSongsSecondSubtitle? = null,
    @SerialName("facepile")
    val getPlaylistAndSongsFacepile: GetPlaylistAndSongsFacepile? = null
)

@Serializable
data class GetPlaylistAndSongsButtonXXX(
    @SerialName("toggleButtonRenderer")
    val getPlaylistAndSongsToggleButtonRenderer: GetPlaylistAndSongsToggleButtonRenderer? = null,
    @SerialName("musicPlayButtonRenderer")
    val getPlaylistAndSongsMusicPlayButtonRenderer: GetPlaylistAndSongsMusicPlayButtonRendererX? = null,
    @SerialName("menuRenderer")
    val getPlaylistAndSongsMenuRenderer: GetPlaylistAndSongsMenuRendererX? = null
)

@Serializable
data class GetPlaylistAndSongsSubtitle(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRunX>? = null
)

@Serializable
data class GetPlaylistAndSongsSecondSubtitle(
    @SerialName("runs")
    val getPlaylistAndSongsRuns: List<GetPlaylistAndSongsRunX>? = null
)

@Serializable
data class GetPlaylistAndSongsFacepile(
    @SerialName("avatarStackViewModel")
    val getPlaylistAndSongsAvatarStackViewModel: GetPlaylistAndSongsAvatarStackViewModel? = null
)

@Serializable
data class GetPlaylistAndSongsToggleButtonRenderer(
    @SerialName("isToggled")
    val getPlaylistAndSongsIsToggled: Boolean? = null,
    @SerialName("isDisabled")
    val getPlaylistAndSongsIsDisabled: Boolean? = null,
    @SerialName("defaultIcon")
    val getPlaylistAndSongsDefaultIcon: GetPlaylistAndSongsDefaultIcon? = null,
    @SerialName("toggledIcon")
    val getPlaylistAndSongsToggledIcon: GetPlaylistAndSongsToggledIcon? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("defaultNavigationEndpoint")
    val getPlaylistAndSongsDefaultNavigationEndpoint: GetPlaylistAndSongsDefaultNavigationEndpoint? = null,
    @SerialName("accessibilityData")
    val getPlaylistAndSongsAccessibilityData: GetPlaylistAndSongsAccessibilityDataXXXX? = null,
    @SerialName("toggledAccessibilityData")
    val getPlaylistAndSongsToggledAccessibilityData: GetPlaylistAndSongsToggledAccessibilityData? = null
)

@Serializable
data class GetPlaylistAndSongsMusicPlayButtonRendererX(
    @SerialName("playNavigationEndpoint")
    val getPlaylistAndSongsPlayNavigationEndpoint: GetPlaylistAndSongsPlayNavigationEndpointX? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("playIcon")
    val getPlaylistAndSongsPlayIcon: GetPlaylistAndSongsPlayIcon? = null,
    @SerialName("pauseIcon")
    val getPlaylistAndSongsPauseIcon: GetPlaylistAndSongsPauseIcon? = null,
    @SerialName("iconColor")
    val getPlaylistAndSongsIconColor: Long? = null,
    @SerialName("backgroundColor")
    val getPlaylistAndSongsBackgroundColor: Int? = null,
    @SerialName("activeBackgroundColor")
    val getPlaylistAndSongsActiveBackgroundColor: Int? = null,
    @SerialName("loadingIndicatorColor")
    val getPlaylistAndSongsLoadingIndicatorColor: Int? = null,
    @SerialName("playingIcon")
    val getPlaylistAndSongsPlayingIcon: GetPlaylistAndSongsPlayingIcon? = null,
    @SerialName("iconLoadingColor")
    val getPlaylistAndSongsIconLoadingColor: Int? = null,
    @SerialName("activeScaleFactor")
    val getPlaylistAndSongsActiveScaleFactor: Int? = null,
    @SerialName("accessibilityPlayData")
    val getPlaylistAndSongsAccessibilityPlayData: GetPlaylistAndSongsAccessibilityPlayData? = null,
    @SerialName("accessibilityPauseData")
    val getPlaylistAndSongsAccessibilityPauseData: GetPlaylistAndSongsAccessibilityPauseData? = null
)

@Serializable
data class GetPlaylistAndSongsMenuRendererX(
    @SerialName("items")
    val getPlaylistAndSongsItems: List<GetPlaylistAndSongsItemXX>? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null,
    @SerialName("accessibility")
    val getPlaylistAndSongsAccessibility: GetPlaylistAndSongsAccessibility? = null
)

@Serializable
data class GetPlaylistAndSongsDefaultIcon(
    @SerialName("iconType")
    val getPlaylistAndSongsIconType: String? = null
)

@Serializable
data class GetPlaylistAndSongsToggledIcon(
    @SerialName("iconType")
    val getPlaylistAndSongsIconType: String? = null
)

@Serializable
data class GetPlaylistAndSongsDefaultNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val getPlaylistAndSongsModalEndpoint: GetPlaylistAndSongsModalEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsAccessibilityDataXXXX(
    @SerialName("accessibilityData")
    val getPlaylistAndSongsAccessibilityData: GetPlaylistAndSongsAccessibilityData? = null
)

@Serializable
data class GetPlaylistAndSongsToggledAccessibilityData(
    @SerialName("accessibilityData")
    val getPlaylistAndSongsAccessibilityData: GetPlaylistAndSongsAccessibilityData? = null
)

@Serializable
data class GetPlaylistAndSongsPlayNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val getPlaylistAndSongsWatchEndpoint: GetPlaylistAndSongsWatchEndpointXXXX? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpointXXXX(
    @SerialName("videoId")
    val getPlaylistAndSongsVideoId: String? = null,
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null,
    @SerialName("params")
    val getPlaylistAndSongsParams: String? = null,
    @SerialName("playerParams")
    val getPlaylistAndSongsPlayerParams: String? = null,
    @SerialName("loggingContext")
    val getPlaylistAndSongsLoggingContext: GetPlaylistAndSongsLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val getPlaylistAndSongsWatchEndpointMusicSupportedConfigs: GetPlaylistAndSongsWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class GetPlaylistAndSongsItemXX(
    @SerialName("menuNavigationItemRenderer")
    val getPlaylistAndSongsMenuNavigationItemRenderer: GetPlaylistAndSongsMenuNavigationItemRendererX? = null,
    @SerialName("menuServiceItemRenderer")
    val getPlaylistAndSongsMenuServiceItemRenderer: GetPlaylistAndSongsMenuServiceItemRendererX? = null
)

@Serializable
data class GetPlaylistAndSongsMenuNavigationItemRendererX(
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsTextXX? = null,
    @SerialName("icon")
    val getPlaylistAndSongsIcon: GetPlaylistAndSongsIcon? = null,
    @SerialName("navigationEndpoint")
    val getPlaylistAndSongsNavigationEndpoint: GetPlaylistAndSongsNavigationEndpointXXXXXX? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsMenuServiceItemRendererX(
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsTextXX? = null,
    @SerialName("icon")
    val getPlaylistAndSongsIcon: GetPlaylistAndSongsIcon? = null,
    @SerialName("serviceEndpoint")
    val getPlaylistAndSongsServiceEndpoint: GetPlaylistAndSongsServiceEndpointXX? = null,
    @SerialName("trackingParams")
    val getPlaylistAndSongsTrackingParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsNavigationEndpointXXXXXX(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("watchPlaylistEndpoint")
    val getPlaylistAndSongsWatchPlaylistEndpoint: GetPlaylistAndSongsWatchPlaylistEndpoint? = null,
    @SerialName("modalEndpoint")
    val getPlaylistAndSongsModalEndpoint: GetPlaylistAndSongsModalEndpoint? = null,
    @SerialName("shareEntityEndpoint")
    val getPlaylistAndSongsShareEntityEndpoint: GetPlaylistAndSongsShareEntityEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsWatchPlaylistEndpoint(
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null,
    @SerialName("params")
    val getPlaylistAndSongsParams: String? = null
)

@Serializable
data class GetPlaylistAndSongsServiceEndpointXX(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("queueAddEndpoint")
    val getPlaylistAndSongsQueueAddEndpoint: GetPlaylistAndSongsQueueAddEndpointX? = null
)

@Serializable
data class GetPlaylistAndSongsQueueAddEndpointX(
    @SerialName("queueTarget")
    val getPlaylistAndSongsQueueTarget: GetPlaylistAndSongsQueueTargetX? = null,
    @SerialName("queueInsertPosition")
    val getPlaylistAndSongsQueueInsertPosition: String? = null,
    @SerialName("commands")
    val getPlaylistAndSongsCommands: List<GetPlaylistAndSongsCommand>? = null
)

@Serializable
data class GetPlaylistAndSongsQueueTargetX(
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null,
    @SerialName("onEmptyQueue")
    val getPlaylistAndSongsOnEmptyQueue: GetPlaylistAndSongsOnEmptyQueueX? = null
)

@Serializable
data class GetPlaylistAndSongsOnEmptyQueueX(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val getPlaylistAndSongsWatchEndpoint: GetPlaylistAndSongsWatchEndpointXXXXX? = null
)

@Serializable
data class GetPlaylistAndSongsWatchEndpointXXXXX(
    @SerialName("playlistId")
    val getPlaylistAndSongsPlaylistId: String? = null
)

@Serializable
data class GetPlaylistAndSongsAvatarStackViewModel(
    @SerialName("avatars")
    val getPlaylistAndSongsAvatars: List<GetPlaylistAndSongsAvatar>? = null,
    @SerialName("text")
    val getPlaylistAndSongsText: GetPlaylistAndSongsTextXXXXXXXXXXX? = null,
    @SerialName("rendererContext")
    val getPlaylistAndSongsRendererContext: GetPlaylistAndSongsRendererContext? = null
)

@Serializable
data class GetPlaylistAndSongsAvatar(
    @SerialName("avatarViewModel")
    val getPlaylistAndSongsAvatarViewModel: GetPlaylistAndSongsAvatarViewModel? = null
)

@Serializable
data class GetPlaylistAndSongsTextXXXXXXXXXXX(
    @SerialName("content")
    val getPlaylistAndSongsContent: String? = null
)

@Serializable
data class GetPlaylistAndSongsRendererContext(
    @SerialName("accessibilityContext")
    val getPlaylistAndSongsAccessibilityContext: GetPlaylistAndSongsAccessibilityContext? = null,
    @SerialName("commandContext")
    val getPlaylistAndSongsCommandContext: GetPlaylistAndSongsCommandContext? = null
)

@Serializable
data class GetPlaylistAndSongsAvatarViewModel(
    @SerialName("image")
    val getPlaylistAndSongsImage: GetPlaylistAndSongsImage? = null,
    @SerialName("avatarImageSize")
    val getPlaylistAndSongsAvatarImageSize: String? = null
)

@Serializable
data class GetPlaylistAndSongsImage(
    @SerialName("sources")
    val getPlaylistAndSongsSources: List<GetPlaylistAndSongsSource?>? = null,
    @SerialName("processor")
    val getPlaylistAndSongsProcessor: GetPlaylistAndSongsProcessor? = null
)

@Serializable
data class GetPlaylistAndSongsSource(
    @SerialName("url")
    val getPlaylistAndSongsUrl: String? = null
)

@Serializable
data class GetPlaylistAndSongsProcessor(
    @SerialName("borderImageProcessor")
    val getPlaylistAndSongsBorderImageProcessor: GetPlaylistAndSongsBorderImageProcessor? = null
)

@Serializable
data class GetPlaylistAndSongsBorderImageProcessor(
    @SerialName("circular")
    val getPlaylistAndSongsCircular: Boolean? = null
)

@Serializable
data class GetPlaylistAndSongsAccessibilityContext(
    @SerialName("label")
    val getPlaylistAndSongsLabel: String? = null
)

@Serializable
data class GetPlaylistAndSongsCommandContext(
    @SerialName("onTap")
    val getPlaylistAndSongsOnTap: GetPlaylistAndSongsOnTap? = null
)

@Serializable
data class GetPlaylistAndSongsOnTap(
    @SerialName("innertubeCommand")
    val getPlaylistAndSongsInnertubeCommand: GetPlaylistAndSongsInnertubeCommand? = null
)

@Serializable
data class GetPlaylistAndSongsInnertubeCommand(
    @SerialName("clickTrackingParams")
    val getPlaylistAndSongsClickTrackingParams: String? = null,
    @SerialName("browseEndpoint")
    val getPlaylistAndSongsBrowseEndpoint: GetPlaylistAndSongsBrowseEndpoint? = null
)

@Serializable
data class GetPlaylistAndSongsMicroformatDataRenderer(
    @SerialName("urlCanonical")
    val getPlaylistAndSongsUrlCanonical: String? = null,
    @SerialName("title")
    val getPlaylistAndSongsTitle: String? = null,
    @SerialName("description")
    val getPlaylistAndSongsDescription: String? = null,
    @SerialName("thumbnail")
    val getPlaylistAndSongsThumbnail: GetPlaylistAndSongsThumbnailX? = null,
    @SerialName("siteName")
    val getPlaylistAndSongsSiteName: String? = null,
    @SerialName("appName")
    val getPlaylistAndSongsAppName: String? = null,
    @SerialName("androidPackage")
    val getPlaylistAndSongsAndroidPackage: String? = null,
    @SerialName("iosAppStoreId")
    val getPlaylistAndSongsIosAppStoreId: String? = null,
    @SerialName("ogType")
    val getPlaylistAndSongsOgType: String? = null,
    @SerialName("urlApplinksWeb")
    val getPlaylistAndSongsUrlApplinksWeb: String? = null,
    @SerialName("urlApplinksIos")
    val getPlaylistAndSongsUrlApplinksIos: String? = null,
    @SerialName("urlApplinksAndroid")
    val getPlaylistAndSongsUrlApplinksAndroid: String? = null,
    @SerialName("urlTwitterIos")
    val getPlaylistAndSongsUrlTwitterIos: String? = null,
    @SerialName("urlTwitterAndroid")
    val getPlaylistAndSongsUrlTwitterAndroid: String? = null,
    @SerialName("twitterCardType")
    val getPlaylistAndSongsTwitterCardType: String? = null,
    @SerialName("twitterSiteHandle")
    val getPlaylistAndSongsTwitterSiteHandle: String? = null
)