package com.example.agendapx.data.update

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class UpdateDialogActivity : AppCompatActivity() {

    private var versionName: String = ""
    private var releaseNotes: String = ""
    private var apkDownloadUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        versionName = intent.getStringExtra(EXTRA_VERSION_NAME) ?: ""
        releaseNotes = intent.getStringExtra(EXTRA_RELEASE_NOTES) ?: ""
        apkDownloadUrl = intent.getStringExtra(EXTRA_APK_URL) ?: ""

        if (versionName.isEmpty() || apkDownloadUrl.isEmpty()) {
            finish()
            return
        }

        showUpdateDialog()
    }

    private fun showUpdateDialog() {
        val message = buildString {
            appendLine("Nueva versión: v$versionName")
            appendLine()
            appendLine("Cambios:")
            appendLine(releaseNotes.take(500))
        }

        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Actualización disponible")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Actualizar") { _, _ ->
                startDownload()
            }
            .setNegativeButton("Ahora no") { _, _ ->
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }

    private fun startDownload() {
        showDownloadProgress()

        UpdateDownloader.downloadAndInstall(this, apkDownloadUrl) { uri ->
            runOnUiThread {
                if (uri != null) {
                    installApk(uri)
                } else {
                    showError("Error al descargar la actualización")
                }
            }
        }
    }

    private fun showDownloadProgress() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 16)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val messageText = TextView(this).apply {
            text = "Descargando actualización..."
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
        }

        layout.addView(messageText)
        layout.addView(progressBar)

        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Descargando")
            .setView(layout)
            .setCancelable(false)
            .setNegativeButton("Cancelar") { _, _ ->
                UpdateDownloader.cancelDownload(this)
                finish()
            }
            .show()
    }

    private fun installApk(uri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (installIntent.resolveActivity(packageManager) != null) {
            startActivity(installIntent)
        } else {
            val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(settingsIntent)
        }

        finish()
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Aceptar", null)
            .setOnDismissListener { finish() }
            .show()
    }

    companion object {
        private const val EXTRA_VERSION_NAME = "update_version_name"
        private const val EXTRA_RELEASE_NOTES = "update_release_notes"
        private const val EXTRA_APK_URL = "update_apk_url"

        fun createIntent(context: android.content.Context, info: UpdateInfo): Intent {
            return Intent(context, UpdateDialogActivity::class.java).apply {
                putExtra(EXTRA_VERSION_NAME, info.versionName)
                putExtra(EXTRA_RELEASE_NOTES, info.releaseNotes)
                putExtra(EXTRA_APK_URL, info.apkDownloadUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
