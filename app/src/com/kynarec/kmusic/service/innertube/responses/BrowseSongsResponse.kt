package innertube.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrowseSongsResponse(
    @SerialName("responseContext")
    val browseSongsResponseContext: BrowseSongsResponseContext? = null,
    @SerialName("contents")
    val browseSongsContents: BrowseSongsContents? = null,
    @SerialName("header")
    val browseSongsHeader: BrowseSongsHeaderX? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("microformat")
    val browseSongsMicroformat: BrowseSongsMicroformat? = null
)

@Serializable
data class BrowseSongsResponseContext(
    @SerialName("visitorData")
    val browseSongsVisitorData: String? = null,
    @SerialName("serviceTrackingParams")
    val browseSongsServiceTrackingParams: List<BrowseSongsServiceTrackingParam?>? = null
)

@Serializable
data class BrowseSongsContents(
    @SerialName("singleColumnBrowseResultsRenderer")
    val browseSongsSingleColumnBrowseResultsRenderer: BrowseSongsSingleColumnBrowseResultsRenderer? = null
)

@Serializable
data class BrowseSongsHeaderX(
    @SerialName("musicHeaderRenderer")
    val browseSongsMusicHeaderRenderer: BrowseSongsMusicHeaderRenderer? = null
)

@Serializable
data class BrowseSongsMicroformat(
    @SerialName("microformatDataRenderer")
    val browseSongsMicroformatDataRenderer: BrowseSongsMicroformatDataRenderer? = null
)

@Serializable
data class BrowseSongsServiceTrackingParam(
    @SerialName("service")
    val browseSongsService: String? = null,
    @SerialName("params")
    val browseSongsParams: List<BrowseSongsParam?>? = null
)

@Serializable
data class BrowseSongsParam(
    @SerialName("key")
    val browseSongsKey: String? = null,
    @SerialName("value")
    val browseSongsValue: String? = null
)

@Serializable
data class BrowseSongsSingleColumnBrowseResultsRenderer(
    @SerialName("tabs")
    val browseSongsTabs: List<BrowseSongsTab>? = null
)

@Serializable
data class BrowseSongsTab(
    @SerialName("tabRenderer")
    val browseSongsTabRenderer: BrowseSongsTabRenderer? = null
)

