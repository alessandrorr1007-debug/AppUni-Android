package com.example.agendapx.data.update

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val publishedAt: String
)
