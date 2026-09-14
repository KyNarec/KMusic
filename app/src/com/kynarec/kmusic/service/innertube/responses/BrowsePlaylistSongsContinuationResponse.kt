package com.kynarec.kmusic.service.innertube.responses
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrowsePlaylistSongsContinuationResponse(
    @SerialName("responseContext")
    val browsePlaylistSongsContinuationResponseContext: BrowsePlaylistSongsContinuationResponseContext? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null,
    @SerialName("onResponseReceivedActions")
    val browsePlaylistSongsContinuationOnResponseReceivedActions: List<BrowsePlaylistSongsContinuationOnResponseReceivedAction>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationResponseContext(
    @SerialName("visitorData")
    val browsePlaylistSongsContinuationVisitorData: String? = null,
    @SerialName("serviceTrackingParams")
    val browsePlaylistSongsContinuationServiceTrackingParams: List<BrowsePlaylistSongsContinuationServiceTrackingParam?>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationOnResponseReceivedAction(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("appendContinuationItemsAction")
    val browsePlaylistSongsContinuationAppendContinuationItemsAction: BrowsePlaylistSongsContinuationAppendContinuationItemsAction? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationServiceTrackingParam(
    @SerialName("service")
    val browsePlaylistSongsContinuationService: String? = null,
    @SerialName("params")
    val browsePlaylistSongsContinuationParams: List<BrowsePlaylistSongsContinuationParam?>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationParam(
    @SerialName("key")
    val browsePlaylistSongsContinuationKey: String? = null,
    @SerialName("value")
    val browsePlaylistSongsContinuationValue: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationAppendContinuationItemsAction(
    @SerialName("continuationItems")
    val browsePlaylistSongsContinuationContinuationItems: List<BrowsePlaylistSongsContinuationContinuationItem>? = null,
    @SerialName("targetId")
    val browsePlaylistSongsContinuationTargetId: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationContinuationItem(
    @SerialName("musicResponsiveListItemRenderer")
    val browsePlaylistSongsContinuationMusicResponsiveListItemRenderer: BrowsePlaylistSongsContinuationMusicResponsiveListItemRenderer? = null,
    @SerialName("continuationItemRenderer")
    val browsePlaylistSongsContinuationContinuationItemRenderer: BrowsePlaylistSongsContinuationContinuationItemRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMusicResponsiveListItemRenderer(
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null,
    @SerialName("thumbnail")
    val browsePlaylistSongsContinuationThumbnail: BrowsePlaylistSongsContinuationThumbnail? = null,
    @SerialName("overlay")
    val browsePlaylistSongsContinuationOverlay: BrowsePlaylistSongsContinuationOverlay? = null,
    @SerialName("flexColumns")
    val browsePlaylistSongsContinuationFlexColumns: List<BrowsePlaylistSongsContinuationFlexColumn>? = null,
    @SerialName("fixedColumns")
    val browsePlaylistSongsContinuationFixedColumns: List<BrowsePlaylistSongsContinuationFixedColumn>? = null,
    @SerialName("menu")
    val browsePlaylistSongsContinuationMenu: BrowsePlaylistSongsContinuationMenu? = null,
    @SerialName("playlistItemData")
    val browsePlaylistSongsContinuationPlaylistItemData: BrowsePlaylistSongsContinuationPlaylistItemData? = null,
    @SerialName("multiSelectCheckbox")
    val browsePlaylistSongsContinuationMultiSelectCheckbox: BrowsePlaylistSongsContinuationMultiSelectCheckbox? = null,
    @SerialName("musicItemRendererDisplayPolicy")
    val browsePlaylistSongsContinuationMusicItemRendererDisplayPolicy: String? = null,
    @SerialName("badges")
    val browsePlaylistSongsContinuationBadges: List<BrowsePlaylistSongsContinuationBadge>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationContinuationItemRenderer(
    @SerialName("trigger")
    val browsePlaylistSongsContinuationTrigger: String? = null,
    @SerialName("continuationEndpoint")
    val browsePlaylistSongsContinuationContinuationEndpoint: BrowsePlaylistSongsContinuationContinuationEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationThumbnail(
    @SerialName("musicThumbnailRenderer")
    val browsePlaylistSongsContinuationMusicThumbnailRenderer: BrowsePlaylistSongsContinuationMusicThumbnailRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationOverlay(
    @SerialName("musicItemThumbnailOverlayRenderer")
    val browsePlaylistSongsContinuationMusicItemThumbnailOverlayRenderer: BrowsePlaylistSongsContinuationMusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationFlexColumn(
    @SerialName("musicResponsiveListItemFlexColumnRenderer")
    val browsePlaylistSongsContinuationMusicResponsiveListItemFlexColumnRenderer: BrowsePlaylistSongsContinuationMusicResponsiveListItemFlexColumnRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationFixedColumn(
    @SerialName("musicResponsiveListItemFixedColumnRenderer")
    val browsePlaylistSongsContinuationMusicResponsiveListItemFixedColumnRenderer: BrowsePlaylistSongsContinuationMusicResponsiveListItemFixedColumnRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMenu(
    @SerialName("menuRenderer")
    val browsePlaylistSongsContinuationMenuRenderer: BrowsePlaylistSongsContinuationMenuRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationPlaylistItemData(
    @SerialName("playlistSetVideoId")
    val browsePlaylistSongsContinuationPlaylistSetVideoId: String? = null,
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null,
    @SerialName("voteSortValue")
    val browsePlaylistSongsContinuationVoteSortValue: Int? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMultiSelectCheckbox(
    @SerialName("checkboxRenderer")
    val browsePlaylistSongsContinuationCheckboxRenderer: BrowsePlaylistSongsContinuationCheckboxRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationBadge(
    @SerialName("musicInlineBadgeRenderer")
    val browsePlaylistSongsContinuationMusicInlineBadgeRenderer: BrowsePlaylistSongsContinuationMusicInlineBadgeRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMusicThumbnailRenderer(
    @SerialName("thumbnail")
    val browsePlaylistSongsContinuationThumbnail: BrowsePlaylistSongsContinuationThumbnailX? = null,
    @SerialName("thumbnailCrop")
    val browsePlaylistSongsContinuationThumbnailCrop: String? = null,
    @SerialName("thumbnailScale")
    val browsePlaylistSongsContinuationThumbnailScale: String? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationThumbnailX(
    @SerialName("thumbnails")
    val browsePlaylistSongsContinuationThumbnails: List<BrowsePlaylistSongsContinuationThumbnailXX>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationThumbnailXX(
    @SerialName("url")
    val browsePlaylistSongsContinuationUrl: String,
    @SerialName("width")
    val browsePlaylistSongsContinuationWidth: Int,
    @SerialName("height")
    val browsePlaylistSongsContinuationHeight: Int
)

@Serializable
data class BrowsePlaylistSongsContinuationMusicItemThumbnailOverlayRenderer(
    @SerialName("background")
    val browsePlaylistSongsContinuationBackground: BrowsePlaylistSongsContinuationBackground? = null,
    @SerialName("content")
    val browsePlaylistSongsContinuationContent: BrowsePlaylistSongsContinuationContent? = null,
    @SerialName("contentPosition")
    val browsePlaylistSongsContinuationContentPosition: String? = null,
    @SerialName("displayStyle")
    val browsePlaylistSongsContinuationDisplayStyle: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationBackground(
    @SerialName("verticalGradient")
    val browsePlaylistSongsContinuationVerticalGradient: BrowsePlaylistSongsContinuationVerticalGradient? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationContent(
    @SerialName("musicPlayButtonRenderer")
    val browsePlaylistSongsContinuationMusicPlayButtonRenderer: BrowsePlaylistSongsContinuationMusicPlayButtonRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationVerticalGradient(
    @SerialName("gradientLayerColors")
    val browsePlaylistSongsContinuationGradientLayerColors: List<String?>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMusicPlayButtonRenderer(
    @SerialName("playNavigationEndpoint")
    val browsePlaylistSongsContinuationPlayNavigationEndpoint: BrowsePlaylistSongsContinuationPlayNavigationEndpoint? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null,
    @SerialName("playIcon")
    val browsePlaylistSongsContinuationPlayIcon: BrowsePlaylistSongsContinuationPlayIcon? = null,
    @SerialName("pauseIcon")
    val browsePlaylistSongsContinuationPauseIcon: BrowsePlaylistSongsContinuationPauseIcon? = null,
    @SerialName("iconColor")
    val browsePlaylistSongsContinuationIconColor: Long? = null,
    @SerialName("backgroundColor")
    val browsePlaylistSongsContinuationBackgroundColor: Int? = null,
    @SerialName("activeBackgroundColor")
    val browsePlaylistSongsContinuationActiveBackgroundColor: Int? = null,
    @SerialName("loadingIndicatorColor")
    val browsePlaylistSongsContinuationLoadingIndicatorColor: Int? = null,
    @SerialName("playingIcon")
    val browsePlaylistSongsContinuationPlayingIcon: BrowsePlaylistSongsContinuationPlayingIcon? = null,
    @SerialName("iconLoadingColor")
    val browsePlaylistSongsContinuationIconLoadingColor: Int? = null,
    @SerialName("activeScaleFactor")
    val browsePlaylistSongsContinuationActiveScaleFactor: Int? = null,
    @SerialName("buttonSize")
    val browsePlaylistSongsContinuationButtonSize: String? = null,
    @SerialName("rippleTarget")
    val browsePlaylistSongsContinuationRippleTarget: String? = null,
    @SerialName("accessibilityPlayData")
    val browsePlaylistSongsContinuationAccessibilityPlayData: BrowsePlaylistSongsContinuationAccessibilityPlayData? = null,
    @SerialName("accessibilityPauseData")
    val browsePlaylistSongsContinuationAccessibilityPauseData: BrowsePlaylistSongsContinuationAccessibilityPauseData? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationPlayNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browsePlaylistSongsContinuationWatchEndpoint: BrowsePlaylistSongsContinuationWatchEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationPlayIcon(
    @SerialName("iconType")
    val browsePlaylistSongsContinuationIconType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationPauseIcon(
    @SerialName("iconType")
    val browsePlaylistSongsContinuationIconType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationPlayingIcon(
    @SerialName("iconType")
    val browsePlaylistSongsContinuationIconType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationAccessibilityPlayData(
    @SerialName("accessibilityData")
    val browsePlaylistSongsContinuationAccessibilityData: BrowsePlaylistSongsContinuationAccessibilityData? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationAccessibilityPauseData(
    @SerialName("accessibilityData")
    val browsePlaylistSongsContinuationAccessibilityData: BrowsePlaylistSongsContinuationAccessibilityData? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationWatchEndpoint(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null,
    @SerialName("playlistId")
    val browsePlaylistSongsContinuationPlaylistId: String? = null,
    @SerialName("playerParams")
    val browsePlaylistSongsContinuationPlayerParams: String? = null,
    @SerialName("playlistSetVideoId")
    val browsePlaylistSongsContinuationPlaylistSetVideoId: String? = null,
    @SerialName("loggingContext")
    val browsePlaylistSongsContinuationLoggingContext: BrowsePlaylistSongsContinuationLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val browsePlaylistSongsContinuationWatchEndpointMusicSupportedConfigs: BrowsePlaylistSongsContinuationWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationLoggingContext(
    @SerialName("vssLoggingContext")
    val browsePlaylistSongsContinuationVssLoggingContext: BrowsePlaylistSongsContinuationVssLoggingContext? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationWatchEndpointMusicSupportedConfigs(
    @SerialName("watchEndpointMusicConfig")
    val browsePlaylistSongsContinuationWatchEndpointMusicConfig: BrowsePlaylistSongsContinuationWatchEndpointMusicConfig? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationVssLoggingContext(
    @SerialName("serializedContextData")
    val browsePlaylistSongsContinuationSerializedContextData: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationWatchEndpointMusicConfig(
    @SerialName("musicVideoType")
    val browsePlaylistSongsContinuationMusicVideoType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationAccessibilityData(
    @SerialName("label")
    val browsePlaylistSongsContinuationLabel: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMusicResponsiveListItemFlexColumnRenderer(
    @SerialName("text")
    val browsePlaylistSongsContinuationText: BrowsePlaylistSongsContinuationText? = null,
    @SerialName("displayPriority")
    val browsePlaylistSongsContinuationDisplayPriority: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationText(
    @SerialName("runs")
    val browsePlaylistSongsContinuationRuns: List<BrowsePlaylistSongsContinuationRun>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationRun(
    @SerialName("text")
    val browsePlaylistSongsContinuationText: String? = null,
    @SerialName("navigationEndpoint")
    val browsePlaylistSongsContinuationNavigationEndpoint: BrowsePlaylistSongsContinuationNavigationEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browsePlaylistSongsContinuationWatchEndpoint: BrowsePlaylistSongsContinuationWatchEndpointX? = null,
    @SerialName("browseEndpoint")
    val browsePlaylistSongsContinuationBrowseEndpoint: BrowsePlaylistSongsContinuationBrowseEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationWatchEndpointX(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null,
    @SerialName("playlistId")
    val browsePlaylistSongsContinuationPlaylistId: String? = null,
    @SerialName("playerParams")
    val browsePlaylistSongsContinuationPlayerParams: String? = null,
    @SerialName("loggingContext")
    val browsePlaylistSongsContinuationLoggingContext: BrowsePlaylistSongsContinuationLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val browsePlaylistSongsContinuationWatchEndpointMusicSupportedConfigs: BrowsePlaylistSongsContinuationWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationBrowseEndpoint(
    @SerialName("browseId")
    val browsePlaylistSongsContinuationBrowseId: String?,
    @SerialName("browseEndpointContextSupportedConfigs")
    val browsePlaylistSongsContinuationBrowseEndpointContextSupportedConfigs: BrowsePlaylistSongsContinuationBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationBrowseEndpointContextSupportedConfigs(
    @SerialName("browseEndpointContextMusicConfig")
    val browsePlaylistSongsContinuationBrowseEndpointContextMusicConfig: BrowsePlaylistSongsContinuationBrowseEndpointContextMusicConfig? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationBrowseEndpointContextMusicConfig(
    @SerialName("pageType")
    val browsePlaylistSongsContinuationPageType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMusicResponsiveListItemFixedColumnRenderer(
    @SerialName("text")
    val browsePlaylistSongsContinuationText: BrowsePlaylistSongsContinuationTextX? = null,
    @SerialName("displayPriority")
    val browsePlaylistSongsContinuationDisplayPriority: String? = null,
    @SerialName("size")
    val browsePlaylistSongsContinuationSize: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationTextX(
    @SerialName("runs")
    val browsePlaylistSongsContinuationRuns: List<BrowsePlaylistSongsContinuationRunX>? = null,
    @SerialName("accessibility")
    val browsePlaylistSongsContinuationAccessibility: BrowsePlaylistSongsContinuationAccessibility? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationRunX(
    @SerialName("text")
    val browsePlaylistSongsContinuationText: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationAccessibility(
    @SerialName("accessibilityData")
    val browsePlaylistSongsContinuationAccessibilityData: BrowsePlaylistSongsContinuationAccessibilityData? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMenuRenderer(
    @SerialName("items")
    val browsePlaylistSongsContinuationItems: List<BrowsePlaylistSongsContinuationItem>? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null,
    @SerialName("topLevelButtons")
    val browsePlaylistSongsContinuationTopLevelButtons: List<BrowsePlaylistSongsContinuationTopLevelButton>? = null,
    @SerialName("accessibility")
    val browsePlaylistSongsContinuationAccessibility: BrowsePlaylistSongsContinuationAccessibility? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationItem(
    @SerialName("menuNavigationItemRenderer")
    val browsePlaylistSongsContinuationMenuNavigationItemRenderer: BrowsePlaylistSongsContinuationMenuNavigationItemRenderer? = null,
    @SerialName("menuServiceItemRenderer")
    val browsePlaylistSongsContinuationMenuServiceItemRenderer: BrowsePlaylistSongsContinuationMenuServiceItemRenderer? = null,
    @SerialName("menuServiceItemDownloadRenderer")
    val browsePlaylistSongsContinuationMenuServiceItemDownloadRenderer: BrowsePlaylistSongsContinuationMenuServiceItemDownloadRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationTopLevelButton(
    @SerialName("likeButtonRenderer")
    val browsePlaylistSongsContinuationLikeButtonRenderer: BrowsePlaylistSongsContinuationLikeButtonRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMenuNavigationItemRenderer(
    @SerialName("text")
    val browsePlaylistSongsContinuationText: BrowsePlaylistSongsContinuationTextXX? = null,
    @SerialName("icon")
    val browsePlaylistSongsContinuationIcon: BrowsePlaylistSongsContinuationIcon? = null,
    @SerialName("navigationEndpoint")
    val browsePlaylistSongsContinuationNavigationEndpoint: BrowsePlaylistSongsContinuationNavigationEndpointX? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMenuServiceItemRenderer(
    @SerialName("text")
    val browsePlaylistSongsContinuationText: BrowsePlaylistSongsContinuationTextXX? = null,
    @SerialName("icon")
    val browsePlaylistSongsContinuationIcon: BrowsePlaylistSongsContinuationIcon? = null,
    @SerialName("serviceEndpoint")
    val browsePlaylistSongsContinuationServiceEndpoint: BrowsePlaylistSongsContinuationServiceEndpoint? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMenuServiceItemDownloadRenderer(
    @SerialName("serviceEndpoint")
    val browsePlaylistSongsContinuationServiceEndpoint: BrowsePlaylistSongsContinuationServiceEndpointX? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null,
    @SerialName("badgeIcon")
    val browsePlaylistSongsContinuationBadgeIcon: BrowsePlaylistSongsContinuationBadgeIcon? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationTextXX(
    @SerialName("runs")
    val browsePlaylistSongsContinuationRuns: List<BrowsePlaylistSongsContinuationRunX>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationIcon(
    @SerialName("iconType")
    val browsePlaylistSongsContinuationIconType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browsePlaylistSongsContinuationWatchEndpoint: BrowsePlaylistSongsContinuationWatchEndpointXX? = null,
    @SerialName("modalEndpoint")
    val browsePlaylistSongsContinuationModalEndpoint: BrowsePlaylistSongsContinuationModalEndpoint? = null,
    @SerialName("browseEndpoint")
    val browsePlaylistSongsContinuationBrowseEndpoint: BrowsePlaylistSongsContinuationBrowseEndpoint? = null,
    @SerialName("shareEntityEndpoint")
    val browsePlaylistSongsContinuationShareEntityEndpoint: BrowsePlaylistSongsContinuationShareEntityEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationWatchEndpointXX(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null,
    @SerialName("playlistId")
    val browsePlaylistSongsContinuationPlaylistId: String? = null,
    @SerialName("params")
    val browsePlaylistSongsContinuationParams: String? = null,
    @SerialName("loggingContext")
    val browsePlaylistSongsContinuationLoggingContext: BrowsePlaylistSongsContinuationLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val browsePlaylistSongsContinuationWatchEndpointMusicSupportedConfigs: BrowsePlaylistSongsContinuationWatchEndpointMusicSupportedConfigs? = null,
    @SerialName("playerParams")
    val browsePlaylistSongsContinuationPlayerParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationModalEndpoint(
    @SerialName("modal")
    val browsePlaylistSongsContinuationModal: BrowsePlaylistSongsContinuationModal? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationShareEntityEndpoint(
    @SerialName("serializedShareEntity")
    val browsePlaylistSongsContinuationSerializedShareEntity: String? = null,
    @SerialName("sharePanelType")
    val browsePlaylistSongsContinuationSharePanelType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationModal(
    @SerialName("modalWithTitleAndButtonRenderer")
    val browsePlaylistSongsContinuationModalWithTitleAndButtonRenderer: BrowsePlaylistSongsContinuationModalWithTitleAndButtonRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationModalWithTitleAndButtonRenderer(
    @SerialName("title")
    val browsePlaylistSongsContinuationTitle: BrowsePlaylistSongsContinuationTitle? = null,
    @SerialName("content")
    val browsePlaylistSongsContinuationContent: BrowsePlaylistSongsContinuationContentX? = null,
    @SerialName("button")
    val browsePlaylistSongsContinuationButton: BrowsePlaylistSongsContinuationButton? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationTitle(
    @SerialName("runs")
    val browsePlaylistSongsContinuationRuns: List<BrowsePlaylistSongsContinuationRunX>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationContentX(
    @SerialName("runs")
    val browsePlaylistSongsContinuationRuns: List<BrowsePlaylistSongsContinuationRunX>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationButton(
    @SerialName("buttonRenderer")
    val browsePlaylistSongsContinuationButtonRenderer: BrowsePlaylistSongsContinuationButtonRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationButtonRenderer(
    @SerialName("style")
    val browsePlaylistSongsContinuationStyle: String? = null,
    @SerialName("isDisabled")
    val browsePlaylistSongsContinuationIsDisabled: Boolean? = null,
    @SerialName("text")
    val browsePlaylistSongsContinuationText: BrowsePlaylistSongsContinuationTextXX? = null,
    @SerialName("navigationEndpoint")
    val browsePlaylistSongsContinuationNavigationEndpoint: BrowsePlaylistSongsContinuationNavigationEndpointXX? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationNavigationEndpointXX(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("signInEndpoint")
    val browsePlaylistSongsContinuationSignInEndpoint: BrowsePlaylistSongsContinuationSignInEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationSignInEndpoint(
    @SerialName("hack")
    val browsePlaylistSongsContinuationHack: Boolean? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationServiceEndpoint(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("queueAddEndpoint")
    val browsePlaylistSongsContinuationQueueAddEndpoint: BrowsePlaylistSongsContinuationQueueAddEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationQueueAddEndpoint(
    @SerialName("queueTarget")
    val browsePlaylistSongsContinuationQueueTarget: BrowsePlaylistSongsContinuationQueueTarget? = null,
    @SerialName("queueInsertPosition")
    val browsePlaylistSongsContinuationQueueInsertPosition: String? = null,
    @SerialName("commands")
    val browsePlaylistSongsContinuationCommands: List<BrowsePlaylistSongsContinuationCommand>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationQueueTarget(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null,
    @SerialName("onEmptyQueue")
    val browsePlaylistSongsContinuationOnEmptyQueue: BrowsePlaylistSongsContinuationOnEmptyQueue? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationCommand(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("addToToastAction")
    val browsePlaylistSongsContinuationAddToToastAction: BrowsePlaylistSongsContinuationAddToToastAction? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationOnEmptyQueue(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browsePlaylistSongsContinuationWatchEndpoint: BrowsePlaylistSongsContinuationWatchEndpointXXX? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationWatchEndpointXXX(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationAddToToastAction(
    @SerialName("item")
    val browsePlaylistSongsContinuationItem: BrowsePlaylistSongsContinuationItemX? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationItemX(
    @SerialName("notificationTextRenderer")
    val browsePlaylistSongsContinuationNotificationTextRenderer: BrowsePlaylistSongsContinuationNotificationTextRenderer? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationNotificationTextRenderer(
    @SerialName("successResponseText")
    val browsePlaylistSongsContinuationSuccessResponseText: BrowsePlaylistSongsContinuationSuccessResponseText? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationSuccessResponseText(
    @SerialName("runs")
    val browsePlaylistSongsContinuationRuns: List<BrowsePlaylistSongsContinuationRunX>? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationServiceEndpointX(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("offlineVideoEndpoint")
    val browsePlaylistSongsContinuationOfflineVideoEndpoint: BrowsePlaylistSongsContinuationOfflineVideoEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationBadgeIcon(
    @SerialName("iconType")
    val browsePlaylistSongsContinuationIconType: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationOfflineVideoEndpoint(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null,
    @SerialName("onAddCommand")
    val browsePlaylistSongsContinuationOnAddCommand: BrowsePlaylistSongsContinuationOnAddCommand? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationOnAddCommand(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("getDownloadActionCommand")
    val browsePlaylistSongsContinuationGetDownloadActionCommand: BrowsePlaylistSongsContinuationGetDownloadActionCommand? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationGetDownloadActionCommand(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null,
    @SerialName("params")
    val browsePlaylistSongsContinuationParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationLikeButtonRenderer(
    @SerialName("target")
    val browsePlaylistSongsContinuationTarget: BrowsePlaylistSongsContinuationTarget? = null,
    @SerialName("likeStatus")
    val browsePlaylistSongsContinuationLikeStatus: String? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null,
    @SerialName("likesAllowed")
    val browsePlaylistSongsContinuationLikesAllowed: Boolean? = null,
    @SerialName("dislikeNavigationEndpoint")
    val browsePlaylistSongsContinuationDislikeNavigationEndpoint: BrowsePlaylistSongsContinuationDislikeNavigationEndpoint? = null,
    @SerialName("likeCommand")
    val browsePlaylistSongsContinuationLikeCommand: BrowsePlaylistSongsContinuationLikeCommand? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationTarget(
    @SerialName("videoId")
    val browsePlaylistSongsContinuationVideoId: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationDislikeNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val browsePlaylistSongsContinuationModalEndpoint: BrowsePlaylistSongsContinuationModalEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationLikeCommand(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val browsePlaylistSongsContinuationModalEndpoint: BrowsePlaylistSongsContinuationModalEndpoint? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationCheckboxRenderer(
    @SerialName("onSelectionChangeCommand")
    val browsePlaylistSongsContinuationOnSelectionChangeCommand: BrowsePlaylistSongsContinuationOnSelectionChangeCommand? = null,
    @SerialName("checkedState")
    val browsePlaylistSongsContinuationCheckedState: String? = null,
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationOnSelectionChangeCommand(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("updateMultiSelectStateCommand")
    val browsePlaylistSongsContinuationUpdateMultiSelectStateCommand: BrowsePlaylistSongsContinuationUpdateMultiSelectStateCommand? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationUpdateMultiSelectStateCommand(
    @SerialName("multiSelectParams")
    val browsePlaylistSongsContinuationMultiSelectParams: String? = null,
    @SerialName("multiSelectItem")
    val browsePlaylistSongsContinuationMultiSelectItem: String? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationMusicInlineBadgeRenderer(
    @SerialName("trackingParams")
    val browsePlaylistSongsContinuationTrackingParams: String? = null,
    @SerialName("icon")
    val browsePlaylistSongsContinuationIcon: BrowsePlaylistSongsContinuationIcon? = null,
    @SerialName("accessibilityData")
    val browsePlaylistSongsContinuationAccessibilityData: BrowsePlaylistSongsContinuationAccessibilityDataXXXX? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationAccessibilityDataXXXX(
    @SerialName("accessibilityData")
    val browsePlaylistSongsContinuationAccessibilityData: BrowsePlaylistSongsContinuationAccessibilityData? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationContinuationEndpoint(
    @SerialName("clickTrackingParams")
    val browsePlaylistSongsContinuationClickTrackingParams: String? = null,
    @SerialName("continuationCommand")
    val browsePlaylistSongsContinuationContinuationCommand: BrowsePlaylistSongsContinuationContinuationCommand? = null
)

@Serializable
data class BrowsePlaylistSongsContinuationContinuationCommand(
    @SerialName("token")
    val browsePlaylistSongsContinuationToken: String? = null,
    @SerialName("request")
    val browsePlaylistSongsContinuationRequest: String? = null
)