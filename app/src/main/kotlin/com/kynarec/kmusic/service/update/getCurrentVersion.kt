package com.kynarec.kmusic.service.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

fun getCurrentVersion(): String {
    val context = PlatformContext.get() as Context
    var versionNumber: String? = null
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            versionNumber = context.packageManager
                .getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                .versionName
        } else {
            @Suppress("DEPRECATION")
            versionNumber = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
        }
    } catch (e: Exception) {
        Log.e("PlatformUpdateManager", "Fetching version Number failed because of: $e")
        Log.w("PlatformUpdateManager", "Falling back to version \"0.0.0\"")
    }
    return versionNumber?: "0.0.0"
}