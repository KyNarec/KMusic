package com.kynarec.kmusic.enums

enum class PlayerRepeatMode {
    RepeatModeAll,
    RepeatModeOne,
    RepeatModeOff
    ;

    override fun toString(): String = when (this) {
        RepeatModeAll -> "Jumps to the beginning of the queue"
        RepeatModeOne -> "Repeats the last song in the queue"
        RepeatModeOff -> "Ends after the last song in the queue"
    }
}