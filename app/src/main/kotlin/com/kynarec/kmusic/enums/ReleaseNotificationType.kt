package com.kynarec.kmusic.enums

enum class ReleaseNotificationType {
    Never,
    PreRelease,
    Release;

    override fun toString(): String {
        return when (this) {
            Never -> "Never"
            PreRelease -> "Latest Pre-Release"
            Release -> "Latest Release"
        }
    }
}