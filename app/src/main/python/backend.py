import re
from innertube import InnerTube
from pprint import pprint
from time import perf_counter

PARAMS_TYPE_VIDEO =     "EgIQAQ%3D%3D"
PARAMS_TYPE_CHANNEL =   "EgIQAg%3D%3D"
PARAMS_TYPE_PLAYLIST =  "EgIQAw%3D%3D"
PARAMS_TYPE_FILM =      "EgIQBA%3D%3D"
PARAMS_TYPE_SONG =      "EgWKAQIIAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D"

print("INNERTUBE HAS BEEN INITIALIZED")

client = InnerTube("WEB_REMIX", "1.20250409.01.00")

def searchSongs(query):
    data = client.search(query=query, params=PARAMS_TYPE_SONG)

    n_data = (data['contents']
              ['tabbedSearchResultsRenderer']
              ['tabs'][0]
              ['tabRenderer'] 
              ['content']
              ['sectionListRenderer']
              ['contents'][1]  
              ['musicShelfRenderer']
              ['contents']
              )
    
    results = []
    for i in n_data:
        video_id = i['musicResponsiveListItemRenderer']['playlistItemData']['videoId']
        # OLD
        # video_id = i['musicResponsiveListItemRenderer']['overlay']['musicItemThumbnailOverlayRenderer']['content']['musicPlayButtonRenderer']['playNavigationEndpoint']['watchEndpoint']['videoId']
        
        #print("videoId: ", video_id)
        #print(i['musicResponsiveListItemRenderer']['overlay']['musicItemThumbnailOverlayRenderer']['content']['musicPlayButtonRenderer']['accessibilityPlayData']['accessibilityData']['label'])
        
        # for Song title
        #print(i['musicResponsiveListItemRenderer']['flexColumns'][0]['musicResponsiveListItemFlexColumnRenderer']['text']['runs'][0]['text'])
        video_title = i['musicResponsiveListItemRenderer']['flexColumns'][0]['musicResponsiveListItemFlexColumnRenderer']['text']['runs'][0]['text']
        #video_title = ""
        
        # for Song duration
        #pprint(i['musicResponsiveListItemRenderer']['flexColumns'][1]['musicResponsiveListItemFlexColumnRenderer']['text']['runs'][4]['text'])
        video_duration = i['musicResponsiveListItemRenderer']['flexColumns'][1]['musicResponsiveListItemFlexColumnRenderer']['text']['runs'][4]['text']

        
        # for Song artist
        #pprint(i['musicResponsiveListItemRenderer']['flexColumns'][1]['musicResponsiveListItemFlexColumnRenderer']['text']['runs'][0]['text'])
        video_artist = i['musicResponsiveListItemRenderer']['flexColumns'][1]['musicResponsiveListItemFlexColumnRenderer']['text']['runs'][0]['text']
        
        # for Song thumbnail in 120x120
        #pprint(i['musicResponsiveListItemRenderer']['thumbnail']['musicThumbnailRenderer']['thumbnail']['thumbnails'][-1]['url'])
        video_thumbnail = i['musicResponsiveListItemRenderer']['thumbnail']['musicThumbnailRenderer']['thumbnail']['thumbnails'][-1]['url']
        

        # Add as a dictionary
        results.append({
            "id": video_id,
            "title": video_title,
            "artist": video_artist,
            "thumbnail": video_thumbnail,
            "duration": video_duration
        })

    return results


def searchOneSong(songName):
    return searchSongs(songName)[0]

def getSongTitle(video_id):
    data = client.next(video_id = video_id)
    #print("client.next")
    n_data = (data.get('contents')
              .get('singleColumnMusicWatchNextResultsRenderer')
              .get('tabbedRenderer')
              .get('watchNextTabbedResultsRenderer')
              .get('tabs')[0]
              .get('tabRenderer')
              .get('content')
              .get('musicQueueRenderer')
              .get('content')
              .get('playlistPanelRenderer')
              .get('contents')[0]
              .get('playlistPanelVideoRenderer')
              )
    
    title = n_data.get('title').get('runs')[0].get('text')
    #print(title)
    return title


