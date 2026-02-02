# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# -dontwarn io.ktor.client.network.sockets.SocketTimeoutException
# -dontwarn io.ktor.client.network.sockets.TimeoutExceptionsCommonKt
# -dontwarn io.ktor.client.plugins.HttpTimeout$HttpTimeoutCapabilityConfiguration
# -dontwarn io.ktor.client.plugins.HttpTimeout$Plugin
# -dontwarn io.ktor.client.plugins.HttpTimeout
# -dontwarn io.ktor.util.InternalAPI
# -dontwarn io.ktor.utils.io.ByteReadChannelJVMKt
# -dontwarn io.ktor.utils.io.CoroutinesKt
# -dontwarn io.ktor.utils.io.core.ByteBuffersKt
# -dontwarn io.ktor.utils.io.core.BytePacketBuilder
# -dontwarn io.ktor.utils.io.core.ByteReadPacket$Companion
# -dontwarn io.ktor.utils.io.core.ByteReadPacket
# -dontwarn io.ktor.utils.io.core.CloseableJVMKt
# -dontwarn io.ktor.utils.io.core.Input
# -dontwarn io.ktor.utils.io.core.InputArraysKt
# -dontwarn io.ktor.utils.io.core.InputPrimitivesKt
# -dontwarn io.ktor.utils.io.core.Output
# -dontwarn io.ktor.utils.io.core.OutputPrimitivesKt
# -dontwarn io.ktor.utils.io.core.PreviewKt
-dontwarn io.ktor.client.network.sockets.SocketTimeoutException
-dontwarn io.ktor.client.network.sockets.TimeoutExceptionsCommonKt
-dontwarn io.ktor.client.plugins.HttpTimeout$HttpTimeoutCapabilityConfiguration
-dontwarn io.ktor.client.plugins.HttpTimeout$Plugin
-dontwarn io.ktor.client.plugins.HttpTimeout
-dontwarn io.ktor.util.InternalAPI
-dontwarn io.ktor.utils.io.ByteReadChannelJVMKt
-dontwarn io.ktor.utils.io.CoroutinesKt
-dontwarn io.ktor.utils.io.core.ByteBuffersKt
-dontwarn io.ktor.utils.io.core.BytePacketBuilder
-dontwarn io.ktor.utils.io.core.ByteReadPacket$Companion
-dontwarn io.ktor.utils.io.core.ByteReadPacket
-dontwarn io.ktor.utils.io.core.CloseableJVMKt
-dontwarn io.ktor.utils.io.core.Input
-dontwarn io.ktor.utils.io.core.InputArraysKt
-dontwarn io.ktor.utils.io.core.InputPrimitivesKt
-dontwarn io.ktor.utils.io.core.Output
-dontwarn io.ktor.utils.io.core.OutputPrimitivesKt
-dontwarn io.ktor.utils.io.core.PreviewKt

#-dontshrink
-dontobfuscate
#-dontoptimize
#-repackageclasses 'defpackage'

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service

-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

#-dontwarn com.kynarec.klyrics.AutoscrollMode
#-dontwarn com.kynarec.klyrics.LyricsDefaults
#-dontwarn com.kynarec.klyrics.LyricsLine$Default
#-dontwarn com.kynarec.klyrics.LyricsLine
#-dontwarn com.kynarec.klyrics.LyricsState
#-dontwarn com.kynarec.klyrics.LyricsViewKt
#-dontwarn com.kynarec.klyrics.UiLyrics
#-dontwarn com.kynarec.lrclib.LrcLib
#-dontwarn com.kynarec.lrclib.LyricsRepository
#-dontwarn com.kynarec.lrclib.model.Lyrics