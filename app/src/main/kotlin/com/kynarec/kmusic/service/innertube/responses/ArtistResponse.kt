package com.kynarec.kmusic.service.innertube.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistResponse(
    @SerialName("responseContext")
    val artistResponseContext: ArtistResponseContext? = null,
    @SerialName("contents")
    val artistContents: ArtistContents? = null,
    @SerialName("header")
    val artistHeader: ArtistHeaderXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("microformat")
    val artistMicroformat: ArtistMicroformat? = null
)

@Serializable
data class ArtistResponseContext(
    @SerialName("visitorData")
    val artistVisitorData: String? = null,
    @SerialName("serviceTrackingParams")
    val artistServiceTrackingParams: List<ArtistServiceTrackingParam?>? = null,
    @SerialName("maxAgeSeconds")
    val artistMaxAgeSeconds: Int? = null
)

@Serializable
data class ArtistContents(
    @SerialName("singleColumnBrowseResultsRenderer")
    val artistSingleColumnBrowseResultsRenderer: ArtistSingleColumnBrowseResultsRenderer? = null
)

@Serializable
data class ArtistHeaderXX(
    @SerialName("musicImmersiveHeaderRenderer")
    val artistMusicImmersiveHeaderRenderer: ArtistMusicImmersiveHeaderRenderer? = null
)

@Serializable
data class ArtistMicroformat(
    @SerialName("microformatDataRenderer")
    val artistMicroformatDataRenderer: ArtistMicroformatDataRenderer? = null
)

@Serializable
data class ArtistServiceTrackingParam(
    @SerialName("service")
    val artistService: String? = null,
    @SerialName("params")
    val artistParams: List<ArtistParam?>? = null
)

@Serializable
data class ArtistParam(
    @SerialName("key")
    val artistKey: String? = null,
    @SerialName("value")
    val artistValue: String? = null
)

@Serializable
data class ArtistSingleColumnBrowseResultsRenderer(
    @SerialName("tabs")
    val artistTabs: List<ArtistTab>? = null
)

@Serializable
data class ArtistTab(
    @SerialName("tabRenderer")
    val artistTabRenderer: ArtistTabRenderer? = null
)