def getSongArtistName(video_id):
    data = client.next(video_id = video_id)
    n_data = (data.get('contents')
              .get('singleColumnMusicWatchNextResultsRenderer')
              .get('tabbedRenderer')
              .get('watchNextTabbedResultsRenderer')
              .get('tabs')[0]
              .get('tabRenderer')
              .get('content')
              .get('musicQueueRenderer')
              .get('content')
              .get('playlistPanelRenderer')
              .get('contents')[0]
              .get('playlistPanelVideoRenderer')
              )
    
    artist = n_data.get('longBylineText').get('runs')[0].get('text')
    #print(n_data.get('longBylineText').get('runs')[0])
    #print(artist)
    return artist


def getSongThumbnailURL(video_id):
    data = client.next(video_id = video_id)
    #print("client.next")
    n_data = (data.get('contents')
              .get('singleColumnMusicWatchNextResultsRenderer')
              .get('tabbedRenderer')
              .get('watchNextTabbedResultsRenderer')
              .get('tabs')[0]
              .get('tabRenderer')
              .get('content')
              .get('musicQueueRenderer')
              .get('content')
              .get('playlistPanelRenderer')
              .get('contents')[0]
              .get('playlistPanelVideoRenderer')
              )
    #print(n_data)

    # -1 gets the last (highest res) thumbnail
    thumbnail = n_data.get('thumbnail').get('thumbnails')[-1].get('url')
    #print("thumbnail URL for songID: ", video_id, " ", thumbnail)

    return thumbnail

def getSongDuration(video_id):
    data = client.next(video_id = video_id)
    n_data = (data.get('contents')
              .get('singleColumnMusicWatchNextResultsRenderer')
              .get('tabbedRenderer')
              .get('watchNextTabbedResultsRenderer')
              .get('tabs')[0]
              .get('tabRenderer')
              .get('content')
              .get('musicQueueRenderer')
              .get('content')
              .get('playlistPanelRenderer')
              .get('contents')[0]
              .get('playlistPanelVideoRenderer')
              )
    #print(n_data.keys())
    #print(n_data.get('lengthText').get('runs')[0].get('text'))
    duration = n_data.get('lengthText').get('runs')[0].get('text')
    return duration

def playSongById(video_id):
    client2 = InnerTube("ANDROID")
    
    data = client2.player(video_id)
    #streams = data["streamingData"]["adaptiveFormats"]
    #print("printStreamableUrls")
    n_data = (data.get('streamingData').get('adaptiveFormats'))
    pprint(n_data[-1])
    #print(n_data)
    #pprint(n_data[-1].get('url'))
    pprint(n_data[-1])
    return n_data[-1].get('url')

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
        t = client.next(video_id = i)
        client.search
        print("request came back from api for song id: ", i)
        data = (t['contents']
              ['singleColumnMusicWatchNextResultsRenderer']
              ['tabbedRenderer']
              ['watchNextTabbedResultsRenderer']
              ['tabs']
              [0]
              ['tabRenderer']
              ['content']
              ['musicQueueRenderer']
              ['content']
              ['playlistPanelRenderer']
              ['contents']
              [0]
              ['playlistPanelVideoRenderer']
              )
        
        # Add as a dictionary
        results.append({
            "id": i,
            "title": data['title']['runs'][0]['text'],
            "artist": data['longBylineText']['runs'][0]['text'],
            "thumbnail": data['thumbnail']['thumbnails'][-1]['url'],
            "duration": data['lengthText']['runs'][0]['text']
        })
    t2 = perf_counter()
    t12 = t2 - t10
    print(f"Elapsed (high‑res): {t12:.6f} seconds")
    print("Processing done")
    return results

def testing(search_query):
    one_song = searchOneSong(search_query)
    many_songs = searchSongs(search_query)
    thumbnail = getSongThumbnailURL(one_song)
    title = getSongTitle(one_song)
    artist = getSongArtistName(one_song)
    duration = getSongDuration(one_song)
    #player = playSongById(one_song)
    

    print("Multiple songs: ", many_songs)
    print("One Song Id: ", one_song)
    print("One Song Title: ", title)
    print("One Song Artist: ", artist)
    print("One Song Duration: ", duration)
    print("One Song URL: ", thumbnail)
    #print(player)


