import re
from innertube import InnerTube
from pprint import pprint
from time import perf_counter

PARAMS_TYPE_VIDEO = "EgIQAQ%3D%3D"
PARAMS_TYPE_CHANNEL = "EgIQAg%3D%3D"
PARAMS_TYPE_PLAYLIST = "EgIQAw%3D%3D"
PARAMS_TYPE_FILM = "EgIQBA%3D%3D"
PARAMS_TYPE_SONG = "EgWKAQIIAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D"

print("INNERTUBE HAS BEEN INITIALIZED")

client = InnerTube("WEB_REMIX", "1.20250409.01.00")
# client = InnerTube("ANDROID")


def searchSongs(query):
    try:
        data = InnerTube("WEB_REMIX", "1.20250409.01.00").search(query=query, params=PARAMS_TYPE_SONG)

        contents = data.get("contents", {}).get("tabbedSearchResultsRenderer", {}) \
            .get("tabs", [])[0].get("tabRenderer", {}).get("content", {}) \
            .get("sectionListRenderer", {}).get("contents", [])

        # Find the dict that contains 'musicShelfRenderer'
        music_shelf = None
        for section in contents:
            if 'musicShelfRenderer' in section:
                music_shelf = section['musicShelfRenderer']
                break

        if music_shelf is None:
            print("musicShelfRenderer not found!")
            return []  # or handle however you like

        n_data = music_shelf.get("contents", [])

        results = []
        for i in n_data:
            renderer = i.get("musicResponsiveListItemRenderer", {})

            video_id = renderer.get("playlistItemData", {}).get("videoId", "UnknownID")

            flexColumns = renderer.get("flexColumns", [])
            video_title = flexColumns[0].get("musicResponsiveListItemFlexColumnRenderer", {}) \
                .get("text", {}).get("runs", [{}])[0].get("text", "Unknown Title")

            artist_data = flexColumns[1].get("musicResponsiveListItemFlexColumnRenderer", {}) \
                .get("text", {}).get("runs", [{}])
            video_artist = artist_data[0].get("text", "Unknown Artist")

            # Duration — be careful, some entries may have fewer runs
            try:
                video_duration = artist_data[4].get("text", "Unknown Duration")
            except IndexError:
                video_duration = "Unknown Duration"

            video_thumbnail = f"https://img.youtube.com/vi/{video_id}/maxresdefault.jpg"

            results.append({
                "id": video_id,
                "title": video_title,
                "artist": video_artist,
                "thumbnail": video_thumbnail,
                "duration": video_duration,
            })

        return results

    except Exception as e:
        print(f"Error parsing song data: {e}")
        return []  # Return empty list if things fail


#def getRadio(video_id):
    client = InnerTube("WEB_REMIX")
    data = client.music_get_queue(video_ids=video_id)
    #data = client.next(video_id=video_id)
    #data = data = client("search", body={"browseId": video_id})
    #data = client.music_get_queue(video_ids=video_id)
    #n_data = client("browse", body={"browseId": video_id})
    n_data = data.get('queueDatas')[-1].get('content').get('playlistPanelVideoRenderer').get('menu').get('menuRenderer').get('items')[0]
    
    #n_data = data  

    
    #n_data = data
    
    pprint(n_data)
    print(n_data.keys())

#getRadio("A__cH65WRvE")
#print(pkg_resources.get_distribution("InnerTube").version)

def getRadio(video_id):
    client = InnerTube("IOS_MUSIC")
    data = client.next(video_id=video_id, playlist_id="RDAMVMA__cH65WRvE", params=PARAMS_TYPE_SONG)
    playlist = (data.get('contents').get('singleColumnMusicWatchNextResultsRenderer').get('playlist').get('playlistPanelRenderer').get('contents'))
    
    
    results = []
    
    for i in playlist:
        t = i.get('playlistPanelVideoRenderer')
        #print(t.keys())
        #print(i.get('playlistPanelVideoRenderer').get('videoId'))
        
        video_id = i.get('playlistPanelVideoRenderer').get('videoId')
        video_title = i.get('playlistPanelVideoRenderer').get('title').get('runs')[0].get('text')
        video_artist = i.get('playlistPanelVideoRenderer').get('longBylineText').get('runs')[0].get('text')
        video_thumbnail = f"https://img.youtube.com/vi/{video_id}/maxresdefault.jpg"
        #video_duration = i.get('playlistPanelVideoRenderer').get('lengthText').get('runs')[0].get('text')
        
        try:
            video_duration = i.get('playlistPanelVideoRenderer').get('lengthText').get('runs')[0].get('text')
        except IndexError:
            video_duration = "NA"
        
        #print(video_artist)
        
        results.append({
                "id": video_id,
                "title": video_title,
                "artist": video_artist,
                "thumbnail": video_thumbnail,
                "duration": video_duration,
            })
    
    #pprint(n_data)
    #print(n_data.keys())

    #pprint(results)
    return results
    
    