@Serializable
data class ArtistTabRenderer(
    @SerialName("title")
    val artistTitle: String? = null,
    @SerialName("content")
    val artistContent: ArtistContent? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistContent(
    @SerialName("sectionListRenderer")
    val artistSectionListRenderer: ArtistSectionListRenderer? = null
)

@Serializable
data class ArtistSectionListRenderer(
    @SerialName("contents")
    val artistContents: List<ArtistContentX>? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistContentX(
    @SerialName("musicShelfRenderer")
    val artistMusicShelfRenderer: ArtistMusicShelfRenderer? = null,
    @SerialName("musicCarouselShelfRenderer")
    val artistMusicCarouselShelfRenderer: ArtistMusicCarouselShelfRenderer? = null,
    @SerialName("musicDescriptionShelfRenderer")
    val artistMusicDescriptionShelfRenderer: ArtistMusicDescriptionShelfRenderer? = null
)

@Serializable
data class ArtistMusicShelfRenderer(
    @SerialName("title")
    val artistTitle: ArtistTitle? = null,
    @SerialName("contents")
    val artistContents: List<ArtistContentXX>? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("bottomText")
    val artistBottomText: ArtistBottomText? = null,
    @SerialName("bottomEndpoint")
    val artistBottomEndpoint: ArtistBottomEndpoint? = null,
    @SerialName("shelfDivider")
    val artistShelfDivider: ArtistShelfDivider? = null
)

@Serializable
data class ArtistMusicCarouselShelfRenderer(
    @SerialName("header")
    val artistHeader: ArtistHeader? = null,
    @SerialName("contents")
    val artistContents: List<ArtistContentXXXXXXX>? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("itemSize")
    val artistItemSize: String? = null
)

@Serializable
data class ArtistMusicDescriptionShelfRenderer(
    @SerialName("header")
    val artistHeader: ArtistHeaderX? = null,
    @SerialName("subheader")
    val artistSubheader: ArtistSubheader? = null,
    @SerialName("description")
    val artistDescription: ArtistDescription? = null,
    @SerialName("moreButton")
    val artistMoreButton: ArtistMoreButton? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistTitle(
    @SerialName("runs")
    val artistRuns: List<ArtistRun?>? = null
)

@Serializable
data class ArtistContentXX(
    @SerialName("musicResponsiveListItemRenderer")
    val artistMusicResponsiveListItemRenderer: ArtistMusicResponsiveListItemRenderer? = null
)

@Serializable
data class ArtistBottomText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistBottomEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("browseEndpoint")
    val artistBrowseEndpoint: ArtistBrowseEndpoint? = null
)

@Serializable
data class ArtistShelfDivider(
    @SerialName("musicShelfDividerRenderer")
    val artistMusicShelfDividerRenderer: ArtistMusicShelfDividerRenderer? = null
)

@Serializable
data class ArtistRun(
    @SerialName("text")
    val artistText: String? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpoint? = null
)

@Serializable
data class ArtistNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("browseEndpoint")
    val artistBrowseEndpoint: ArtistBrowseEndpoint? = null
)

@Serializable
data class ArtistBrowseEndpoint(
    @SerialName("browseId")
    val artistBrowseId: String? = null,
    @SerialName("params")
    val artistParams: String? = null,
    @SerialName("browseEndpointContextSupportedConfigs")
    val artistBrowseEndpointContextSupportedConfigs: ArtistBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class ArtistBrowseEndpointContextSupportedConfigs(
    @SerialName("browseEndpointContextMusicConfig")
    val artistBrowseEndpointContextMusicConfig: ArtistBrowseEndpointContextMusicConfig? = null
)

@Serializable
data class ArtistBrowseEndpointContextMusicConfig(
    @SerialName("pageType")
    val artistPageType: String? = null
)

@Serializable
data class ArtistMusicResponsiveListItemRenderer(
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("thumbnail")
    val artistThumbnail: ArtistThumbnail? = null,
    @SerialName("overlay")
    val artistOverlay: ArtistOverlay? = null,
    @SerialName("flexColumns")
    val artistFlexColumns: List<ArtistFlexColumn>? = null,
    @SerialName("menu")
    val artistMenu: ArtistMenu? = null,
    @SerialName("badges")
    val artistBadges: List<ArtistBadge>? = null,
    @SerialName("playlistItemData")
    val artistPlaylistItemData: ArtistPlaylistItemData? = null
)

@Serializable
data class ArtistThumbnail(
    @SerialName("musicThumbnailRenderer")
    val artistMusicThumbnailRenderer: ArtistMusicThumbnailRenderer? = null
)

@Serializable
data class ArtistOverlay(
    @SerialName("musicItemThumbnailOverlayRenderer")
    val artistMusicItemThumbnailOverlayRenderer: ArtistMusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class ArtistFlexColumn(
    @SerialName("musicResponsiveListItemFlexColumnRenderer")
    val artistMusicResponsiveListItemFlexColumnRenderer: ArtistMusicResponsiveListItemFlexColumnRenderer? = null
)

@Serializable
data class ArtistMenu(
    @SerialName("menuRenderer")
    val artistMenuRenderer: ArtistMenuRenderer? = null
)

@Serializable
data class ArtistBadge(
    @SerialName("musicInlineBadgeRenderer")
    val artistMusicInlineBadgeRenderer: ArtistMusicInlineBadgeRenderer? = null
)

@Serializable
data class ArtistPlaylistItemData(
    @SerialName("videoId")
    val artistVideoId: String? = null
)

@Serializable
data class ArtistMusicThumbnailRenderer(
    @SerialName("thumbnail")
    val artistThumbnail: ArtistThumbnailX? = null,
    @SerialName("thumbnailCrop")
    val artistThumbnailCrop: String? = null,
    @SerialName("thumbnailScale")
    val artistThumbnailScale: String? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistThumbnailX(
    @SerialName("thumbnails")
    val artistThumbnails: List<ArtistThumbnailXX>? = null
)

@Serializable
data class ArtistThumbnailXX(
    @SerialName("url")
    val artistUrl: String,
    @SerialName("width")
    val artistWidth: Int,
    @SerialName("height")
    val artistHeight: Int
)

@Serializable
data class ArtistMusicItemThumbnailOverlayRenderer(
    @SerialName("background")
    val artistBackground: ArtistBackground? = null,
    @SerialName("content")
    val artistContent: ArtistContentXXX? = null,
    @SerialName("contentPosition")
    val artistContentPosition: String? = null,
    @SerialName("displayStyle")
    val artistDisplayStyle: String? = null
)

@Serializable
data class ArtistBackground(
    @SerialName("verticalGradient")
    val artistVerticalGradient: ArtistVerticalGradient? = null
)

@Serializable
data class ArtistContentXXX(
    @SerialName("musicPlayButtonRenderer")
    val artistMusicPlayButtonRenderer: ArtistMusicPlayButtonRenderer? = null
)

@Serializable
data class ArtistVerticalGradient(
    @SerialName("gradientLayerColors")
    val artistGradientLayerColors: List<String?>? = null
)

@Serializable
data class ArtistMusicPlayButtonRenderer(
    @SerialName("playNavigationEndpoint")
    val artistPlayNavigationEndpoint: ArtistPlayNavigationEndpoint? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("playIcon")
    val artistPlayIcon: ArtistPlayIcon? = null,
    @SerialName("pauseIcon")
    val artistPauseIcon: ArtistPauseIcon? = null,
    @SerialName("iconColor")
    val artistIconColor: Long? = null,
    @SerialName("backgroundColor")
    val artistBackgroundColor: Int? = null,
    @SerialName("activeBackgroundColor")
    val artistActiveBackgroundColor: Int? = null,
    @SerialName("loadingIndicatorColor")
    val artistLoadingIndicatorColor: Int? = null,
    @SerialName("playingIcon")
    val artistPlayingIcon: ArtistPlayingIcon? = null,
    @SerialName("iconLoadingColor")
    val artistIconLoadingColor: Int? = null,
    @SerialName("activeScaleFactor")
    val artistActiveScaleFactor: Int? = null,
    @SerialName("buttonSize")
    val artistButtonSize: String? = null,
    @SerialName("rippleTarget")
    val artistRippleTarget: String? = null,
    @SerialName("accessibilityPlayData")
    val artistAccessibilityPlayData: ArtistAccessibilityPlayData? = null,
    @SerialName("accessibilityPauseData")
    val artistAccessibilityPauseData: ArtistAccessibilityPauseData? = null
)

@Serializable
data class ArtistPlayNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpoint? = null
)

@Serializable
data class ArtistPlayIcon(
    @SerialName("iconType")
    val artistIconType: String? = null
)

@Serializable
data class ArtistPauseIcon(
    @SerialName("iconType")
    val artistIconType: String? = null
)

@Serializable
data class ArtistPlayingIcon(
    @SerialName("iconType")
    val artistIconType: String? = null
)

@Serializable
data class ArtistAccessibilityPlayData(
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityData? = null
)

@Serializable
data class ArtistAccessibilityPauseData(
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityData? = null
)

@Serializable
data class ArtistWatchEndpoint(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("index")
    val artistIndex: Int? = null,
    @SerialName("loggingContext")
    val artistLoggingContext: ArtistLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val artistWatchEndpointMusicSupportedConfigs: ArtistWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class ArtistLoggingContext(
    @SerialName("vssLoggingContext")
    val artistVssLoggingContext: ArtistVssLoggingContext? = null
)

@Serializable
data class ArtistWatchEndpointMusicSupportedConfigs(
    @SerialName("watchEndpointMusicConfig")
    val artistWatchEndpointMusicConfig: ArtistWatchEndpointMusicConfig? = null
)

@Serializable
data class ArtistVssLoggingContext(
    @SerialName("serializedContextData")
    val artistSerializedContextData: String? = null
)

@Serializable
data class ArtistWatchEndpointMusicConfig(
    @SerialName("musicVideoType")
    val artistMusicVideoType: String? = null
)

@Serializable
data class ArtistAccessibilityData(
    @SerialName("label")
    val artistLabel: String? = null
)

@Serializable
data class ArtistMusicResponsiveListItemFlexColumnRenderer(
    @SerialName("text")
    val artistText: ArtistText? = null,
    @SerialName("displayPriority")
    val artistDisplayPriority: String? = null
)

@Serializable
data class ArtistText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunX>? = null
)

@Serializable
data class ArtistRunX(
    @SerialName("text")
    val artistText: String? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointX? = null
)

@Serializable
data class ArtistNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpointX? = null,
    @SerialName("browseEndpoint")
    val artistBrowseEndpoint: ArtistBrowseEndpointX? = null
)

@Serializable
data class ArtistWatchEndpointX(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("loggingContext")
    val artistLoggingContext: ArtistLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val artistWatchEndpointMusicSupportedConfigs: ArtistWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class ArtistBrowseEndpointX(
    @SerialName("browseId")
    val artistBrowseId: String? = null,
    @SerialName("browseEndpointContextSupportedConfigs")
    val artistBrowseEndpointContextSupportedConfigs: ArtistBrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class ArtistMenuRenderer(
    @SerialName("items")
    val artistItems: List<ArtistItem>? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("topLevelButtons")
    val artistTopLevelButtons: List<ArtistTopLevelButton>? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibility? = null
)

@Serializable
data class ArtistItem(
    @SerialName("menuNavigationItemRenderer")
    val artistMenuNavigationItemRenderer: ArtistMenuNavigationItemRenderer? = null,
    @SerialName("menuServiceItemRenderer")
    val artistMenuServiceItemRenderer: ArtistMenuServiceItemRenderer? = null,
    @SerialName("menuServiceItemDownloadRenderer")
    val artistMenuServiceItemDownloadRenderer: ArtistMenuServiceItemDownloadRenderer? = null
)

@Serializable
data class ArtistTopLevelButton(
    @SerialName("likeButtonRenderer")
    val artistLikeButtonRenderer: ArtistLikeButtonRenderer? = null
)

@Serializable
data class ArtistAccessibility(
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityData? = null
)

@Serializable
data class ArtistMenuNavigationItemRenderer(
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistMenuServiceItemRenderer(
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("serviceEndpoint")
    val artistServiceEndpoint: ArtistServiceEndpoint? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistMenuServiceItemDownloadRenderer(
    @SerialName("serviceEndpoint")
    val artistServiceEndpoint: ArtistServiceEndpointX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("badgeIcon")
    val artistBadgeIcon: ArtistBadgeIcon? = null
)

@Serializable
data class ArtistTextX(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistIcon(
    @SerialName("iconType")
    val artistIconType: String? = null
)

@Serializable
data class ArtistNavigationEndpointXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpointXX? = null,
    @SerialName("modalEndpoint")
    val artistModalEndpoint: ArtistModalEndpoint? = null,
    @SerialName("browseEndpoint")
    val artistBrowseEndpoint: ArtistBrowseEndpointX? = null,
    @SerialName("shareEntityEndpoint")
    val artistShareEntityEndpoint: ArtistShareEntityEndpoint? = null
)

@Serializable
data class ArtistRunXX(
    @SerialName("text")
    val artistText: String? = null
)

@Serializable
data class ArtistWatchEndpointXX(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("params")
    val artistParams: String? = null,
    @SerialName("loggingContext")
    val artistLoggingContext: ArtistLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val artistWatchEndpointMusicSupportedConfigs: ArtistWatchEndpointMusicSupportedConfigs? = null,
    @SerialName("playerParams")
    val artistPlayerParams: String? = null
)

@Serializable
data class ArtistModalEndpoint(
    @SerialName("modal")
    val artistModal: ArtistModal? = null
)

@Serializable
data class ArtistShareEntityEndpoint(
    @SerialName("serializedShareEntity")
    val artistSerializedShareEntity: String? = null,
    @SerialName("sharePanelType")
    val artistSharePanelType: String? = null
)

@Serializable
data class ArtistModal(
    @SerialName("modalWithTitleAndButtonRenderer")
    val artistModalWithTitleAndButtonRenderer: ArtistModalWithTitleAndButtonRenderer? = null
)

@Serializable
data class ArtistModalWithTitleAndButtonRenderer(
    @SerialName("title")
    val artistTitle: ArtistTitleX? = null,
    @SerialName("content")
    val artistContent: ArtistContentXXXX? = null,
    @SerialName("button")
    val artistButton: ArtistButton? = null
)

@Serializable
data class ArtistTitleX(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistContentXXXX(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistButton(
    @SerialName("buttonRenderer")
    val artistButtonRenderer: ArtistButtonRenderer? = null
)

@Serializable
data class ArtistButtonRenderer(
    @SerialName("style")
    val artistStyle: String? = null,
    @SerialName("isDisabled")
    val artistIsDisabled: Boolean? = null,
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistNavigationEndpointXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("signInEndpoint")
    val artistSignInEndpoint: ArtistSignInEndpoint? = null
)

@Serializable
data class ArtistSignInEndpoint(
    @SerialName("hack")
    val artistHack: Boolean? = null
)

@Serializable
data class ArtistServiceEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("queueAddEndpoint")
    val artistQueueAddEndpoint: ArtistQueueAddEndpoint? = null
)

@Serializable
data class ArtistQueueAddEndpoint(
    @SerialName("queueTarget")
    val artistQueueTarget: ArtistQueueTarget? = null,
    @SerialName("queueInsertPosition")
    val artistQueueInsertPosition: String? = null,
    @SerialName("commands")
    val artistCommands: List<ArtistCommand>? = null
)

@Serializable
data class ArtistQueueTarget(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("onEmptyQueue")
    val artistOnEmptyQueue: ArtistOnEmptyQueue? = null
)

@Serializable
data class ArtistCommand(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("addToToastAction")
    val artistAddToToastAction: ArtistAddToToastAction? = null
)

@Serializable
data class ArtistOnEmptyQueue(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpointXXX? = null
)

@Serializable
data class ArtistWatchEndpointXXX(
    @SerialName("videoId")
    val artistVideoId: String? = null
)

@Serializable
data class ArtistAddToToastAction(
    @SerialName("item")
    val artistItem: ArtistItemX? = null
)

@Serializable
data class ArtistItemX(
    @SerialName("notificationTextRenderer")
    val artistNotificationTextRenderer: ArtistNotificationTextRenderer? = null
)

@Serializable
data class ArtistNotificationTextRenderer(
    @SerialName("successResponseText")
    val artistSuccessResponseText: ArtistSuccessResponseText? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistSuccessResponseText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistServiceEndpointX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("offlineVideoEndpoint")
    val artistOfflineVideoEndpoint: ArtistOfflineVideoEndpoint? = null
)

@Serializable
data class ArtistBadgeIcon(
    @SerialName("iconType")
    val artistIconType: String? = null
)

@Serializable
data class ArtistOfflineVideoEndpoint(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("onAddCommand")
    val artistOnAddCommand: ArtistOnAddCommand? = null
)

@Serializable
data class ArtistOnAddCommand(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("getDownloadActionCommand")
    val artistGetDownloadActionCommand: ArtistGetDownloadActionCommand? = null
)

@Serializable
data class ArtistGetDownloadActionCommand(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("params")
    val artistParams: String? = null
)

@Serializable
data class ArtistLikeButtonRenderer(
    @SerialName("target")
    val artistTarget: ArtistTarget? = null,
    @SerialName("likeStatus")
    val artistLikeStatus: String? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("likesAllowed")
    val artistLikesAllowed: Boolean? = null,
    @SerialName("dislikeNavigationEndpoint")
    val artistDislikeNavigationEndpoint: ArtistDislikeNavigationEndpoint? = null,
    @SerialName("likeCommand")
    val artistLikeCommand: ArtistLikeCommand? = null
)

@Serializable
data class ArtistTarget(
    @SerialName("videoId")
    val artistVideoId: String? = null
)

@Serializable
data class ArtistDislikeNavigationEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val artistModalEndpoint: ArtistModalEndpoint? = null
)

@Serializable
data class ArtistLikeCommand(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val artistModalEndpoint: ArtistModalEndpoint? = null
)

@Serializable
data class ArtistMusicInlineBadgeRenderer(
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityDataXXX? = null
)

@Serializable
data class ArtistAccessibilityDataXXX(
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityData? = null
)

@Serializable
data class ArtistMusicShelfDividerRenderer(
    @SerialName("hidden")
    val artistHidden: Boolean? = null
)

@Serializable
data class ArtistHeader(
    @SerialName("musicCarouselShelfBasicHeaderRenderer")
    val artistMusicCarouselShelfBasicHeaderRenderer: ArtistMusicCarouselShelfBasicHeaderRenderer? = null
)

@Serializable
data class ArtistContentXXXXXXX(
    @SerialName("musicTwoRowItemRenderer")
    val artistMusicTwoRowItemRenderer: ArtistMusicTwoRowItemRenderer? = null
)

@Serializable
data class ArtistMusicCarouselShelfBasicHeaderRenderer(
    @SerialName("title")
    val artistTitle: ArtistTitleXXXX? = null,
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityDataXXX? = null,
    @SerialName("headerStyle")
    val artistHeaderStyle: String? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("moreContentButton")
    val artistMoreContentButton: ArtistMoreContentButton? = null
)

@Serializable
data class ArtistTitleXXXX(
    @SerialName("runs")
    val artistRuns: List<ArtistRun>? = null
)

@Serializable
data class ArtistMoreContentButton(
    @SerialName("buttonRenderer")
    val artistButtonRenderer: ArtistButtonRendererXXX? = null
)

@Serializable
data class ArtistButtonRendererXXX(
    @SerialName("style")
    val artistStyle: String? = null,
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpoint? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityDataXXX? = null
)

@Serializable
data class ArtistMusicTwoRowItemRenderer(
    @SerialName("thumbnailRenderer")
    val artistThumbnailRenderer: ArtistThumbnailRenderer? = null,
    @SerialName("aspectRatio")
    val artistAspectRatio: String? = null,
    @SerialName("title")
    val artistTitle: ArtistTitleXXXX? = null,
    @SerialName("subtitle")
    val artistSubtitle: ArtistSubtitle? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXXXXXXXXXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("menu")
    val artistMenu: ArtistMenuX? = null,
    @SerialName("thumbnailOverlay")
    val artistThumbnailOverlay: ArtistThumbnailOverlay? = null,
    @SerialName("subtitleBadges")
    val artistSubtitleBadges: List<ArtistSubtitleBadge>? = null
)

@Serializable
data class ArtistThumbnailRenderer(
    @SerialName("musicThumbnailRenderer")
    val artistMusicThumbnailRenderer: ArtistMusicThumbnailRenderer? = null
)

@Serializable
data class ArtistSubtitle(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXXXXXXXXXXXXXXXXXX>? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibility? = null
)

@Serializable
data class ArtistNavigationEndpointXXXXXXXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("browseEndpoint")
    val artistBrowseEndpoint: ArtistBrowseEndpoint? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpoint? = null
)

@Serializable
data class ArtistMenuX(
    @SerialName("menuRenderer")
    val artistMenuRenderer: ArtistMenuRendererX? = null
)

@Serializable
data class ArtistThumbnailOverlay(
    @SerialName("musicItemThumbnailOverlayRenderer")
    val artistMusicItemThumbnailOverlayRenderer: ArtistMusicItemThumbnailOverlayRendererX? = null
)

@Serializable
data class ArtistSubtitleBadge(
    @SerialName("musicInlineBadgeRenderer")
    val artistMusicInlineBadgeRenderer: ArtistMusicInlineBadgeRenderer? = null
)

@Serializable
data class ArtistRunXXXXXXXXXXXXXXXXXX(
    @SerialName("text")
    val artistText: String? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXXXXXXXXX? = null
)

@Serializable
data class ArtistNavigationEndpointXXXXXXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("browseEndpoint")
    val artistBrowseEndpoint: ArtistBrowseEndpointX? = null
)

@Serializable
data class ArtistMenuRendererX(
    @SerialName("items")
    val artistItems: List<ArtistItemXX>? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibility? = null
)

@Serializable
data class ArtistItemXX(
    @SerialName("menuNavigationItemRenderer")
    val artistMenuNavigationItemRenderer: ArtistMenuNavigationItemRendererX? = null,
    @SerialName("menuServiceItemRenderer")
    val artistMenuServiceItemRenderer: ArtistMenuServiceItemRendererX? = null,
    @SerialName("toggleMenuServiceItemRenderer")
    val artistToggleMenuServiceItemRenderer: ArtistToggleMenuServiceItemRenderer? = null,
    @SerialName("menuServiceItemDownloadRenderer")
    val artistMenuServiceItemDownloadRenderer: ArtistMenuServiceItemDownloadRenderer? = null
)

@Serializable
data class ArtistMenuNavigationItemRendererX(
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXXXXXXXXXXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistMenuServiceItemRendererX(
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("serviceEndpoint")
    val artistServiceEndpoint: ArtistServiceEndpointXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistToggleMenuServiceItemRenderer(
    @SerialName("defaultText")
    val artistDefaultText: ArtistDefaultText? = null,
    @SerialName("defaultIcon")
    val artistDefaultIcon: ArtistDefaultIcon? = null,
    @SerialName("defaultServiceEndpoint")
    val artistDefaultServiceEndpoint: ArtistDefaultServiceEndpoint? = null,
    @SerialName("toggledText")
    val artistToggledText: ArtistToggledText? = null,
    @SerialName("toggledIcon")
    val artistToggledIcon: ArtistToggledIcon? = null,
    @SerialName("toggledServiceEndpoint")
    val artistToggledServiceEndpoint: ArtistToggledServiceEndpoint? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistNavigationEndpointXXXXXXXXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchPlaylistEndpoint")
    val artistWatchPlaylistEndpoint: ArtistWatchPlaylistEndpoint? = null,
    @SerialName("modalEndpoint")
    val artistModalEndpoint: ArtistModalEndpoint? = null,
    @SerialName("shareEntityEndpoint")
    val artistShareEntityEndpoint: ArtistShareEntityEndpoint? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpointXXXXX? = null
)

@Serializable
data class ArtistWatchPlaylistEndpoint(
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("params")
    val artistParams: String? = null
)

@Serializable
data class ArtistWatchEndpointXXXXX(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("params")
    val artistParams: String? = null,
    @SerialName("loggingContext")
    val artistLoggingContext: ArtistLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val artistWatchEndpointMusicSupportedConfigs: ArtistWatchEndpointMusicSupportedConfigs? = null
)

@Serializable
data class ArtistServiceEndpointXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("queueAddEndpoint")
    val artistQueueAddEndpoint: ArtistQueueAddEndpointX? = null
)

@Serializable
data class ArtistQueueAddEndpointX(
    @SerialName("queueTarget")
    val artistQueueTarget: ArtistQueueTargetX? = null,
    @SerialName("queueInsertPosition")
    val artistQueueInsertPosition: String? = null,
    @SerialName("commands")
    val artistCommands: List<ArtistCommand>? = null
)

@Serializable
data class ArtistQueueTargetX(
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("onEmptyQueue")
    val artistOnEmptyQueue: ArtistOnEmptyQueueX? = null,
    @SerialName("videoId")
    val artistVideoId: String? = null
)

@Serializable
data class ArtistOnEmptyQueueX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpointXXXXXX? = null
)

@Serializable
data class ArtistWatchEndpointXXXXXX(
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("videoId")
    val artistVideoId: String? = null
)

@Serializable
data class ArtistDefaultText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistDefaultIcon(
    @SerialName("iconType")
    val artistIconType: String? = null
)

@Serializable
data class ArtistDefaultServiceEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val artistModalEndpoint: ArtistModalEndpoint? = null
)

@Serializable
data class ArtistToggledText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistToggledIcon(
    @SerialName("iconType")
    val artistIconType: String? = null
)

@Serializable
data class ArtistToggledServiceEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("likeEndpoint")
    val artistLikeEndpoint: ArtistLikeEndpoint? = null
)

@Serializable
data class ArtistLikeEndpoint(
    @SerialName("status")
    val artistStatus: String? = null,
    @SerialName("target")
    val artistTarget: ArtistTargetX? = null
)

@Serializable
data class ArtistTargetX(
    @SerialName("playlistId")
    val artistPlaylistId: String? = null
)

@Serializable
data class ArtistMusicItemThumbnailOverlayRendererX(
    @SerialName("background")
    val artistBackground: ArtistBackground? = null,
    @SerialName("content")
    val artistContent: ArtistContentXXXXXXXXXX? = null,
    @SerialName("contentPosition")
    val artistContentPosition: String? = null,
    @SerialName("displayStyle")
    val artistDisplayStyle: String? = null
)

@Serializable
data class ArtistContentXXXXXXXXXX(
    @SerialName("musicPlayButtonRenderer")
    val artistMusicPlayButtonRenderer: ArtistMusicPlayButtonRendererX? = null
)

@Serializable
data class ArtistMusicPlayButtonRendererX(
    @SerialName("playNavigationEndpoint")
    val artistPlayNavigationEndpoint: ArtistPlayNavigationEndpointX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("playIcon")
    val artistPlayIcon: ArtistPlayIcon? = null,
    @SerialName("pauseIcon")
    val artistPauseIcon: ArtistPauseIcon? = null,
    @SerialName("iconColor")
    val artistIconColor: Long? = null,
    @SerialName("backgroundColor")
    val artistBackgroundColor: Long? = null,
    @SerialName("activeBackgroundColor")
    val artistActiveBackgroundColor: Long? = null,
    @SerialName("loadingIndicatorColor")
    val artistLoadingIndicatorColor: Int? = null,
    @SerialName("playingIcon")
    val artistPlayingIcon: ArtistPlayingIcon? = null,
    @SerialName("iconLoadingColor")
    val artistIconLoadingColor: Int? = null,
    @SerialName("activeScaleFactor")
    val artistActiveScaleFactor: Double? = null,
    @SerialName("buttonSize")
    val artistButtonSize: String? = null,
    @SerialName("rippleTarget")
    val artistRippleTarget: String? = null,
    @SerialName("accessibilityPlayData")
    val artistAccessibilityPlayData: ArtistAccessibilityPlayData? = null,
    @SerialName("accessibilityPauseData")
    val artistAccessibilityPauseData: ArtistAccessibilityPauseData? = null
)

@Serializable
data class ArtistPlayNavigationEndpointX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchPlaylistEndpoint")
    val artistWatchPlaylistEndpoint: ArtistWatchPlaylistEndpoint? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpointXXXXXXX? = null
)

@Serializable
data class ArtistWatchEndpointXXXXXXX(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("index")
    val artistIndex: Int? = null,
    @SerialName("loggingContext")
    val artistLoggingContext: ArtistLoggingContext? = null,
    @SerialName("watchEndpointMusicSupportedConfigs")
    val artistWatchEndpointMusicSupportedConfigs: ArtistWatchEndpointMusicSupportedConfigs? = null,
    @SerialName("playerParams")
    val artistPlayerParams: String? = null
)

@Serializable
data class ArtistHeaderX(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistSubheader(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistDescription(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistMoreButton(
    @SerialName("toggleButtonRenderer")
    val artistToggleButtonRenderer: ArtistToggleButtonRenderer? = null
)

@Serializable
data class ArtistToggleButtonRenderer(
    @SerialName("isToggled")
    val artistIsToggled: Boolean? = null,
    @SerialName("isDisabled")
    val artistIsDisabled: Boolean? = null,
    @SerialName("defaultIcon")
    val artistDefaultIcon: ArtistDefaultIcon? = null,
    @SerialName("defaultText")
    val artistDefaultText: ArtistDefaultText? = null,
    @SerialName("toggledIcon")
    val artistToggledIcon: ArtistToggledIcon? = null,
    @SerialName("toggledText")
    val artistToggledText: ArtistToggledText? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistMusicImmersiveHeaderRenderer(
    @SerialName("title")
    val artistTitle: ArtistTitleX? = null,
    @SerialName("subscriptionButton")
    val artistSubscriptionButton: ArtistSubscriptionButton? = null,
    @SerialName("description")
    val artistDescription: ArtistDescription? = null,
    @SerialName("moreButton")
    val artistMoreButton: ArtistMoreButton? = null,
    @SerialName("menu")
    val artistMenu: ArtistMenuXX? = null,
    @SerialName("thumbnail")
    val artistThumbnail: ArtistThumbnail? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("playButton")
    val artistPlayButton: ArtistPlayButton? = null,
    @SerialName("startRadioButton")
    val artistStartRadioButton: ArtistStartRadioButton? = null,
    @SerialName("shareEndpoint")
    val artistShareEndpoint: ArtistShareEndpoint? = null,
    @SerialName("monthlyListenerCount")
    val artistMonthlyListenerCount: ArtistMonthlyListenerCount? = null
)

@Serializable
data class ArtistSubscriptionButton(
    @SerialName("subscribeButtonRenderer")
    val artistSubscribeButtonRenderer: ArtistSubscribeButtonRenderer? = null
)

@Serializable
data class ArtistMenuXX(
    @SerialName("menuRenderer")
    val artistMenuRenderer: ArtistMenuRendererXX? = null
)

@Serializable
data class ArtistPlayButton(
    @SerialName("buttonRenderer")
    val artistButtonRenderer: ArtistButtonRendererXXXXXXXXX? = null
)

@Serializable
data class ArtistStartRadioButton(
    @SerialName("buttonRenderer")
    val artistButtonRenderer: ArtistButtonRendererXXXXXXXXXX? = null
)

@Serializable
data class ArtistShareEndpoint(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("shareEntityEndpoint")
    val artistShareEntityEndpoint: ArtistShareEntityEndpoint? = null
)

@Serializable
data class ArtistMonthlyListenerCount(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibility? = null
)

@Serializable
data class ArtistSubscribeButtonRenderer(
    @SerialName("subscriberCountText")
    val artistSubscriberCountText: ArtistSubscriberCountText? = null,
    @SerialName("subscribed")
    val artistSubscribed: Boolean? = null,
    @SerialName("enabled")
    val artistEnabled: Boolean? = null,
    @SerialName("type")
    val artistType: String? = null,
    @SerialName("channelId")
    val artistChannelId: String? = null,
    @SerialName("showPreferences")
    val artistShowPreferences: Boolean? = null,
    @SerialName("subscriberCountWithSubscribeText")
    val artistSubscriberCountWithSubscribeText: ArtistSubscriberCountWithSubscribeText? = null,
    @SerialName("subscribedButtonText")
    val artistSubscribedButtonText: ArtistSubscribedButtonText? = null,
    @SerialName("unsubscribedButtonText")
    val artistUnsubscribedButtonText: ArtistUnsubscribedButtonText? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("unsubscribeButtonText")
    val artistUnsubscribeButtonText: ArtistUnsubscribeButtonText? = null,
    @SerialName("serviceEndpoints")
    val artistServiceEndpoints: List<ArtistServiceEndpointXXXX>? = null,
    @SerialName("longSubscriberCountText")
    val artistLongSubscriberCountText: ArtistLongSubscriberCountText? = null,
    @SerialName("shortSubscriberCountText")
    val artistShortSubscriberCountText: ArtistShortSubscriberCountText? = null,
    @SerialName("subscribeAccessibility")
    val artistSubscribeAccessibility: ArtistSubscribeAccessibility? = null,
    @SerialName("unsubscribeAccessibility")
    val artistUnsubscribeAccessibility: ArtistUnsubscribeAccessibility? = null,
    @SerialName("signInEndpoint")
    val artistSignInEndpoint: ArtistSignInEndpointXXXXX? = null
)

@Serializable
data class ArtistSubscriberCountText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistSubscriberCountWithSubscribeText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistSubscribedButtonText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistUnsubscribedButtonText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistUnsubscribeButtonText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistServiceEndpointXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("subscribeEndpoint")
    val artistSubscribeEndpoint: ArtistSubscribeEndpoint? = null,
    @SerialName("signalServiceEndpoint")
    val artistSignalServiceEndpoint: ArtistSignalServiceEndpoint? = null
)

@Serializable
data class ArtistLongSubscriberCountText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibility? = null
)

@Serializable
data class ArtistShortSubscriberCountText(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistSubscribeAccessibility(
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityData? = null
)

@Serializable
data class ArtistUnsubscribeAccessibility(
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityData? = null
)

@Serializable
data class ArtistSignInEndpointXXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("modalEndpoint")
    val artistModalEndpoint: ArtistModalEndpoint? = null
)

@Serializable
data class ArtistSubscribeEndpoint(
    @SerialName("channelIds")
    val artistChannelIds: List<String?>? = null,
    @SerialName("params")
    val artistParams: String? = null
)

@Serializable
data class ArtistSignalServiceEndpoint(
    @SerialName("signal")
    val artistSignal: String? = null,
    @SerialName("actions")
    val artistActions: List<ArtistAction>? = null
)

@Serializable
data class ArtistAction(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("openPopupAction")
    val artistOpenPopupAction: ArtistOpenPopupAction? = null
)

@Serializable
data class ArtistOpenPopupAction(
    @SerialName("popup")
    val artistPopup: ArtistPopup? = null,
    @SerialName("popupType")
    val artistPopupType: String? = null
)

@Serializable
data class ArtistPopup(
    @SerialName("confirmDialogRenderer")
    val artistConfirmDialogRenderer: ArtistConfirmDialogRenderer? = null
)

@Serializable
data class ArtistConfirmDialogRenderer(
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("dialogMessages")
    val artistDialogMessages: List<ArtistDialogMessage>? = null,
    @SerialName("confirmButton")
    val artistConfirmButton: ArtistConfirmButton? = null,
    @SerialName("cancelButton")
    val artistCancelButton: ArtistCancelButton? = null
)

@Serializable
data class ArtistDialogMessage(
    @SerialName("runs")
    val artistRuns: List<ArtistRunXX>? = null
)

@Serializable
data class ArtistConfirmButton(
    @SerialName("buttonRenderer")
    val artistButtonRenderer: ArtistButtonRendererXXXXXX? = null
)

@Serializable
data class ArtistCancelButton(
    @SerialName("buttonRenderer")
    val artistButtonRenderer: ArtistButtonRendererXXXXXXX? = null
)

@Serializable
data class ArtistButtonRendererXXXXXX(
    @SerialName("style")
    val artistStyle: String? = null,
    @SerialName("size")
    val artistSize: String? = null,
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("serviceEndpoint")
    val artistServiceEndpoint: ArtistServiceEndpointXXXXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistServiceEndpointXXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("unsubscribeEndpoint")
    val artistUnsubscribeEndpoint: ArtistUnsubscribeEndpoint? = null
)

@Serializable
data class ArtistUnsubscribeEndpoint(
    @SerialName("channelIds")
    val artistChannelIds: List<String?>? = null
)

@Serializable
data class ArtistButtonRendererXXXXXXX(
    @SerialName("style")
    val artistStyle: String? = null,
    @SerialName("size")
    val artistSize: String? = null,
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistMenuRendererXX(
    @SerialName("items")
    val artistItems: List<ArtistItemXXXX>? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibility? = null
)

@Serializable
data class ArtistItemXXXX(
    @SerialName("menuNavigationItemRenderer")
    val artistMenuNavigationItemRenderer: ArtistMenuNavigationItemRendererXX? = null
)

@Serializable
data class ArtistMenuNavigationItemRendererXX(
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXXXXXXXXXXXXXXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null
)

@Serializable
data class ArtistNavigationEndpointXXXXXXXXXXXXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("shareEntityEndpoint")
    val artistShareEntityEndpoint: ArtistShareEntityEndpoint? = null
)

@Serializable
data class ArtistButtonRendererXXXXXXXXX(
    @SerialName("style")
    val artistStyle: String? = null,
    @SerialName("size")
    val artistSize: String? = null,
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXXXXXXXXXXXXXXXX? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibilityXXXXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityDataXXX? = null
)

@Serializable
data class ArtistNavigationEndpointXXXXXXXXXXXXXXXX(
    @SerialName("clickTrackingParams")
    val artistClickTrackingParams: String? = null,
    @SerialName("watchEndpoint")
    val artistWatchEndpoint: ArtistWatchEndpointXXXXXXXX? = null
)

@Serializable
data class ArtistAccessibilityXXXXX(
    @SerialName("label")
    val artistLabel: String? = null
)

@Serializable
data class ArtistWatchEndpointXXXXXXXX(
    @SerialName("videoId")
    val artistVideoId: String? = null,
    @SerialName("playlistId")
    val artistPlaylistId: String? = null,
    @SerialName("params")
    val artistParams: String? = null,
    @SerialName("loggingContext")
    val artistLoggingContext: ArtistLoggingContext? = null
)

@Serializable
data class ArtistButtonRendererXXXXXXXXXX(
    @SerialName("text")
    val artistText: ArtistTextX? = null,
    @SerialName("icon")
    val artistIcon: ArtistIcon? = null,
    @SerialName("navigationEndpoint")
    val artistNavigationEndpoint: ArtistNavigationEndpointXXXXXXXXXXXXXXXX? = null,
    @SerialName("accessibility")
    val artistAccessibility: ArtistAccessibilityXXXXX? = null,
    @SerialName("trackingParams")
    val artistTrackingParams: String? = null,
    @SerialName("accessibilityData")
    val artistAccessibilityData: ArtistAccessibilityDataXXX? = null
)

@Serializable
data class ArtistMicroformatDataRenderer(
    @SerialName("urlCanonical")
    val artistUrlCanonical: String? = null,
    @SerialName("title")
    val artistTitle: String? = null,
    @SerialName("description")
    val artistDescription: String? = null,
    @SerialName("thumbnail")
    val artistThumbnail: ArtistThumbnailX? = null,
    @SerialName("siteName")
    val artistSiteName: String? = null,
    @SerialName("appName")
    val artistAppName: String? = null,
    @SerialName("androidPackage")
    val artistAndroidPackage: String? = null,
    @SerialName("iosAppStoreId")
    val artistIosAppStoreId: String? = null,
    @SerialName("ogType")
    val artistOgType: String? = null,
    @SerialName("urlApplinksWeb")
    val artistUrlApplinksWeb: String? = null,
    @SerialName("urlApplinksIos")
    val artistUrlApplinksIos: String? = null,
    @SerialName("urlApplinksAndroid")
    val artistUrlApplinksAndroid: String? = null,
    @SerialName("urlTwitterIos")
    val artistUrlTwitterIos: String? = null,
    @SerialName("urlTwitterAndroid")
    val artistUrlTwitterAndroid: String? = null,
    @SerialName("twitterCardType")
    val artistTwitterCardType: String? = null,
    @SerialName("twitterSiteHandle")
    val artistTwitterSiteHandle: String? = null
)
