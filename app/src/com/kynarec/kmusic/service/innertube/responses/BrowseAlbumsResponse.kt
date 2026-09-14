package com.kynarec.kmusic.service.innertube.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BrowseAlbumsResponse(
    @SerialName("responseContext")
    val browseAlbumsResponseContext: BrowseAlbumsResponseContext? = null,
    @SerialName("contents")
    val browseAlbumsContents: BrowseAlbumsContents? = null,
    @SerialName("header")
    val browseAlbumsHeader: BrowseAlbumsHeaderX? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("frameworkUpdates")
    val browseAlbumsFrameworkUpdates: BrowseAlbumsFrameworkUpdates? = null
)

@Serializable
data class BrowseAlbumsResponseContext(
    @SerialName("visitorData")
    val browseAlbumsVisitorData: String? = null,
    @SerialName("serviceTrackingParams")
    val browseAlbumsServiceTrackingParams: List<BrowseAlbumsServiceTrackingParam?>? = null
)

@Serializable
data class BrowseAlbumsContents(
    @SerialName("singleColumnBrowseResultsRenderer")
    val browseAlbumsSingleColumnBrowseResultsRenderer: BrowseAlbumsSingleColumnBrowseResultsRenderer? = null
)

@Serializable
data class BrowseAlbumsHeaderX(
    @SerialName("musicHeaderRenderer")
    val browseAlbumsMusicHeaderRenderer: BrowseAlbumsMusicHeaderRenderer? = null
)

@Serializable
data class BrowseAlbumsFrameworkUpdates(
    @SerialName("entityBatchUpdate")
    val browseAlbumsEntityBatchUpdate: BrowseAlbumsEntityBatchUpdate? = null
)

@Serializable
data class BrowseAlbumsServiceTrackingParam(
    @SerialName("service")
    val browseAlbumsService: String?,
    @SerialName("params")
    val browseAlbumsParams: List<BrowseAlbumsParam?>? = null
)

@Serializable
data class BrowseAlbumsParam(
    @SerialName("key")
    val browseAlbumsKey: String? = null,
    @SerialName("value")
    val browseAlbumsValue: String? = null
)

@Serializable
data class BrowseAlbumsSingleColumnBrowseResultsRenderer(
    @SerialName("tabs")
    val browseAlbumsTabs: List<BrowseAlbumsTab>? = null
)

@Serializable
data class BrowseAlbumsTab(
    @SerialName("tabRenderer")
    val browseAlbumsTabRenderer: BrowseAlbumsTabRenderer? = null
)

