from innertube import InnerTube
import re

PARAMS_TYPE_VIDEO =     "EgIQAQ%3D%3D"
PARAMS_TYPE_CHANNEL =   "EgIQAg%3D%3D"
PARAMS_TYPE_PLAYLIST =  "EgIQAw%3D%3D"
PARAMS_TYPE_FILM =      "EgIQBA%3D%3D"
PARAMS_TYPE_SONG =      "EgWKAQIIAWoQEAMQBBAJEAoQBRAREBAQFQ%3D%3D"

client = InnerTube("WEB_REMIX", "1.20250409.01.00")
def searchSongs(query):
    data = client.search(query=query, params=PARAMS_TYPE_SONG)

    n_data = (data.get('contents')
              .get('tabbedSearchResultsRenderer')
              .get('tabs')[0]
              .get('tabRenderer')  
              .get('content')
              .get('sectionListRenderer')
              .get('contents')[1]  
              .get('musicShelfRenderer')
              .get('contents')
              )
    #print(n_data)
    video_ids = []
    for i in n_data:
        video_id = i.get('musicResponsiveListItemRenderer').get('overlay').get('musicItemThumbnailOverlayRenderer').get('content').get('musicPlayButtonRenderer').get('playNavigationEndpoint').get('watchEndpoint').get('videoId')
        #print("videoId: ", video_id)
        video_ids.append(video_id)

    return video_ids
#searchSongs("Numb")

def searchOneSong(songName):

    return searchSongs(songName)[0]

print(searchOneSong("Numb"))

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
    print(n_data.get('longBylineText').get('runs')[0])
    print(artist)
    return artist


getSongArtistName("5qZQEq_C3vc")
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
    print("thumbnail URL for songID: ", video_id, " ", thumbnail)

    return thumbnail