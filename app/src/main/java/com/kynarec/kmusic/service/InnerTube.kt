package com.kynarec.kmusic.service

import com.mewsic.innertube.InnertubeClient
import com.mewsic.innertube.enums.Client

class InnerTube {
    val web = InnertubeClient(Client.WEB_REMIX)
    val ios = InnertubeClient(Client.ANDROID)

    // ID for Lost by Linkin Park   auGo9bUpBf8


    suspend fun main() {
        ios.config()
        val data = ios.player("auGo9bUpBf8")
        println("Here comes a lot of data: ")
        println()
        println(data)
    }
}