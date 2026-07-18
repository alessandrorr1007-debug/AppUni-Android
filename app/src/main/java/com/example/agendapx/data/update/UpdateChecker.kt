package com.example.agendapx.data.update

import android.content.Context
import android.content.pm.PackageManager
import com.example.agendapx.data.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object UpdateChecker {

    private const val GITHUB_REPO = "alessandrorr1007-debug/AppUni-Android"
    private const val API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val response = NetworkUtils.hacerGet(API_URL)
            val json = JSONObject(response)

            val tagName = json.optString("tag_name", "")
            val remoteVersion = tagName.removePrefix("v")
            val currentVersion = getCurrentVersionName(context)

            if (compareVersions(remoteVersion, currentVersion) <= 0) {
                return@withContext null
            }

            val assets = json.optJSONArray("assets")
            var apkUrl = ""

            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            if (apkUrl.isEmpty()) return@withContext null

            UpdateInfo(
                versionName = remoteVersion,
                versionCode = parseVersionCode(remoteVersion),
                releaseNotes = json.optString("body", "Nueva versión disponible"),
                apkDownloadUrl = apkUrl,
                publishedAt = json.optString("published_at", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }

    private fun compareVersions(remote: String, local: String): Int {
        val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }

        val maxParts = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxParts) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r - l
        }
        return 0
    }

    private fun parseVersionCode(version: String): Int {
        val parts = version.split(".").map { it.toIntOrNull() ?: 0 }
        val major = parts.getOrElse(0) { 0 }
        val minor = parts.getOrElse(1) { 0 }
        val patch = parts.getOrElse(2) { 0 }
        return major * 10000 + minor * 100 + patch
    }
}
