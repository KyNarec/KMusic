package com.kynarec.kmusic.service.innertube.responses

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
    val musicShelfRenderer: MusicShelfRenderer? = null,
    val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer?= null
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
    val thumbnail: ThumbnailContainer? = null,
    val navigationEndpoint: NavigationEndpoint? = null
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
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null,
    val browseId: String?
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
    val adaptiveFormats: List<AdaptiveFormat>? = null,
    val formats: List<Format> = emptyList(),
)

@Serializable
data class Format(
    val itag: Int,
    val url: String? = null,
    val mimeType: String? = null,
    val bitrate: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val lastModified: String? = null,
    val quality: String? = null,
    val fps: Int? = null,
    val qualityLabel: String? = null,
    val projectionType: String? = null,
    val audioQuality: String? = null,
    val approxDurationMs: String? = null,
    val audioSampleRate: String? = null,
    val audioChannels: Int? = null,

    // Fields specific to Adaptive formats
    val initRange: ByteRange? = null,
    val indexRange: ByteRange? = null,
    val contentLength: String? = null,
    val averageBitrate: Int? = null,

    // Signature Cipher (for formats requiring de-scrambling)
    val signatureCipher: String? = null
)

@Serializable
data class ByteRange(
    val start: String,
    val end: String
)

@Serializable
data class AdaptiveFormat(
    val averageBitrate: Int? = null,
    val url: String? = null,
    val audioQuality: String? = null,
    val itag: Int? = null
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
    val text: String? = null,
    val navigationEndpoint: AlbumAndSongsNavigationEndpoint? = null
)

// ========================================== getAlbumAndSongs

@Serializable
data class AlbumBrowseResponse(
    val contents: SearchAlbumContents? = null,
    val microformat: Microformat? = null
)

@Serializable
data class SearchAlbumContents(
    val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer? = null
)

@Serializable
data class TwoColumnBrowseResultsRenderer(
    val secondaryContents: SecondaryContents? = null,
    val tabs: List<Tab>
)

@Serializable
data class SecondaryContents(
    val sectionListRenderer: SectionListRendererAlbum? = null
)

@Serializable
data class SectionListRendererAlbum(
    val contents: List<SectionContentAlbum>? = null
)
@Serializable
data class SectionContentAlbum(
    val musicShelfRenderer: MusicShelfRendererAlbum? = null,
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer? = null
)

@Serializable
data class MusicShelfRendererAlbum(
    val contents: List<MusicItemAlbum>? = null
)

@Serializable
data class MusicItemAlbum(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRendererAlbum? = null
)

@Serializable
data class MusicResponsiveListItemRendererAlbum(
    val playlistItemData: PlaylistItemData? = null,
    val index: AlbumIndex? = null,
    val overlay: AlbumAndSongsOverlay? = null,
    val flexColumns: List<AlbumAndSongsFlexColumns>? = null,
    val fixedColumns: List<AlbumAndSongsFixedColumns>? = null,
    val menu: Menu? = null
)

@Serializable
data class Menu(
    val menuRenderer: MenuRenderer? = null
)

@Serializable
data class MenuRenderer(
    val items: List<MenuItem>? = null
)

@Serializable
data class MenuItem(
    val menuNavigationItemRenderer: MenuNavigationItemRenderer? = null
)

@Serializable
data class MenuNavigationItemRenderer(
    val text: TextRuns? = null,
    val navigationEndpoint: AlbumAndSongsNavigationEndpoint? = null
)
@Serializable
data class AlbumAndSongsFlexColumns(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null
)

@Serializable
data class AlbumAndSongsNavigationEndpoint(
    val watchEndpoint: AlbumAndSongsWatchEndpoint? = null,
    val browseEndpoint: BrowseEndpoint? = null
)

@Serializable
data class AlbumAndSongsWatchEndpoint(
    val videoId: String? = null
)

@Serializable
data class AlbumAndSongsFixedColumns(
    val musicResponsiveListItemFixedColumnRenderer: AlbumAndSongsMusicResponsiveListItemFixedColumnRenderer
)

@Serializable
data class AlbumAndSongsMusicResponsiveListItemFixedColumnRenderer(
    val text: TextRuns?= null
)

