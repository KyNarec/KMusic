package com.kynarec.kmusic.service.update

data class Version(
    val major: Int, val minor: Int, val patch: Int, val preRelease: String? = null
) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)

        return when {
            preRelease == null && other.preRelease == null -> 0
            preRelease == null -> 1  // stable > pre-release
            other.preRelease == null -> -1
            else -> comparePreRelease(preRelease, other.preRelease)
        }
    }

    private fun comparePreRelease(a: String, b: String): Int {
        val aParts = a.split(".")
        val bParts = b.split(".")
        val maxSize = maxOf(aParts.size, bParts.size)

        for (i in 0 until maxSize) {
            val aPart = aParts.getOrNull(i)
            val bPart = bParts.getOrNull(i)

            // shorter identifier list has lower precedence (e.g. "alpha" < "alpha.1")
            if (aPart == null) return -1
            if (bPart == null) return 1

            val aNum = aPart.toIntOrNull()
            val bNum = bPart.toIntOrNull()

            val cmp = when {
                aNum != null && bNum != null -> aNum.compareTo(bNum) // numeric compare
                aNum != null -> -1 // numeric identifiers < alphanumeric
                bNum != null -> 1
                else -> aPart.compareTo(bPart) // string compare
            }
            if (cmp != 0) return cmp
        }
        return 0
    }

    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        preRelease?.let { append("-$it") }
    }

    companion object {
        fun parse(versionString: String): Version? {
            val cleaned = versionString.trim().removePrefix("v").removePrefix("V").trimStart('.')
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
