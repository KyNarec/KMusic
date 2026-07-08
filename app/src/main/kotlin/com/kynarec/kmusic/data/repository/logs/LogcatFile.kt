package com.kynarec.kmusic.data.repository.logs

import kotlinx.serialization.Serializable

@Serializable
data class LogcatFile(
    val metadata: Metadata,
    val logcatMessages: List<LogcatMessage>
)

@Serializable
data class Metadata(
    val device: Device,
    val filter: String,
    val projectApplicationIds: List<String>
)

/**
 * Mirrors the oneof-style "device" object in Android Studio's exported .logcat
 * files: only one of [emulatorDevice] / [physicalDevice] is populated.
 */
@Serializable
data class Device(
    val emulatorDevice: EmulatorDeviceInfo? = null,
    val physicalDevice: PhysicalDeviceInfo? = null
)

@Serializable
data class EmulatorDeviceInfo(
    val serialNumber: String,
    val isOnline: Boolean,
    val release: String,
    val apiLevel: ApiLevel,
    val featureLevel: Int,
    val avdName: String,
    val avdPath: String,
    val type: String
)

@Serializable
data class PhysicalDeviceInfo(
    val serialNumber: String,
    val isOnline: Boolean,
    val release: String,
    val apiLevel: ApiLevel,
    val featureLevel: Int,
    val manufacturer: String,
    val model: String,
    val type: String
)

@Serializable
data class ApiLevel(
    val majorVersion: Int,
    val minorVersion: Int
)

@Serializable
data class LogcatMessage(
    val header: LogcatHeader,
    val message: String
)

@Serializable
data class LogcatHeader(
    val logLevel: String,
    val pid: Int,
    val tid: Int,
    val applicationId: String,
    val processName: String,
    val tag: String,
    val timestamp: Timestamp
)

@Serializable
data class Timestamp(
    val seconds: Long,
    val nanos: Int
)