@Serializable
data class AlbumAndSongsOverlay(
    val musicItemThumbnailOverlayRenderer: AlbumAndSongsMusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class AlbumAndSongsMusicItemThumbnailOverlayRenderer(
    val content: AlbumAndSongsContent? = null
)

@Serializable
data class AlbumAndSongsContent(
    val musicPlayButtonRenderer: AlbumAndSongsMusicPlayButtonRenderer? = null
)

@Serializable
data class AlbumAndSongsMusicPlayButtonRenderer(
    val accessibilityPlayData: AlbumAndSongsAccessibilityPlayData? = null
)

@Serializable
data class AlbumAndSongsAccessibilityPlayData(
    val accessibilityData: AlbumAndSongsAccessibilityData? = null
)

@Serializable
data class AlbumAndSongsAccessibilityData(
    val label: String? = null
)

@Serializable
data class PlaylistItemData(
    val videoId: String? = null,
    val playlistSetVideoId: String? = null
)

@Serializable
data class AlbumIndex(
    val runs: List<Run>? = null
)

@Serializable
data class MusicCarouselShelfRenderer(
    val contents: List<Item>? = null
)

@Serializable
data class Item(
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer? = null
)

@Serializable
data class MusicTwoRowItemRenderer(
    val thumbnailRenderer: ThumbnailRendererAlbum? = null,
)

@Serializable
data class ThumbnailRendererAlbum(
    val musicThumbnailRenderer: MusicThumbnailRendererAlbum? = null
)

@Serializable
data class MusicThumbnailRendererAlbum(
    val thumbnail: ThumbnailAlbum? = null
)
@Serializable
data class ThumbnailAlbum(
    val thumbnails: List<ThumbnailBetter> = emptyList()
)

@Serializable
data class MusicResponsiveHeaderRenderer(
    val thumbnail: ThumbnailRendererAlbum? = null,
    val title: TextRuns? = null,
    val subtitle: TextRuns? = null,
    val description: Description? = null
)

@Serializable
data class Description(
    val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null
)

@Serializable
data class MusicDescriptionShelfRenderer(
    val description: TextRuns? = null
)

@Serializable
data class Microformat(
    val microformatDataRenderer: MicroformatDataRenderer? = null
)

@Serializable
data class MicroformatDataRenderer(
    val urlCanonical: String? = null,
    val thumbnail: ThumbnailAlbum? = null
)

// ========================================== searchAlbums
// x.contents.tabbedSearchResultsRenderer.tabs[0]
// .tabRenderer.content.sectionListRenderer.contents[1]
// .musicShelfRenderer.contents

@Serializable
data class SearchAlbumsResponse(
    val contents: SearchAlbumsContents? = null
)

@Serializable
data class SearchAlbumsContents(
    val tabbedSearchResultsRenderer: TabbedAlbumsSearchResultsRenderer? = null
)

@Serializable
data class TabbedAlbumsSearchResultsRenderer(
    val tabs: List<SearchAlbumsTabs>? = null
)

@Serializable
data class SearchAlbumsTabs(
    val tabRenderer: SearchAlbumsTabRenderer? = null
)

@Serializable
data class SearchAlbumsTabRenderer(
    val content: SearchAlbumsContent? = null
)

@Serializable
data class SearchAlbumsContent(
    val sectionListRenderer: SearchAlbumsSectionListRenderer? = null
)

@Serializable
data class SearchAlbumsSectionListRenderer(
    val contents: List<SearchAlbumsContents2>? = null
)

@Serializable
data class SearchAlbumsContents2(
    val musicShelfRenderer: SearchAlbumsMusicShelfRenderer? = null
)

@Serializable
data class SearchAlbumsMusicShelfRenderer(
    val contents: List<SearchAlbumsContents3>? = null,
    val continuations: List<SearchAlbumsContinuations>? = null
)

@Serializable
data class SearchAlbumsContents3(
    val musicResponsiveListItemRenderer: SearchAlbumsMusicResponsiveListItemRenderer? = null,
)

@Serializable
data class SearchAlbumsMusicResponsiveListItemRenderer(
    val thumbnail: ThumbnailRendererAlbum? = null,
    val flexColumns: List<SearchAlbumsFlexColumns>? = null,
    val navigationEndpoint: SearchAlbumsNavigationEndpoint? = null
)

@Serializable
data class SearchAlbumsFlexColumns(
    val musicResponsiveListItemFlexColumnRenderer: SearchAlbumsText? = null
)

@Serializable
data class SearchAlbumsText(
    val text: TextRuns? = null
)

@Serializable
data class SearchAlbumsNavigationEndpoint(
    val browseEndpoint: SearchAlbumsBrowseEndpoint? = null
)

@Serializable
data class SearchAlbumsBrowseEndpoint(
    val browseId: String? = null
)

@Serializable
data class SearchAlbumsContinuations(
    val nextContinuationData: SearchAlbumsNextContinuationData
)

@Serializable
data class SearchAlbumsNextContinuationData(
    val continuation: String? = null
)

// ========================================== searchArtists
@Serializable
data class SearchArtistsResponse(
    val contents: SearchArtistsContents? = null
)
@Serializable
data class SearchArtistsContents(
    val tabbedSearchResultsRenderer: TabbedArtistsSearchResultsRenderer? = null
)

@Serializable
data class TabbedArtistsSearchResultsRenderer(
    val tabs: List<SearchArtistsTabs>? = null
)

@Serializable
data class SearchArtistsTabs(
    val tabRenderer: SearchArtistsTabRenderer? = null
)

@Serializable
data class SearchArtistsTabRenderer(
    val content: SearchArtistsContent? = null
)

@Serializable
data class SearchArtistsContent(
    val sectionListRenderer: SearchArtistsSectionListRenderer? = null
)

@Serializable
data class SearchArtistsSectionListRenderer(
    val contents: List<SearchArtistsContents2>? = null
)

@Serializable
data class SearchArtistsContents2(
    val musicShelfRenderer: SearchArtistsMusicShelfRenderer? = null
)

@Serializable
data class SearchArtistsMusicShelfRenderer(
    val contents: List<SearchArtistsContents3>? = null,
)

@Serializable
data class SearchArtistsContents3(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer ? = null,
)

@Serializable
data class SearchArtistsFlexColumns(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null
)