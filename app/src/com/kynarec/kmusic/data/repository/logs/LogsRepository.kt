package com.kynarec.kmusic.data.repository.logs


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.core.content.FileProvider
import com.kynarec.kmusic.ui.components.settings.logs.LogTimespan
import kotlinx.serialization.json.Json
import java.io.File

class LogsRepository(
    private val context: Context
) {

    // Matches a "logcat -v epoch" line, e.g.:
    // 1783525901.683  7479  7479 I Zygote  : Process 7479 created for com.kynarec.kmusic.debug
    // Leading whitespace is allowed (adb pads columns), and the fractional part
    // can be anywhere from millisecond (3 digits) to nanosecond (9 digit) precision
    // depending on the device/logcat version.
    private val lineRegex = Regex(
        """^\s*(\d+)\.(\d+)\s+(\d+)\s+(\d+)\s+([VDIWEFA])\s+(.*?):\s?(.*)$"""
    )

    // Buffer separator lines like "--------- beginning of main" -- not log entries.
    private val bufferMarkerRegex = Regex("""^\s*-+\s*beginning of\s+\S+\s*$""")

    fun captureLogs(filename: String, logTimespan: LogTimespan) {
        val process = if (logTimespan == LogTimespan.AllTime) {
            Runtime.getRuntime().exec(
                arrayOf(
                    "logcat",
                    "-d",
                    "--pid=${Process.myPid()}",
                    "-v",
                    "epoch"
                )
            )
        } else {
            Runtime.getRuntime().exec(
                arrayOf(
                    "logcat",
                    "-d",
                    "-T",
                    logTimespan.formatedTime(),
                    "--pid=${Process.myPid()}",
                    "-v",
                    "epoch",
                )
            )
        }
        val logText = process.inputStream.bufferedReader().readText()

        val logcatFile = LogcatFile(
            metadata = buildMetadata(),
            logcatMessages = parseLogLines(logText)
        )

        val json = Json {
            prettyPrint = true
        }
        val file = File(context.filesDir, filename)
        file.writeText(
            json.encodeToString(LogcatFile.serializer(), logcatFile)
        )
    }

    fun shareLogs(
        context: Context,
        file: File
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Send logs")
        )
    }

    fun getLogcatFiles(): List<Pair<File, Boolean>> {
        val files =  context.filesDir
            .listFiles { file -> file.isFile && file.name.endsWith(".logcat") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        return files.map {
            it to false
        }
    }

    private fun parseLogLines(rawText: String): List<LogcatMessage> {
        val applicationId = context.packageName
        val messages = mutableListOf<LogcatMessage>()
        var lastIndex = -1

        rawText.lineSequence().forEach { line ->
            if (line.isBlank()) return@forEach

            // Skip buffer separator lines, e.g. "--------- beginning of main".
            if (bufferMarkerRegex.matches(line)) return@forEach

            val match = lineRegex.find(line)
            if (match == null) {
                // Continuation line (e.g. a multi-line stack trace) -- append
                // it to the previous message instead of dropping it.
                if (lastIndex >= 0) {
                    val prev = messages[lastIndex]
                    messages[lastIndex] = prev.copy(message = prev.message + "\n" + line.trim())
                }
                return@forEach
            }

            val (secStr, fractionStr, pidStr, tidStr, levelChar, tag, message) = match.destructured

            messages.add(
                LogcatMessage(
                    header = LogcatHeader(
                        logLevel = mapLevel(levelChar),
                        pid = pidStr.toIntOrNull() ?: 0,
                        tid = tidStr.toIntOrNull() ?: 0,
                        applicationId = applicationId,
                        processName = applicationId,
                        tag = tag.trim(),
                        timestamp = Timestamp(
                            seconds = secStr.toLongOrNull() ?: 0L,
                            nanos = fractionToNanos(fractionStr)
                        )
                    ),
                    message = message
                )
            )
            lastIndex = messages.lastIndex
        }

        return messages
    }

    /**
     * The fractional-second part of the epoch timestamp can be millisecond
     * (3 digits, e.g. "683"), microsecond (6 digits), or nanosecond (9 digits)
     * precision depending on the device/logcat version. Normalize it to
     * nanoseconds by right-padding with zeros to 9 digits.
     */
    private fun fractionToNanos(fraction: String): Int {
        val padded = fraction.padEnd(9, '0').take(9)
        return padded.toIntOrNull() ?: 0
    }

    private fun mapLevel(levelChar: String): String = when (levelChar) {
        "V" -> "VERBOSE"
        "D" -> "DEBUG"
        "I" -> "INFO"
        "W" -> "WARN"
        "E" -> "ERROR"
        "F", "A" -> "ASSERT"
        else -> "INFO"
    }

    private fun buildMetadata(): Metadata {
        val device = if (isEmulator()) {
            Device(
                emulatorDevice = EmulatorDeviceInfo(
                    serialNumber = Build.SERIAL ?: "unknown",
                    isOnline = true,
                    release = Build.VERSION.RELEASE,
                    apiLevel = ApiLevel(
                        majorVersion = Build.VERSION.SDK_INT,
                        minorVersion = 0
                    ),
                    featureLevel = Build.VERSION.SDK_INT,
                    avdName = Build.MODEL,
                    avdPath = "",
                    type = "HANDHELD"
                )
            )
        } else {
            Device(
                physicalDevice = PhysicalDeviceInfo(
                    serialNumber = Build.SERIAL ?: "unknown",
                    isOnline = true,
                    release = Build.VERSION.RELEASE,
                    apiLevel = ApiLevel(
                        majorVersion = Build.VERSION.SDK_INT,
                        minorVersion = 0
                    ),
                    featureLevel = Build.VERSION.SDK_INT,
                    manufacturer = Build.MANUFACTURER,
                    model = Build.MODEL,
                    type = "HANDHELD"
                )
            )
        }

        return Metadata(
            device = device,
            filter = "package:mine -message:setRequestedFrameRate",
            projectApplicationIds = listOf(
                "com.kynarec.kmusic.debug",
                "com.kynarec.kmusic.debug.test",
                "com.kynarec.kmusic",
                "com.kynarec.lrclib.test",
                "com.kynarec.klyrics.test"
            )
        )
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk")
    }

    fun deleteLog(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }
}
