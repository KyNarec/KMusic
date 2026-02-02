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

#-if @kotlinx.serialization.Serializable class **
#-keepclassmembers class <1> {
#    static <1>$Companion Companion;
#}
#
#-if @kotlinx.serialization.Serializable class ** {
#    static **$* *;
#}
#-keepclassmembers class <2>$<3> {
#    kotlinx.serialization.KSerializer serializer(...);
#}
#
#-if @kotlinx.serialization.Serializable class ** {
#    public static ** INSTANCE;
#}
#-keepclassmembers class <1> {
#    public static <1> INSTANCE;
#    kotlinx.serialization.KSerializer serializer(...);
#}
#
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Service
#
#-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile

# 1. Keep the package and all its classes
-keep class com.kynarec.klyrics.** { *; }

# 2. Keep the names of all members (fields/methods) to prevent breakage
# in state restoration and data class copy() methods
-keepclassmembers class com.kynarec.klyrics.** {
    <fields>;
    <methods>;
}

# 3. Specifically keep the LyricsLine sealed interface and its implementations
# because the UI logic relies on the specific types (WordSynced, Default, Unsynced)
-keep interface com.kynarec.klyrics.LyricsLine { *; }
-keep class com.kynarec.klyrics.LyricsLine$* { *; }

# 4. Keep the Enum for AutoscrollMode so it doesn't get obfuscated
-keepclassmembers enum com.kynarec.klyrics.AutoscrollMode {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 5. Keep Compose-specific annotations used in your file
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault
-keepattributes Signature

# Keep @Stable and @Immutable classes to ensure Compose compiler optimizations aren't stripped
-keep @androidx.compose.runtime.Stable class *
-keep @androidx.compose.runtime.Immutable class *

# Prevent R8 from over-optimizing/renaming the actual Composable functions
-keepclassmembers class com.kynarec.klyrics.** {
    @androidx.compose.runtime.Composable <methods>;
    @androidx.compose.runtime.ReadOnlyComposable <methods>;
}

# Keep the synthetic lambda classes that the Compose compiler creates
# This helps resolve those "Failed to tokenize" warnings
-keep class com.kynarec.klyrics.**$* { *; }

# Keep specific types used in your LyricsViewKt function parameters
-keep class com.kynarec.klyrics.LyricsState { *; }
-keep class com.kynarec.klyrics.AutoscrollMode { *; }
-keep class com.kynarec.klyrics.UiLyrics { *; }