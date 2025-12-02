package com.kynarec.kmusic.service.innertube

enum class ClientName(val label: String, val version: String, val userAgent: String) {
    Web("WEB", "2.20250626.01.00",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    ),
    MWeb("MWEB", "2.20211214.00.00",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    Android("ANDROID", "19.17.34",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    IOS("IOS", "19.16.3",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/98.2  Mobile/15E148 Safari/605.1.15"
    ),
    TvHtml5("TVHTML5", "7.20210224.00.00",
        "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
    ),
    TvLite("TVLITE", "2",
        "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
    ),
    TvAndroid("TVANDROID", "1.0",
        "Mozilla/5.0 (Linux; Android 5.1.1; AFTT Build/LVY48F; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.10"
    ),
    XBoxOneGuide("XBOXONEGUIDE", "1.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; Xbox; Xbox One) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10553"
    ),
    AndroidCreator("ANDROID_CREATOR", "21.06.103",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    IOSCreator("IOS_CREATOR", "20.47.100",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/98.2  Mobile/15E148 Safari/605.1.15"
    ),
    TvApple("TVAPPLE", "1.0",
        "AppleCoreMedia/1.0.0.12B466 (Apple TV; U; CPU OS 8_1_3 like Mac OS X; en_us)"
    ),
    AndroidKids("ANDROID_KIDS", "7.12.3",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    IOSKids("IOS_KIDS", "5.42.2",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/98.2  Mobile/15E148 Safari/605.1.15"
    ),
    AndroidMusic("ANDROID_MUSIC", "5.01",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    IOSMusic("IOS_MUSIC", "4.16.1",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 15_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/98.2  Mobile/15E148 Safari/605.1.15"
    ),
    WebRemix("WEB_REMIX", "1.20230724.00.00",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    ),
    WebMusic("WEB_MUSIC", "1.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    ),
    WebCreator("WEB_CREATOR", "1.20210223.01.00",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    ),
    WebKids("WEB_KIDS", "2.20220414.00.00",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    ),
    TvHtml5Cast("TVHTML5_CAST", "1.1",
        "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
    ),
    TvHtml5Audio("TVHTML5_AUDIO", "2.0",
        "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
    ),
    TvUnpluggedAndroid("TV_UNPLUGGED_ANDROID", "1.22.062.06.90",
        "Mozilla/5.0 (Linux; Android 5.1.1; AFTT Build/LVY48F; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.10"
    ),
    AndroidEmbeddedPlayer("ANDROID_EMBEDDED_PLAYER", "17.13.3",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    WebEmbeddedPlayer("WEB_EMBEDDED_PLAYER", "1.20220413.01.00",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    ),
    GoogleAssistant("GOOGLE_ASSISTANT", "0.1",
        "Mozilla/5.0 (Linux; Android 11; Pixel 2; DuplexWeb-Google/1.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.193 Mobile Safari/537.36"
    ),
    WebUnplugged("WEB_UNPLUGGED", "1.20220403",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    ),
    AndroidLite("ANDROID_LITE", "3.26.1",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    AndroidVr("ANDROID_VR", "1.28.63",
        "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Mobile Safari/537.36"
    ),
    AndroidTv("ANDROID_TV", "2.16.032",
        "Mozilla/5.0 (Linux; Android 5.1.1; AFTT Build/LVY48F; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/49.0.2623.10"
    ),
    TvHtml5Kids("TVHTML5_KIDS", "3.20220325",
        "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
    ),
    TvHtml5Unplugged("TVHTML5_UNPLUGGED", "6.13",
        "Mozilla/5.0 (PlayStation 4 5.55) AppleWebKit/601.2 (KHTML, like Gecko)"
    ),
    WebAnalytics("WEB_MUSIC_ANALYTICS", "0.2",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
    )
}