pprint(getRadio("A__cH65WRvE"))

def searchPlaylist(query):
    data = client.search(query=query)
    
    n_data= data
    
    pprint(n_data)
    print(n_data.keys())

#searchPlaylist("RDAMVMA__cH65WRvE")

# not in use
def searchOneSong(songName):
    return searchSongs(songName)[0]

#not in use
def getSongTitle(video_id):
    data = client.next(video_id=video_id)
    # print("client.next")
    n_data = (
        data.get("contents")
        .get("singleColumnMusicWatchNextResultsRenderer")
        .get("tabbedRenderer")
        .get("watchNextTabbedResultsRenderer")
        .get("tabs")[0]
        .get("tabRenderer")
        .get("content")
        .get("musicQueueRenderer")
        .get("content")
        .get("playlistPanelRenderer")
        .get("contents")[0]
        .get("playlistPanelVideoRenderer")
    )

    title = n_data.get("title").get("runs")[0].get("text")
    # print(title)
    return title

# not in use
def getSongArtistName(video_id):
    data = client.next(video_id=video_id)
    n_data = (
        data.get("contents")
        .get("singleColumnMusicWatchNextResultsRenderer")
        .get("tabbedRenderer")
        .get("watchNextTabbedResultsRenderer")
        .get("tabs")[0]
        .get("tabRenderer")
        .get("content")
        .get("musicQueueRenderer")
        .get("content")
        .get("playlistPanelRenderer")
        .get("contents")[0]
        .get("playlistPanelVideoRenderer")
    )

    artist = n_data.get("longBylineText").get("runs")[0].get("text")
    # print(n_data.get('longBylineText').get('runs')[0])
    # print(artist)
    return artist

# not in use
def getSongThumbnailURL(video_id):
    data = client.next(video_id=video_id)
    # print("client.next")
    n_data = (
        data.get("contents")
        .get("singleColumnMusicWatchNextResultsRenderer")
        .get("tabbedRenderer")
        .get("watchNextTabbedResultsRenderer")
        .get("tabs")[0]
        .get("tabRenderer")
        .get("content")
        .get("musicQueueRenderer")
        .get("content")
        .get("playlistPanelRenderer")
        .get("contents")[0]
        .get("playlistPanelVideoRenderer")
    )
    # print(n_data)

    # -1 gets the last (highest res) thumbnail
    thumbnail = n_data.get("thumbnail").get("thumbnails")[-1].get("url")
    # print("thumbnail URL for songID: ", video_id, " ", thumbnail)

    return thumbnail

# not in use
def getSongDuration(video_id):
    data = client.next(video_id=video_id)
    n_data = (
        data.get("contents")
        .get("singleColumnMusicWatchNextResultsRenderer")
        .get("tabbedRenderer")
        .get("watchNextTabbedResultsRenderer")
        .get("tabs")[0]
        .get("tabRenderer")
        .get("content")
        .get("musicQueueRenderer")
        .get("content")
        .get("playlistPanelRenderer")
        .get("contents")[0]
        .get("playlistPanelVideoRenderer")
    )
    # print(n_data.keys())
    # print(n_data.get('lengthText').get('runs')[0].get('text'))
    duration = n_data.get("lengthText").get("runs")[0].get("text")
    return duration

#not in use
def playSongById(video_id):
    client2 = InnerTube("ANDROID")

    data = client2.player(video_id)
    # streams = data["streamingData"]["adaptiveFormats"]
    # print("printStreamableUrls")
    n_data = data.get("streamingData").get("adaptiveFormats")
    pprint(n_data[-1])
    # print(n_data)
    # pprint(n_data[-1].get('url'))
    pprint(n_data[-1])
    return n_data[-1].get("url")

