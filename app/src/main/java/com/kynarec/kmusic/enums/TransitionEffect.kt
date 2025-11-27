package com.kynarec.kmusic.enums

enum class TransitionEffect(val label: String) {
    SlideVertical("Slide Vertical"),
    SlideHorizontal("Slide Horizontal"),
    Scale("Scale"),
    Fade("Fade"),
    Expand("Expand"),
    None("None");

    companion object {
        val all get() = values().toList()
        fun fromName(name: String) = values().firstOrNull { it.name == name } ?: None
    }
}