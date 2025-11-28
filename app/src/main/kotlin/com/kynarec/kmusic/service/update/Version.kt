package com.kynarec.kmusic.service.update

data class Version(
    val major: Int, val minor: Int, val patch: Int, val preRelease: String? = null
) : Comparable<Version> {

    override fun compareTo(other: Version): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)

        // Handle pre-release versions (alpha, beta, rc)
        return when {
            preRelease == null && other.preRelease == null -> 0
            preRelease == null -> 1 // stable > pre-release
            other.preRelease == null -> -1
            else -> preRelease.compareTo(other.preRelease)
        }
    }

    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        preRelease?.let { append("-$it") }
    }

    companion object {
        fun parse(versionString: String): Version? {
            // Clean up the version string: remove 'v' prefix, trim whitespace, remove leading dots
            val cleaned = versionString.trim().removePrefix("v").removePrefix("V").trimStart('.')

            // Support both X.Y.Z and X.Y formats (patch defaults to 0)
            val regex = """(\d+)\.(\d+)(?:\.(\d+))?(?:-(.+))?""".toRegex()
            val match = regex.matchEntire(cleaned) ?: return null

            return Version(
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues.getOrNull(3)?.toIntOrNull() ?: 0,
                preRelease = match.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() })
        }
    }
}