@Serializable
data class BrowseAlbumsTabRenderer(
    @SerialName("content")
    val browseAlbumsContent: BrowseAlbumsContent? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsContent(
    @SerialName("sectionListRenderer")
    val browseAlbumsSectionListRenderer: BrowseAlbumsSectionListRenderer? = null
)

@Serializable
data class BrowseAlbumsSectionListRenderer(
    @SerialName("contents")
    val browseAlbumsContents: List<BrowseAlbumsContentX>? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("header")
    val browseAlbumsHeader: BrowseAlbumsHeader? = null
)

@Serializable
data class BrowseAlbumsContentX(
    @SerialName("gridRenderer")
    val browseAlbumsGridRenderer: BrowseAlbumsGridRenderer? = null
)

@Serializable
data class BrowseAlbumsHeader(
    @SerialName("musicSideAlignedItemRenderer")
    val browseAlbumsMusicSideAlignedItemRenderer: BrowseAlbumsMusicSideAlignedItemRenderer? = null
)

@Serializable
data class BrowseAlbumsGridRenderer(
    @SerialName("items")
    val browseAlbumsItems: List<BrowseAlbumsItem>? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsItem(
    @SerialName("musicTwoRowItemRenderer")
    val browseAlbumsMusicTwoRowItemRenderer: BrowseAlbumsMusicTwoRowItemRenderer? = null
)

@Serializable
data class BrowseAlbumsMusicTwoRowItemRenderer(
    @SerialName("thumbnailRenderer")
    val browseAlbumsThumbnailRenderer: BrowseAlbumsThumbnailRenderer? = null,
    @SerialName("aspectRatio")
    val browseAlbumsAspectRatio: String? = null,
    @SerialName("title")
    val browseAlbumsTitle: BrowseAlbumsTitle? = null,
    @SerialName("subtitle")
    val browseAlbumsSubtitle: BrowseAlbumsSubtitle? = null,
    @SerialName("navigationEndpoint")
    val browseAlbumsNavigationEndpoint: BrowseAlbumsNavigationEndpointX? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("menu")
    val browseAlbumsMenu: BrowseAlbumsMenu? = null,
    @SerialName("thumbnailOverlay")
    val browseAlbumsThumbnailOverlay: BrowseAlbumsThumbnailOverlay? = null,
    @SerialName("subtitleBadges")
    val browseAlbumsSubtitleBadges: List<BrowseAlbumsSubtitleBadge>? = null
)

@Serializable
data class BrowseAlbumsThumbnailRenderer(
    @SerialName("musicThumbnailRenderer")
    val browseAlbumsMusicThumbnailRenderer: BrowseAlbumsMusicThumbnailRenderer? = null
)

@Serializable
data class BrowseAlbumsTitle(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRun>? = null
)

@Serializable
data class BrowseAlbumsSubtitle(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("browseEndpoint")
    val browseAlbumsBrowseEndpoint: BrowseAlbumsBrowseEndpoint? = null
)

@Serializable
data class BrowseAlbumsMenu(
    @SerialName("menuRenderer")
    val browseAlbumsMenuRenderer: BrowseAlbumsMenuRenderer? = null
)

@Serializable
data class BrowseAlbumsThumbnailOverlay(
    @SerialName("musicItemThumbnailOverlayRenderer")
    val browseAlbumsMusicItemThumbnailOverlayRenderer: BrowseAlbumsMusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class BrowseAlbumsSubtitleBadge(
    @SerialName("musicInlineBadgeRenderer")
    val browseAlbumsMusicInlineBadgeRenderer: BrowseAlbumsMusicInlineBadgeRenderer? = null
)

@Serializable
data class BrowseAlbumsMusicThumbnailRenderer(
    @SerialName("thumbnail")
    val browseAlbumsThumbnail: BrowseAlbumsThumbnail? = null,
    @SerialName("thumbnailCrop")
    val browseAlbumsThumbnailCrop: String? = null,
    @SerialName("thumbnailScale")
    val browseAlbumsThumbnailScale: String? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsThumbnail(
    @SerialName("thumbnails")
    val browseAlbumsThumbnails: List<BrowseAlbumsThumbnailX>? = null
)

@Serializable
data class BrowseAlbumsThumbnailX(
    @SerialName("url")
    val browseAlbumsUrl: String,
    @SerialName("width")
    val browseAlbumsWidth: Int,
    @SerialName("height")
    val browseAlbumsHeight: Int
)

@Serializable
data class BrowseAlbumsRun(
    @SerialName("text")
    val browseAlbumsText: String? = null,
    @SerialName("navigationEndpoint")
    val browseAlbumsNavigationEndpoint: BrowseAlbumsNavigationEndpointX? = null
)

@Serializable
data class BrowseAlbumsBrowseEndpoint(
    @SerialName("browseId")
    val browseAlbumsBrowseId: String? = null,
    @SerialName("params")
    val browseAlbumsParams: String? = null,
    @SerialName("browseEndpointContextSupportedConfigs")
    val browseAlbumsBrowseEndpointContextSupportedConfigs: BrowseAlbumsBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class BrowseAlbumsBrowseEndpointContextSupportedConfigs(
    @SerialName("browseEndpointContextMusicConfig")
    val browseAlbumsBrowseEndpointContextMusicConfig: BrowseAlbumsBrowseEndpointContextMusicConfig? = null
)

@Serializable
data class BrowseAlbumsBrowseEndpointContextMusicConfig(
    @SerialName("pageType")
    val browseAlbumsPageType: String? = null
)

@Serializable
data class BrowseAlbumsRunX(
    @SerialName("text")
    val browseAlbumsText: String? = null
)

@Serializable
data class BrowseAlbumsMenuRenderer(
    @SerialName("items")
    val browseAlbumsItems: List<BrowseAlbumsItemX>? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("accessibility")
    val browseAlbumsAccessibility: BrowseAlbumsAccessibility? = null
)

@Serializable
data class BrowseAlbumsItemX(
    @SerialName("menuNavigationItemRenderer")
    val browseAlbumsMenuNavigationItemRenderer: BrowseAlbumsMenuNavigationItemRenderer? = null,
    @SerialName("menuServiceItemRenderer")
    val browseAlbumsMenuServiceItemRenderer: BrowseAlbumsMenuServiceItemRenderer? = null,
    @SerialName("toggleMenuServiceItemRenderer")
    val browseAlbumsToggleMenuServiceItemRenderer: BrowseAlbumsToggleMenuServiceItemRenderer? = null
)

@Serializable
data class BrowseAlbumsAccessibility(
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityData? = null
)

@Serializable
data class BrowseAlbumsMenuNavigationItemRenderer(
    @SerialName("text")
    val browseAlbumsText: BrowseAlbumsText? = null,
    @SerialName("icon")
    val browseAlbumsIcon: BrowseAlbumsIcon? = null,
    @SerialName("navigationEndpoint")
    val browseAlbumsNavigationEndpoint: BrowseAlbumsNavigationEndpointXX? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsMenuServiceItemRenderer(
    @SerialName("text")
    val browseAlbumsText: BrowseAlbumsText? = null,
    @SerialName("icon")
    val browseAlbumsIcon: BrowseAlbumsIcon? = null,
    @SerialName("serviceEndpoint")
    val browseAlbumsServiceEndpoint: BrowseAlbumsServiceEndpoint? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsToggleMenuServiceItemRenderer(
    @SerialName("defaultText")
    val browseAlbumsDefaultText: BrowseAlbumsDefaultText? = null,
    @SerialName("defaultIcon")
    val browseAlbumsDefaultIcon: BrowseAlbumsDefaultIcon? = null,
    @SerialName("defaultServiceEndpoint")
    val browseAlbumsDefaultServiceEndpoint: BrowseAlbumsDefaultServiceEndpoint? = null,
    @SerialName("toggledText")
    val browseAlbumsToggledText: BrowseAlbumsToggledText? = null,
    @SerialName("toggledIcon")
    val browseAlbumsToggledIcon: BrowseAlbumsToggledIcon? = null,
    @SerialName("toggledServiceEndpoint")
    val browseAlbumsToggledServiceEndpoint: BrowseAlbumsToggledServiceEndpoint? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsText(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsIcon(
    @SerialName("iconType")
    val browseAlbumsIconType: String? = null
)

@Serializable
data class BrowseAlbumsNavigationEndpointXX(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("watchPlaylistEndpoint")
    val browseAlbumsWatchPlaylistEndpoint: BrowseAlbumsWatchPlaylistEndpoint? = null,
    @SerialName("modalEndpoint")
    val browseAlbumsModalEndpoint: BrowseAlbumsModalEndpoint? = null,
    @SerialName("browseEndpoint")
    val browseAlbumsBrowseEndpoint: BrowseAlbumsBrowseEndpointXX? = null,
    @SerialName("shareEntityEndpoint")
    val browseAlbumsShareEntityEndpoint: BrowseAlbumsShareEntityEndpoint? = null
)

@Serializable
data class BrowseAlbumsWatchPlaylistEndpoint(
    @SerialName("playlistId")
    val browseAlbumsPlaylistId: String? = null,
    @SerialName("params")
    val browseAlbumsParams: String? = null
)

@Serializable
data class BrowseAlbumsModalEndpoint(
    @SerialName("modal")
    val browseAlbumsModal: BrowseAlbumsModal? = null
)

@Serializable
data class BrowseAlbumsBrowseEndpointXX(
    @SerialName("browseId")
    val browseAlbumsBrowseId: String? = null,
    @SerialName("browseEndpointContextSupportedConfigs")
    val browseAlbumsBrowseEndpointContextSupportedConfigs: BrowseAlbumsBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class BrowseAlbumsShareEntityEndpoint(
    @SerialName("serializedShareEntity")
    val browseAlbumsSerializedShareEntity: String? = null,
    @SerialName("sharePanelType")
    val browseAlbumsSharePanelType: String? = null
)

@Serializable
data class BrowseAlbumsModal(
    @SerialName("modalWithTitleAndButtonRenderer")
    val browseAlbumsModalWithTitleAndButtonRenderer: BrowseAlbumsModalWithTitleAndButtonRenderer? = null
)

@Serializable
data class BrowseAlbumsModalWithTitleAndButtonRenderer(
    @SerialName("title")
    val browseAlbumsTitle: BrowseAlbumsTitleX? = null,
    @SerialName("content")
    val browseAlbumsContent: BrowseAlbumsContentXX? = null,
    @SerialName("button")
    val browseAlbumsButton: BrowseAlbumsButton? = null
)

@Serializable
data class BrowseAlbumsTitleX(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsContentXX(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsButton(
    @SerialName("buttonRenderer")
    val browseAlbumsButtonRenderer: BrowseAlbumsButtonRenderer? = null
)

@Serializable
data class BrowseAlbumsButtonRenderer(
    @SerialName("style")
    val browseAlbumsStyle: String? = null,
    @SerialName("isDisabled")
    val browseAlbumsIsDisabled: Boolean? = null,
    @SerialName("text")
    val browseAlbumsText: BrowseAlbumsText? = null,
    @SerialName("navigationEndpoint")
    val browseAlbumsNavigationEndpoint: BrowseAlbumsNavigationEndpointXXX? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsNavigationEndpointXXX(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("signInEndpoint")
    val browseAlbumsSignInEndpoint: BrowseAlbumsSignInEndpoint? = null
)

@Serializable
data class BrowseAlbumsSignInEndpoint(
    @SerialName("hack")
    val browseAlbumsHack: Boolean? = null
)

@Serializable
data class BrowseAlbumsServiceEndpoint(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("queueAddEndpoint")
    val browseAlbumsQueueAddEndpoint: BrowseAlbumsQueueAddEndpoint? = null
)

@Serializable
data class BrowseAlbumsQueueAddEndpoint(
    @SerialName("queueTarget")
    val browseAlbumsQueueTarget: BrowseAlbumsQueueTarget? = null,
    @SerialName("queueInsertPosition")
    val browseAlbumsQueueInsertPosition: String? = null,
    @SerialName("commands")
    val browseAlbumsCommands: List<BrowseAlbumsCommand>? = null
)

@Serializable
data class BrowseAlbumsQueueTarget(
    @SerialName("playlistId")
    val browseAlbumsPlaylistId: String? = null,
    @SerialName("onEmptyQueue")
    val browseAlbumsOnEmptyQueue: BrowseAlbumsOnEmptyQueue? = null
)

@Serializable
data class BrowseAlbumsCommand(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("addToToastAction")
    val browseAlbumsAddToToastAction: BrowseAlbumsAddToToastAction? = null
)

@Serializable
data class BrowseAlbumsOnEmptyQueue(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val browseAlbumsWatchEndpoint: BrowseAlbumsWatchEndpoint? = null
)

@Serializable
data class BrowseAlbumsWatchEndpoint(
    @SerialName("playlistId")
    val browseAlbumsPlaylistId: String? = null
)

@Serializable
data class BrowseAlbumsAddToToastAction(
    @SerialName("item")
    val browseAlbumsItem: BrowseAlbumsItemXX? = null
)

@Serializable
data class BrowseAlbumsItemXX(
    @SerialName("notificationTextRenderer")
    val browseAlbumsNotificationTextRenderer: BrowseAlbumsNotificationTextRenderer? = null
)

@Serializable
data class BrowseAlbumsNotificationTextRenderer(
    @SerialName("successResponseText")
    val browseAlbumsSuccessResponseText: BrowseAlbumsSuccessResponseText? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsSuccessResponseText(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsDefaultText(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsDefaultIcon(
    @SerialName("iconType")
    val browseAlbumsIconType: String? = null
)

@Serializable
data class BrowseAlbumsDefaultServiceEndpoint(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val browseAlbumsModalEndpoint: BrowseAlbumsModalEndpoint? = null
)

@Serializable
data class BrowseAlbumsToggledText(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsToggledIcon(
    @SerialName("iconType")
    val browseAlbumsIconType: String? = null
)

@Serializable
data class BrowseAlbumsToggledServiceEndpoint(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("likeEndpoint")
    val browseAlbumsLikeEndpoint: BrowseAlbumsLikeEndpoint? = null
)

@Serializable
data class BrowseAlbumsLikeEndpoint(
    @SerialName("status")
    val browseAlbumsStatus: String? = null,
    @SerialName("target")
    val browseAlbumsTarget: BrowseAlbumsTarget? = null
)

@Serializable
data class BrowseAlbumsTarget(
    @SerialName("playlistId")
    val browseAlbumsPlaylistId: String? = null
)

@Serializable
data class BrowseAlbumsAccessibilityData(
    @SerialName("label")
    val browseAlbumsLabel: String? = null
)

@Serializable
data class BrowseAlbumsMusicItemThumbnailOverlayRenderer(
    @SerialName("background")
    val browseAlbumsBackground: BrowseAlbumsBackground? = null,
    @SerialName("content")
    val browseAlbumsContent: BrowseAlbumsContentXXXX? = null,
    @SerialName("contentPosition")
    val browseAlbumsContentPosition: String? = null,
    @SerialName("displayStyle")
    val browseAlbumsDisplayStyle: String? = null
)

@Serializable
data class BrowseAlbumsBackground(
    @SerialName("verticalGradient")
    val browseAlbumsVerticalGradient: BrowseAlbumsVerticalGradient? = null
)

@Serializable
data class BrowseAlbumsContentXXXX(
    @SerialName("musicPlayButtonRenderer")
    val browseAlbumsMusicPlayButtonRenderer: BrowseAlbumsMusicPlayButtonRenderer? = null
)

@Serializable
data class BrowseAlbumsVerticalGradient(
    @SerialName("gradientLayerColors")
    val browseAlbumsGradientLayerColors: List<String?>? = null
)

@Serializable
data class BrowseAlbumsMusicPlayButtonRenderer(
    @SerialName("playNavigationEndpoint")
    val browseAlbumsPlayNavigationEndpoint: BrowseAlbumsPlayNavigationEndpoint? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("playIcon")
    val browseAlbumsPlayIcon: BrowseAlbumsPlayIcon? = null,
    @SerialName("pauseIcon")
    val browseAlbumsPauseIcon: BrowseAlbumsPauseIcon? = null,
    @SerialName("iconColor")
    val browseAlbumsIconColor: Long? = null,
    @SerialName("backgroundColor")
    val browseAlbumsBackgroundColor: Long? = null,
    @SerialName("activeBackgroundColor")
    val browseAlbumsActiveBackgroundColor: Long? = null,
    @SerialName("loadingIndicatorColor")
    val browseAlbumsLoadingIndicatorColor: Int? = null,
    @SerialName("playingIcon")
    val browseAlbumsPlayingIcon: BrowseAlbumsPlayingIcon? = null,
    @SerialName("iconLoadingColor")
    val browseAlbumsIconLoadingColor: Int? = null,
    @SerialName("activeScaleFactor")
    val browseAlbumsActiveScaleFactor: Double? = null,
    @SerialName("buttonSize")
    val browseAlbumsButtonSize: String? = null,
    @SerialName("rippleTarget")
    val browseAlbumsRippleTarget: String? = null,
    @SerialName("accessibilityPlayData")
    val browseAlbumsAccessibilityPlayData: BrowseAlbumsAccessibilityPlayData? = null,
    @SerialName("accessibilityPauseData")
    val browseAlbumsAccessibilityPauseData: BrowseAlbumsAccessibilityPauseData? = null
)

@Serializable
data class BrowseAlbumsPlayNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("watchPlaylistEndpoint")
    val browseAlbumsWatchPlaylistEndpoint: BrowseAlbumsWatchPlaylistEndpointX? = null
)

@Serializable
data class BrowseAlbumsPlayIcon(
    @SerialName("iconType")
    val browseAlbumsIconType: String? = null
)

@Serializable
data class BrowseAlbumsPauseIcon(
    @SerialName("iconType")
    val browseAlbumsIconType: String? = null
)

@Serializable
data class BrowseAlbumsPlayingIcon(
    @SerialName("iconType")
    val browseAlbumsIconType: String? = null
)

@Serializable
data class BrowseAlbumsAccessibilityPlayData(
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityData? = null
)

@Serializable
data class BrowseAlbumsAccessibilityPauseData(
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityData? = null
)

@Serializable
data class BrowseAlbumsWatchPlaylistEndpointX(
    @SerialName("playlistId")
    val browseAlbumsPlaylistId: String? = null
)

@Serializable
data class BrowseAlbumsMusicInlineBadgeRenderer(
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("icon")
    val browseAlbumsIcon: BrowseAlbumsIcon? = null,
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityDataXXX? = null
)

@Serializable
data class BrowseAlbumsAccessibilityDataXXX(
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityData? = null
)

@Serializable
data class BrowseAlbumsMusicSideAlignedItemRenderer(
    @SerialName("startItems")
    val browseAlbumsStartItems: List<BrowseAlbumsStartItem>? = null,
    @SerialName("endItems")
    val browseAlbumsEndItems: List<BrowseAlbumsEndItem>? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsStartItem(
    @SerialName("chipCloudRenderer")
    val browseAlbumsChipCloudRenderer: BrowseAlbumsChipCloudRenderer? = null
)

@Serializable
data class BrowseAlbumsEndItem(
    @SerialName("musicSortFilterButtonRenderer")
    val browseAlbumsMusicSortFilterButtonRenderer: BrowseAlbumsMusicSortFilterButtonRenderer? = null
)

@Serializable
data class BrowseAlbumsChipCloudRenderer(
    @SerialName("chips")
    val browseAlbumsChips: List<BrowseAlbumsChip>? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsChip(
    @SerialName("chipCloudChipRenderer")
    val browseAlbumsChipCloudChipRenderer: BrowseAlbumsChipCloudChipRenderer? = null
)

@Serializable
data class BrowseAlbumsChipCloudChipRenderer(
    @SerialName("text")
    val browseAlbumsText: BrowseAlbumsText? = null,
    @SerialName("navigationEndpoint")
    val browseAlbumsNavigationEndpoint: BrowseAlbumsNavigationEndpointXXXXX? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityDataXXX? = null,
    @SerialName("isSelected")
    val browseAlbumsIsSelected: Boolean? = null,
    @SerialName("onDeselectedCommand")
    val browseAlbumsOnDeselectedCommand: BrowseAlbumsOnDeselectedCommand? = null,
    @SerialName("uniqueId")
    val browseAlbumsUniqueId: String? = null
)

@Serializable
data class BrowseAlbumsNavigationEndpointXXXXX(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("browseSectionListReloadEndpoint")
    val browseAlbumsBrowseSectionListReloadEndpoint: BrowseAlbumsBrowseSectionListReloadEndpoint? = null
)

@Serializable
data class BrowseAlbumsOnDeselectedCommand(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("browseSectionListReloadEndpoint")
    val browseAlbumsBrowseSectionListReloadEndpoint: BrowseAlbumsBrowseSectionListReloadEndpoint? = null
)

@Serializable
data class BrowseAlbumsBrowseSectionListReloadEndpoint(
    @SerialName("continuation")
    val browseAlbumsContinuation: BrowseAlbumsContinuation? = null
)

@Serializable
data class BrowseAlbumsContinuation(
    @SerialName("reloadContinuationData")
    val browseAlbumsReloadContinuationData: BrowseAlbumsReloadContinuationData? = null
)

@Serializable
data class BrowseAlbumsReloadContinuationData(
    @SerialName("continuation")
    val browseAlbumsContinuation: String? = null,
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("showSpinnerOverlay")
    val browseAlbumsShowSpinnerOverlay: Boolean? = null
)

@Serializable
data class BrowseAlbumsMusicSortFilterButtonRenderer(
    @SerialName("title")
    val browseAlbumsTitle: BrowseAlbumsTitleX? = null,
    @SerialName("icon")
    val browseAlbumsIcon: BrowseAlbumsIcon? = null,
    @SerialName("menu")
    val browseAlbumsMenu: BrowseAlbumsMenuX? = null,
    @SerialName("accessibility")
    val browseAlbumsAccessibility: BrowseAlbumsAccessibility? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsMenuX(
    @SerialName("musicMultiSelectMenuRenderer")
    val browseAlbumsMusicMultiSelectMenuRenderer: BrowseAlbumsMusicMultiSelectMenuRenderer? = null
)

@Serializable
data class BrowseAlbumsMusicMultiSelectMenuRenderer(
    @SerialName("title")
    val browseAlbumsTitle: BrowseAlbumsTitleXXXX? = null,
    @SerialName("options")
    val browseAlbumsOptions: List<BrowseAlbumsOption>? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("formEntityKey")
    val browseAlbumsFormEntityKey: String? = null
)

@Serializable
data class BrowseAlbumsTitleXXXX(
    @SerialName("musicMenuTitleRenderer")
    val browseAlbumsMusicMenuTitleRenderer: BrowseAlbumsMusicMenuTitleRenderer? = null
)

@Serializable
data class BrowseAlbumsOption(
    @SerialName("musicMultiSelectMenuItemRenderer")
    val browseAlbumsMusicMultiSelectMenuItemRenderer: BrowseAlbumsMusicMultiSelectMenuItemRenderer? = null
)

@Serializable
data class BrowseAlbumsMusicMenuTitleRenderer(
    @SerialName("primaryText")
    val browseAlbumsPrimaryText: BrowseAlbumsPrimaryText? = null
)

@Serializable
data class BrowseAlbumsPrimaryText(
    @SerialName("runs")
    val browseAlbumsRuns: List<BrowseAlbumsRunX>? = null
)

@Serializable
data class BrowseAlbumsMusicMultiSelectMenuItemRenderer(
    @SerialName("title")
    val browseAlbumsTitle: BrowseAlbumsTitleX? = null,
    @SerialName("formItemEntityKey")
    val browseAlbumsFormItemEntityKey: String? = null,
    @SerialName("selectedCommand")
    val browseAlbumsSelectedCommand: BrowseAlbumsSelectedCommand? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null,
    @SerialName("selectedIcon")
    val browseAlbumsSelectedIcon: BrowseAlbumsSelectedIcon? = null,
    @SerialName("selectedAccessibility")
    val browseAlbumsSelectedAccessibility: BrowseAlbumsSelectedAccessibility? = null,
    @SerialName("deselectedAccessibility")
    val browseAlbumsDeselectedAccessibility: BrowseAlbumsDeselectedAccessibility? = null
)

@Serializable
data class BrowseAlbumsSelectedCommand(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("commandExecutorCommand")
    val browseAlbumsCommandExecutorCommand: BrowseAlbumsCommandExecutorCommand? = null
)

@Serializable
data class BrowseAlbumsSelectedIcon(
    @SerialName("iconType")
    val browseAlbumsIconType: String? = null
)

@Serializable
data class BrowseAlbumsSelectedAccessibility(
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityData? = null
)

@Serializable
data class BrowseAlbumsDeselectedAccessibility(
    @SerialName("accessibilityData")
    val browseAlbumsAccessibilityData: BrowseAlbumsAccessibilityData? = null
)

@Serializable
data class BrowseAlbumsCommandExecutorCommand(
    @SerialName("commands")
    val browseAlbumsCommands: List<BrowseAlbumsCommandX>? = null
)

@Serializable
data class BrowseAlbumsCommandX(
    @SerialName("clickTrackingParams")
    val browseAlbumsClickTrackingParams: String? = null,
    @SerialName("musicCheckboxFormItemMutatedCommand")
    val browseAlbumsMusicCheckboxFormItemMutatedCommand: BrowseAlbumsMusicCheckboxFormItemMutatedCommand? = null,
    @SerialName("browseSectionListReloadEndpoint")
    val browseAlbumsBrowseSectionListReloadEndpoint: BrowseAlbumsBrowseSectionListReloadEndpoint? = null
)

@Serializable
data class BrowseAlbumsMusicCheckboxFormItemMutatedCommand(
    @SerialName("formItemEntityKey")
    val browseAlbumsFormItemEntityKey: String? = null,
    @SerialName("newCheckedState")
    val browseAlbumsNewCheckedState: Boolean? = null
)

@Serializable
data class BrowseAlbumsMusicHeaderRenderer(
    @SerialName("title")
    val browseAlbumsTitle: BrowseAlbumsTitleX? = null,
    @SerialName("trackingParams")
    val browseAlbumsTrackingParams: String? = null
)

@Serializable
data class BrowseAlbumsEntityBatchUpdate(
    @SerialName("mutations")
    val browseAlbumsMutations: List<BrowseAlbumsMutation?>? = null,
    @SerialName("timestamp")
    val browseAlbumsTimestamp: BrowseAlbumsTimestamp? = null
)

@Serializable
data class BrowseAlbumsMutation(
    @SerialName("entityKey")
    val browseAlbumsEntityKey: String? = null,
    @SerialName("type")
    val browseAlbumsType: String? = null,
    @SerialName("payload")
    val browseAlbumsPayload: BrowseAlbumsPayload? = null
)

@Serializable
data class BrowseAlbumsTimestamp(
    @SerialName("seconds")
    val browseAlbumsSeconds: String? = null,
    @SerialName("nanos")
    val browseAlbumsNanos: Int? = null
)

@Serializable
data class BrowseAlbumsPayload(
    @SerialName("musicForm")
    val browseAlbumsMusicForm: BrowseAlbumsMusicForm? = null,
    @SerialName("musicFormBooleanChoice")
    val browseAlbumsMusicFormBooleanChoice: BrowseAlbumsMusicFormBooleanChoice? = null
)

@Serializable
data class BrowseAlbumsMusicForm(
    @SerialName("id")
    val browseAlbumsId: String? = null,
    @SerialName("booleanChoiceEntityKeys")
    val browseAlbumsBooleanChoiceEntityKeys: List<String?>? = null
)

@Serializable
data class BrowseAlbumsMusicFormBooleanChoice(
    @SerialName("id")
    val browseAlbumsId: String? = null,
    @SerialName("parentFormEntityKey")
    val browseAlbumsParentFormEntityKey: String? = null,
    @SerialName("selected")
    val browseAlbumsSelected: Boolean? = null,
    @SerialName("opaqueToken")
    val browseAlbumsOpaqueToken: String? = null
)