#not in use
# Pretty much useless now because it is not in use anymore
def searchSongsWithDetails(query):
    print("request reached python and is sent to api")
    t0 = perf_counter()
    list_of_song_ids = searchSongs(query)
    t1 = perf_counter()
    t10 = t1 - t0
    print(f"Elapsed (high‑res): {t10:.6f} seconds")
    print("request came back from api and is now processing")
    results = []
    for i in list_of_song_ids:
        print("request going to api for song id: ", i)
        t = client.next(video_id=i)
        client.search
        print("request came back from api for song id: ", i)
        data = t["contents"]["singleColumnMusicWatchNextResultsRenderer"][
            "tabbedRenderer"
        ]["watchNextTabbedResultsRenderer"]["tabs"][0]["tabRenderer"]["content"][
            "musicQueueRenderer"
        ][
            "content"
        ][
            "playlistPanelRenderer"
        ][
            "contents"
        ][
            0
        ][
            "playlistPanelVideoRenderer"
        ]

        # Add as a dictionary
        results.append(
            {
                "id": i,
                "title": data["title"]["runs"][0]["text"],
                "artist": data["longBylineText"]["runs"][0]["text"],
                "thumbnail": data["thumbnail"]["thumbnails"][-1]["url"],
                "duration": data["lengthText"]["runs"][0]["text"],
            }
        )
    t2 = perf_counter()
    t12 = t2 - t10
    print(f"Elapsed (high‑res): {t12:.6f} seconds")
    print("Processing done")
    return results

# not in use
def testing(search_query):
    one_song = searchOneSong(search_query)
    many_songs = searchSongs(search_query)
    thumbnail = getSongThumbnailURL(one_song)
    title = getSongTitle(one_song)
    artist = getSongArtistName(one_song)
    duration = getSongDuration(one_song)
    # player = playSongById(one_song)

    print("Multiple songs: ", many_songs)
    print("One Song Id: ", one_song)
    print("One Song Title: ", title)
    print("One Song Artist: ", artist)
    print("One Song Duration: ", duration)
    print("One Song URL: ", thumbnail)
    # print(player)

# not in use
def playSongByIdWithBestQuality(video_id):
    client2 = InnerTube("WEB_REMIX")

    data = client2.player(video_id)
    # streams = data["streamingData"]["adaptiveFormats"]
    # print("printStreamableUrls")

    n_data = data.get("streamingData").get("adaptiveFormats")
    pprint(n_data)
    for i in n_data:
        if i.get("audioQuality") == "AUDIO_QUALITY_HIGH":
            print("Found AUDIO_QUALITY_HIGH stream")
            return i.get("url")
        elif i.get("audioQuality") == "AUDIO_QUALITY_MEDIUM":
            print("Found AUDIO_QUALITY_MEDIUM stream")
            return i.get("url")
        else:
            print(
                "No AUDIO_QUALITY_MEDIUM or AUDIO_QUALITY_HIGH stream found, returning last available stream"
            )
    return n_data[-1].get("url")


def playSongByIdWithBestBitrate(video_id):
    client2 = InnerTube("ANDROID")

    data = client2.player(video_id)
    # streams = data["streamingData"]["adaptiveFormats"]
    # print("printStreamableUrls")
    hBitrate = 0
    currentHighestBitrateUrl = ""
    n_data = data.get("streamingData").get("adaptiveFormats")
    pprint(n_data)
    for i in n_data:
        if (
            i.get("audioQuality") == "AUDIO_QUALITY_HIGH"
            or i.get("audioQuality") == "AUDIO_QUALITY_MEDIUM"
        ):
            # Check if averageBitrate exists and is not None
            bitrate = i.get("averageBitrate")
            # bitrate = i.get('bitrate')
            url = i.get("url")
            print("Bitrate: ", bitrate, " URL: ", url)

            # Only consider streams that have both bitrate AND url
            if bitrate is not None and url is not None and bitrate > hBitrate:
                # pprint(i)
                hBitrate = bitrate
                currentHighestBitrateUrl = url

    print("Highest bitrate found: ", hBitrate)
    print("Highest bitrate URL: ", currentHighestBitrateUrl)
    return currentHighestBitrateUrl

# not in use
def tester(video_id):
    client2 = InnerTube("ANDROID")
    data = client2.player(video_id)
    n_data = (data).get("streamingData").get("adaptiveFormats")

    pprint(n_data)
    # pprint(n_data.keys())


#playSongByIdWithBestBitrate("A__cH65WRvE")
# tester("A__cH65WRvE")
