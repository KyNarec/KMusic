package com.kynarec.kmusic.ui.components.settings.logs

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class LogTimespan {
    AllTime,
    Day,
    Hour,
    Minutes;

    override fun toString(): String {
        return when (this) {
            AllTime -> "All Time"
            Day -> "1 Day"
            Hour -> "1 Hour"
            Minutes -> "3 Min"
        }
    }

    fun formatedTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        return when (this) {
            AllTime -> "All Time"
            Day -> LocalDateTime.now().minusDays(1).format(formatter)
            Hour -> LocalDateTime.now().minusHours(2).format(formatter)
            Minutes -> LocalDateTime.now().minusMinutes(3).format(formatter)
        }
    }
}