@Serializable
data class BrowseSongsTabRenderer(
    @SerialName("content")
    val browseSongsContent: BrowseSongsContent? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsContent(
    @SerialName("sectionListRenderer")
    val browseSongsSectionListRenderer: BrowseSongsSectionListRenderer? = null
)

@Serializable
data class BrowseSongsSectionListRenderer(
    @SerialName("contents")
    val browseSongsContents: List<BrowseSongsContentX>? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsContentX(
    @SerialName("musicPlaylistShelfRenderer")
    val browseSongsMusicPlaylistShelfRenderer: BrowseSongsMusicPlaylistShelfRenderer? = null
)

@Serializable
data class BrowseSongsMusicPlaylistShelfRenderer(
    @SerialName("playlistId")
    val browseSongsPlaylistId: String? = null,
    @SerialName("header")
    val browseSongsHeader: BrowseSongsHeader? = null,
    @SerialName("contents")
    val browseSongsContents: List<BrowseSongsContentXX>? = null,
    @SerialName("collapsedItemCount")
    val browseSongsCollapsedItemCount: Int? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("contentsMultiSelectable")
    val browseSongsContentsMultiSelectable: Boolean? = null,
    @SerialName("targetId")
    val browseSongsTargetId: String? = null
)

@Serializable
data class BrowseSongsHeader(
    @SerialName("musicSideAlignedItemRenderer")
    val browseSongsMusicSideAlignedItemRenderer: BrowseSongsMusicSideAlignedItemRenderer? = null
)

@Serializable
data class BrowseSongsContentXX(
    @SerialName("musicResponsiveListItemRenderer")
    val browseSongsMusicResponsiveListItemRenderer: BrowseSongsMusicResponsiveListItemRenderer? = null,
    @SerialName("continuationItemRenderer")
    val browseSongsContinuationItemRenderer: BrowseSongsContinuationItemRenderer? = null
)

@Serializable
data class BrowseSongsMusicSideAlignedItemRenderer(
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsMusicResponsiveListItemRenderer(
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("thumbnail")
    val browseSongsThumbnail: BrowseSongsThumbnail? = null,
    @SerialName("overlay")
    val browseSongsOverlay: BrowseSongsOverlay? = null,
    @SerialName("flexColumns")
    val browseSongsFlexColumns: List<BrowseSongsFlexColumn>? = null,
    @SerialName("fixedColumns")
    val browseSongsFixedColumns: List<BrowseSongsFixedColumn>? = null,
    @SerialName("menu")
    val browseSongsMenu: BrowseSongsMenu? = null,
    @SerialName("playlistItemData")
    val browseSongsPlaylistItemData: BrowseSongsPlaylistItemData? = null,
    @SerialName("multiSelectCheckbox")
    val browseSongsMultiSelectCheckbox: BrowseSongsMultiSelectCheckbox? = null,
    @SerialName("badges")
    val browseSongsBadges: List<BrowseSongsBadge>? = null
)

@Serializable
data class BrowseSongsContinuationItemRenderer(
    @SerialName("trigger")
    val browseSongsTrigger: String? = null,
    @SerialName("continuationEndpoint")
    val browseSongsContinuationEndpoint: BrowseSongsContinuationEndpoint? = null
)

@Serializable
data class BrowseSongsThumbnail(
    @SerialName("musicThumbnailRenderer")
    val browseSongsMusicThumbnailRenderer: BrowseSongsMusicThumbnailRenderer? = null
)

@Serializable
data class BrowseSongsOverlay(
    @SerialName("musicItemThumbnailOverlayRenderer")
    val browseSongsMusicItemThumbnailOverlayRenderer: BrowseSongsMusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class BrowseSongsFlexColumn(
    @SerialName("musicResponsiveListItemFlexColumnRenderer")
    val browseSongsMusicResponsiveListItemFlexColumnRenderer: BrowseSongsMusicResponsiveListItemFlexColumnRenderer? = null
)

@Serializable
data class BrowseSongsFixedColumn(
    @SerialName("musicResponsiveListItemFixedColumnRenderer")
    val browseSongsMusicResponsiveListItemFixedColumnRenderer: BrowseSongsMusicResponsiveListItemFixedColumnRenderer? = null
)

@Serializable
data class BrowseSongsMenu(
    @SerialName("menuRenderer")
    val browseSongsMenuRenderer: BrowseSongsMenuRenderer? = null
)

@Serializable
data class BrowseSongsPlaylistItemData(
    @SerialName("playlistSetVideoId")
    val browseSongsPlaylistSetVideoId: String? = null,
    @SerialName("videoId")
    val browseSongsVideoId: String? = null,
    @SerialName("voteSortValue")
    val browseSongsVoteSortValue: Int? = null
)

@Serializable
data class BrowseSongsMultiSelectCheckbox(
    @SerialName("checkboxRenderer")
    val browseSongsCheckboxRenderer: BrowseSongsCheckboxRenderer? = null
)

@Serializable
data class BrowseSongsBadge(
    @SerialName("musicInlineBadgeRenderer")
    val browseSongsMusicInlineBadgeRenderer: BrowseSongsMusicInlineBadgeRenderer? = null
)

@Serializable
data class BrowseSongsMusicThumbnailRenderer(
    @SerialName("thumbnail")
    val browseSongsThumbnail: BrowseSongsThumbnailX? = null,
    @SerialName("thumbnailCrop")
    val browseSongsThumbnailCrop: String? = null,
    @SerialName("thumbnailScale")
    val browseSongsThumbnailScale: String? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsThumbnailX(
    @SerialName("thumbnails")
    val browseSongsThumbnails: List<BrowseSongsThumbnailXX>? = null
)

@Serializable
data class BrowseSongsThumbnailXX(
    @SerialName("url")
    val browseSongsUrl: String,
    @SerialName("width")
    val browseSongsWidth: Int,
    @SerialName("height")
    val browseSongsHeight: Int
)

@Serializable
data class BrowseSongsMusicItemThumbnailOverlayRenderer(
    @SerialName("background")
    val browseSongsBackground: BrowseSongsBackground? = null,
    @SerialName("content")
    val browseSongsContent: BrowseSongsContentXXX? = null,
    @SerialName("contentPosition")
    val browseSongsContentPosition: String? = null,
    @SerialName("displayStyle")
    val browseSongsDisplayStyle: String? = null
)

@Serializable
data class BrowseSongsBackground(
    @SerialName("verticalGradient")
    val browseSongsVerticalGradient: BrowseSongsVerticalGradient? = null
)

@Serializable
data class BrowseSongsContentXXX(
    @SerialName("musicPlayButtonRenderer")
    val browseSongsMusicPlayButtonRenderer: BrowseSongsMusicPlayButtonRenderer? = null
)

@Serializable
data class BrowseSongsVerticalGradient(
    @SerialName("gradientLayerColors")
    val browseSongsGradientLayerColors: List<String?>? = null
)

@Serializable
data class BrowseSongsMusicPlayButtonRenderer(
    @SerialName("playNavigationEndpoint")
    val browseSongsPlayNavigationEndpoint: BrowseSongsPlayNavigationEndpoint? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("playIcon")
    val browseSongsPlayIcon: BrowseSongsPlayIcon? = null,
    @SerialName("pauseIcon")
    val browseSongsPauseIcon: BrowseSongsPauseIcon? = null,
    @SerialName("iconColor")
    val browseSongsIconColor: Long? = null,
    @SerialName("backgroundColor")
    val browseSongsBackgroundColor: Int? = null,
    @SerialName("activeBackgroundColor")
    val browseSongsActiveBackgroundColor: Int? = null,
    @SerialName("loadingIndicatorColor")
    val browseSongsLoadingIndicatorColor: Int? = null,
    @SerialName("playingIcon")
    val browseSongsPlayingIcon: BrowseSongsPlayingIcon? = null,
    @SerialName("iconLoadingColor")
    val browseSongsIconLoadingColor: Int? = null,
    @SerialName("activeScaleFactor")
    val browseSongsActiveScaleFactor: Int? = null,
    @SerialName("buttonSize")
    val browseSongsButtonSize: String? = null,
    @SerialName("rippleTarget")
    val browseSongsRippleTarget: String? = null,
    @SerialName("accessibilityPlayData")
    val browseSongsAccessibilityPlayData: BrowseSongsAccessibilityPlayData? = null,
    @SerialName("accessibilityPauseData")
    val browseSongsAccessibilityPauseData: BrowseSongsAccessibilityPauseData? = null
)

@Serializable
data class BrowseSongsPlayNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browseSongsWatchEndpoint: BrowseSongsWatchEndpoint? = null
)

@Serializable
data class BrowseSongsPlayIcon(
    @SerialName("iconType")
    val browseSongsIconType: String? = null
)

@Serializable
data class BrowseSongsPauseIcon(
    @SerialName("iconType")
    val browseSongsIconType: String? = null
)

@Serializable
data class BrowseSongsPlayingIcon(
    @SerialName("iconType")
    val browseSongsIconType: String? = null
)

@Serializable
data class BrowseSongsAccessibilityPlayData(
    @SerialName("accessibilityData")
    val browseSongsAccessibilityData: BrowseSongsAccessibilityData? = null
)

@Serializable
data class BrowseSongsAccessibilityPauseData(
    @SerialName("accessibilityData")
    val browseSongsAccessibilityData: BrowseSongsAccessibilityData? = null
)

@Serializable
data class BrowseSongsWatchEndpoint(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null,
    @SerialName("playlistId")
    val browseSongsPlaylistId: String? = null,
    @SerialName("playerParams")
    val browseSongsPlayerParams: String? = null,
    @SerialName("playlistSetVideoId")
    val browseSongsPlaylistSetVideoId: String? = null,
    @SerialName("loggingContext")
    val browseSongsLoggingContext: BrowseSongsLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val browseSongsWatchEndpointMusicSupportedConfigs: BrowseSongsWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class BrowseSongsLoggingContext(
    @SerialName("vssLoggingContext")
    val browseSongsVssLoggingContext: BrowseSongsVssLoggingContext? = null
)

@Serializable
data class BrowseSongsWatchEndpointMusicSupportedConfigs(
    @SerialName("watchEndpointMusicConfig")
    val browseSongsWatchEndpointMusicConfig: BrowseSongsWatchEndpointMusicConfig? = null
)

@Serializable
data class BrowseSongsVssLoggingContext(
    @SerialName("serializedContextData")
    val browseSongsSerializedContextData: String? = null
)

@Serializable
data class BrowseSongsWatchEndpointMusicConfig(
    @SerialName("musicVideoType")
    val browseSongsMusicVideoType: String? = null
)

@Serializable
data class BrowseSongsAccessibilityData(
    @SerialName("label")
    val browseSongsLabel: String? = null
)

@Serializable
data class BrowseSongsMusicResponsiveListItemFlexColumnRenderer(
    @SerialName("text")
    val browseSongsText: BrowseSongsText? = null,
    @SerialName("displayPriority")
    val browseSongsDisplayPriority: String? = null
)

@Serializable
data class BrowseSongsText(
    @SerialName("runs")
    val browseSongsRuns: List<BrowseSongsRun>? = null,
    @SerialName("accessibility")
    val browseSongsAccessibility: BrowseSongsAccessibility? = null
)

@Serializable
data class BrowseSongsRun(
    @SerialName("text")
    val browseSongsText: String,
    @SerialName("navigationEndpoint")
    val browseSongsNavigationEndpoint: BrowseSongsNavigationEndpoint? = null
)

@Serializable
data class BrowseSongsAccessibility(
    @SerialName("accessibilityData")
    val browseSongsAccessibilityData: BrowseSongsAccessibilityData? = null
)

@Serializable
data class BrowseSongsNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browseSongsWatchEndpoint: BrowseSongsWatchEndpointX? = null,
    @SerialName("browseEndpoint")
    val browseSongsBrowseEndpoint: BrowseSongsBrowseEndpoint? = null
)

@Serializable
data class BrowseSongsWatchEndpointX(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null,
    @SerialName("playlistId")
    val browseSongsPlaylistId: String? = null,
    @SerialName("loggingContext")
    val browseSongsLoggingContext: BrowseSongsLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val browseSongsWatchEndpointMusicSupportedConfigs: BrowseSongsWatchEndpointMusicSupportedConfigs? = null,
    @SerialName("playerParams")
    val browseSongsPlayerParams: String? = null
)

@Serializable
data class BrowseSongsBrowseEndpoint(
    @SerialName("browseId")
    val browseSongsBrowseId: String? = null,
    @SerialName("browseEndpointContextSupportedConfigs")
    val browseSongsBrowseEndpointContextSupportedConfigs: BrowseSongsBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class BrowseSongsBrowseEndpointContextSupportedConfigs(
    @SerialName("browseEndpointContextMusicConfig")
    val browseSongsBrowseEndpointContextMusicConfig: BrowseSongsBrowseEndpointContextMusicConfig? = null
)

@Serializable
data class BrowseSongsBrowseEndpointContextMusicConfig(
    @SerialName("pageType")
    val browseSongsPageType: String? = null
)

@Serializable
data class BrowseSongsMusicResponsiveListItemFixedColumnRenderer(
    @SerialName("text")
    val browseSongsText: BrowseSongsTextX? = null,
    @SerialName("displayPriority")
    val browseSongsDisplayPriority: String? = null,
    @SerialName("size")
    val browseSongsSize: String? = null
)

@Serializable
data class BrowseSongsTextX(
    @SerialName("runs")
    val browseSongsRuns: List<BrowseSongsRunX>? = null,
    @SerialName("accessibility")
    val browseSongsAccessibility: BrowseSongsAccessibility? = null
)

@Serializable
data class BrowseSongsRunX(
    @SerialName("text")
    val browseSongsText: String? = null
)

@Serializable
data class BrowseSongsMenuRenderer(
    @SerialName("items")
    val browseSongsItems: List<BrowseSongsItem>? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("topLevelButtons")
    val browseSongsTopLevelButtons: List<BrowseSongsTopLevelButton>? = null,
    @SerialName("accessibility")
    val browseSongsAccessibility: BrowseSongsAccessibility? = null
)

@Serializable
data class BrowseSongsItem(
    @SerialName("menuNavigationItemRenderer")
    val browseSongsMenuNavigationItemRenderer: BrowseSongsMenuNavigationItemRenderer? = null,
    @SerialName("menuServiceItemRenderer")
    val browseSongsMenuServiceItemRenderer: BrowseSongsMenuServiceItemRenderer? = null,
    @SerialName("menuServiceItemDownloadRenderer")
    val browseSongsMenuServiceItemDownloadRenderer: BrowseSongsMenuServiceItemDownloadRenderer? = null
)

@Serializable
data class BrowseSongsTopLevelButton(
    @SerialName("likeButtonRenderer")
    val browseSongsLikeButtonRenderer: BrowseSongsLikeButtonRenderer? = null
)

@Serializable
data class BrowseSongsMenuNavigationItemRenderer(
    @SerialName("text")
    val browseSongsText: BrowseSongsTextXX? = null,
    @SerialName("icon")
    val browseSongsIcon: BrowseSongsIcon? = null,
    @SerialName("navigationEndpoint")
    val browseSongsNavigationEndpoint: BrowseSongsNavigationEndpointX? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsMenuServiceItemRenderer(
    @SerialName("text")
    val browseSongsText: BrowseSongsTextXX? = null,
    @SerialName("icon")
    val browseSongsIcon: BrowseSongsIcon? = null,
    @SerialName("serviceEndpoint")
    val browseSongsServiceEndpoint: BrowseSongsServiceEndpoint? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsMenuServiceItemDownloadRenderer(
    @SerialName("serviceEndpoint")
    val browseSongsServiceEndpoint: BrowseSongsServiceEndpointX? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("badgeIcon")
    val browseSongsBadgeIcon: BrowseSongsBadgeIcon? = null
)

@Serializable
data class BrowseSongsTextXX(
    @SerialName("runs")
    val browseSongsRuns: List<BrowseSongsRunX>? = null
)

@Serializable
data class BrowseSongsIcon(
    @SerialName("iconType")
    val browseSongsIconType: String? = null
)

@Serializable
data class BrowseSongsNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browseSongsWatchEndpoint: BrowseSongsWatchEndpointXX? = null,
    @SerialName("modalEndpoint")
    val browseSongsModalEndpoint: BrowseSongsModalEndpoint? = null,
    @SerialName("browseEndpoint")
    val browseSongsBrowseEndpoint: BrowseSongsBrowseEndpoint? = null,
    @SerialName("shareEntityEndpoint")
    val browseSongsShareEntityEndpoint: BrowseSongsShareEntityEndpoint? = null
)

@Serializable
data class BrowseSongsWatchEndpointXX(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null,
    @SerialName("playlistId")
    val browseSongsPlaylistId: String? = null,
    @SerialName("params")
    val browseSongsParams: String? = null,
    @SerialName("loggingContext")
    val browseSongsLoggingContext: BrowseSongsLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val browseSongsWatchEndpointMusicSupportedConfigs: BrowseSongsWatchEndpointMusicSupportedConfigs? = null,
    @SerialName("playerParams")
    val browseSongsPlayerParams: String? = null
)

@Serializable
data class BrowseSongsModalEndpoint(
    @SerialName("modal")
    val browseSongsModal: BrowseSongsModal? = null
)

@Serializable
data class BrowseSongsShareEntityEndpoint(
    @SerialName("serializedShareEntity")
    val browseSongsSerializedShareEntity: String? = null,
    @SerialName("sharePanelType")
    val browseSongsSharePanelType: String? = null
)

@Serializable
data class BrowseSongsModal(
    @SerialName("modalWithTitleAndButtonRenderer")
    val browseSongsModalWithTitleAndButtonRenderer: BrowseSongsModalWithTitleAndButtonRenderer? = null
)

@Serializable
data class BrowseSongsModalWithTitleAndButtonRenderer(
    @SerialName("title")
    val browseSongsTitle: BrowseSongsTitle? = null,
    @SerialName("content")
    val browseSongsContent: BrowseSongsContentXXXX? = null,
    @SerialName("button")
    val browseSongsButton: BrowseSongsButton? = null
)

@Serializable
data class BrowseSongsTitle(
    @SerialName("runs")
    val browseSongsRuns: List<BrowseSongsRunX>? = null
)

@Serializable
data class BrowseSongsContentXXXX(
    @SerialName("runs")
    val browseSongsRuns: List<BrowseSongsRunX>? = null
)

@Serializable
data class BrowseSongsButton(
    @SerialName("buttonRenderer")
    val browseSongsButtonRenderer: BrowseSongsButtonRenderer? = null
)

@Serializable
data class BrowseSongsButtonRenderer(
    @SerialName("style")
    val browseSongsStyle: String? = null,
    @SerialName("isDisabled")
    val browseSongsIsDisabled: Boolean? = null,
    @SerialName("text")
    val browseSongsText: BrowseSongsTextXX? = null,
    @SerialName("navigationEndpoint")
    val browseSongsNavigationEndpoint: BrowseSongsNavigationEndpointXX? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsNavigationEndpointXX(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("signInEndpoint")
    val browseSongsSignInEndpoint: BrowseSongsSignInEndpoint? = null
)

@Serializable
data class BrowseSongsSignInEndpoint(
    @SerialName("hack")
    val browseSongsHack: Boolean? = null
)

@Serializable
data class BrowseSongsServiceEndpoint(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("queueAddEndpoint")
    val browseSongsQueueAddEndpoint: BrowseSongsQueueAddEndpoint? = null
)

@Serializable
data class BrowseSongsQueueAddEndpoint(
    @SerialName("queueTarget")
    val browseSongsQueueTarget: BrowseSongsQueueTarget? = null,
    @SerialName("queueInsertPosition")
    val browseSongsQueueInsertPosition: String? = null,
    @SerialName("commands")
    val browseSongsCommands: List<BrowseSongsCommand>? = null
)

@Serializable
data class BrowseSongsQueueTarget(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null,
    @SerialName("onEmptyQueue")
    val browseSongsOnEmptyQueue: BrowseSongsOnEmptyQueue? = null
)

@Serializable
data class BrowseSongsCommand(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("addToToastAction")
    val browseSongsAddToToastAction: BrowseSongsAddToToastAction? = null
)

@Serializable
data class BrowseSongsOnEmptyQueue(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browseSongsWatchEndpoint: BrowseSongsWatchEndpointXXX? = null
)

@Serializable
data class BrowseSongsWatchEndpointXXX(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null
)

@Serializable
data class BrowseSongsAddToToastAction(
    @SerialName("item")
    val browseSongsItem: BrowseSongsItemX? = null
)

@Serializable
data class BrowseSongsItemX(
    @SerialName("notificationTextRenderer")
    val browseSongsNotificationTextRenderer: BrowseSongsNotificationTextRenderer? = null
)

@Serializable
data class BrowseSongsNotificationTextRenderer(
    @SerialName("successResponseText")
    val browseSongsSuccessResponseText: BrowseSongsSuccessResponseText? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsSuccessResponseText(
    @SerialName("runs")
    val browseSongsRuns: List<BrowseSongsRunX>? = null
)

@Serializable
data class BrowseSongsServiceEndpointX(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("offlineVideoEndpoint")
    val browseSongsOfflineVideoEndpoint: BrowseSongsOfflineVideoEndpoint? = null
)

@Serializable
data class BrowseSongsBadgeIcon(
    @SerialName("iconType")
    val browseSongsIconType: String? = null
)

@Serializable
data class BrowseSongsOfflineVideoEndpoint(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null,
    @SerialName("onAddCommand")
    val browseSongsOnAddCommand: BrowseSongsOnAddCommand? = null
)

@Serializable
data class BrowseSongsOnAddCommand(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("getDownloadActionCommand")
    val browseSongsGetDownloadActionCommand: BrowseSongsGetDownloadActionCommand? = null
)

@Serializable
data class BrowseSongsGetDownloadActionCommand(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null,
    @SerialName("params")
    val browseSongsParams: String? = null
)

@Serializable
data class BrowseSongsLikeButtonRenderer(
    @SerialName("target")
    val browseSongsTarget: BrowseSongsTarget? = null,
    @SerialName("likeStatus")
    val browseSongsLikeStatus: String? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("likesAllowed")
    val browseSongsLikesAllowed: Boolean? = null,
    @SerialName("dislikeNavigationEndpoint")
    val browseSongsDislikeNavigationEndpoint: BrowseSongsDislikeNavigationEndpoint? = null,
    @SerialName("likeCommand")
    val browseSongsLikeCommand: BrowseSongsLikeCommand? = null
)

@Serializable
data class BrowseSongsTarget(
    @SerialName("videoId")
    val browseSongsVideoId: String? = null
)

@Serializable
data class BrowseSongsDislikeNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val browseSongsModalEndpoint: BrowseSongsModalEndpoint? = null
)

@Serializable
data class BrowseSongsLikeCommand(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val browseSongsModalEndpoint: BrowseSongsModalEndpoint? = null
)

@Serializable
data class BrowseSongsCheckboxRenderer(
    @SerialName("onSelectionChangeCommand")
    val browseSongsOnSelectionChangeCommand: BrowseSongsOnSelectionChangeCommand? = null,
    @SerialName("checkedState")
    val browseSongsCheckedState: String? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsOnSelectionChangeCommand(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("updateMultiSelectStateCommand")
    val browseSongsUpdateMultiSelectStateCommand: BrowseSongsUpdateMultiSelectStateCommand? = null
)

@Serializable
data class BrowseSongsUpdateMultiSelectStateCommand(
    @SerialName("multiSelectParams")
    val browseSongsMultiSelectParams: String? = null,
    @SerialName("multiSelectItem")
    val browseSongsMultiSelectItem: String? = null
)

@Serializable
data class BrowseSongsMusicInlineBadgeRenderer(
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null,
    @SerialName("icon")
    val browseSongsIcon: BrowseSongsIcon? = null,
    @SerialName("accessibilityData")
    val browseSongsAccessibilityData: BrowseSongsAccessibilityDataXXXXX? = null
)

@Serializable
data class BrowseSongsAccessibilityDataXXXXX(
    @SerialName("accessibilityData")
    val browseSongsAccessibilityData: BrowseSongsAccessibilityData? = null
)

@Serializable
data class BrowseSongsContinuationEndpoint(
    @SerialName("clickTrackingParams")
    val browseSongsClickTrackingParams: String? = null,
    @SerialName("continuationCommand")
    val browseSongsContinuationCommand: BrowseSongsContinuationCommand? = null
)

@Serializable
data class BrowseSongsContinuationCommand(
    @SerialName("token")
    val browseSongsToken: String? = null,
    @SerialName("request")
    val browseSongsRequest: String? = null
)

@Serializable
data class BrowseSongsMusicHeaderRenderer(
    @SerialName("title")
    val browseSongsTitle: BrowseSongsTitle? = null,
    @SerialName("trackingParams")
    val browseSongsTrackingParams: String? = null
)

@Serializable
data class BrowseSongsMicroformatDataRenderer(
    @SerialName("urlCanonical")
    val browseSongsUrlCanonical: String? = null,
    @SerialName("title")
    val browseSongsTitle: String? = null,
    @SerialName("description")
    val browseSongsDescription: String? = null,
    @SerialName("thumbnail")
    val browseSongsThumbnail: BrowseSongsThumbnailX? = null,
    @SerialName("siteName")
    val browseSongsSiteName: String? = null,
    @SerialName("appName")
    val browseSongsAppName: String? = null,
    @SerialName("androidPackage")
    val browseSongsAndroidPackage: String? = null,
    @SerialName("iosAppStoreId")
    val browseSongsIosAppStoreId: String? = null,
    @SerialName("ogType")
    val browseSongsOgType: String? = null,
    @SerialName("urlApplinksWeb")
    val browseSongsUrlApplinksWeb: String? = null,
    @SerialName("urlApplinksIos")
    val browseSongsUrlApplinksIos: String? = null,
    @SerialName("urlApplinksAndroid")
    val browseSongsUrlApplinksAndroid: String? = null,
    @SerialName("urlTwitterIos")
    val browseSongsUrlTwitterIos: String? = null,
    @SerialName("urlTwitterAndroid")
    val browseSongsUrlTwitterAndroid: String? = null,
    @SerialName("twitterCardType")
    val browseSongsTwitterCardType: String? = null,
    @SerialName("twitterSiteHandle")
    val browseSongsTwitterSiteHandle: String? = null
)
