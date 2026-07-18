package com.example.agendapx.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object UpdateDownloader {

    private var downloadId: Long = -1L
    private var onCompleteCallback: ((Uri?) -> Unit)? = null

    fun downloadAndInstall(
        context: Context,
        apkUrl: String,
        onComplete: (Uri?) -> Unit
    ) {
        onCompleteCallback = onComplete

        val fileName = "AgendaPx-update.apk"
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        if (file.exists()) file.delete()

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Descargando actualización")
            .setDescription("Descargando AgendaPx...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(file))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) != downloadId) return

                val uri = getDownloadedUri(ctx ?: return)
                onCompleteCallback?.invoke(uri)
                onCompleteCallback = null

                try {
                    ctx?.unregisterReceiver(this)
                } catch (_: Exception) {}
            }
        }

        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, intentFilter)
        }
    }

    private fun getDownloadedUri(context: Context): Uri? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor: Cursor = downloadManager.query(query) ?: return null

        return try {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(columnIndex)

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "AgendaPx-update.apk")
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } else null
            } else null
        } finally {
            cursor.close()
        }
    }

    fun cancelDownload(context: Context) {
        if (downloadId != -1L) {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.remove(downloadId)
            downloadId = -1L
        }
    }
}
