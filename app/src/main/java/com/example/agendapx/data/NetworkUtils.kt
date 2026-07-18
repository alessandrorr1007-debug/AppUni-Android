package com.example.agendapx.data

import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {

    private const val CONNECT_TIMEOUT = 30_000
    private const val READ_TIMEOUT = 60_000

    fun hacerGet(urlTexto: String, token: String? = null): String {
        val connection = URL(urlTexto).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }

            if (connection.responseCode in 200..299) {
                return connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                return connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
        } finally {
            connection.disconnect()
        }
    }

    fun hacerGetAuth(urlTexto: String): String {
        val token = runCatching { UserPreferences.getTokenSync() }.getOrNull()
        return hacerGet(urlTexto, token)
    }

    fun hacerPost(urlTexto: String, jsonBody: JSONObject, token: String? = null): String {
        val connection = URL(urlTexto).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.doOutput = true

            connection.outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
            }

            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            return stream?.bufferedReader()?.use { it.readText() } ?: ""
        } finally {
            connection.disconnect()
        }
    }

    fun hacerPostAuth(urlTexto: String, jsonBody: JSONObject): String {
        val token = runCatching { UserPreferences.getTokenSync() }.getOrNull()
        return hacerPost(urlTexto, jsonBody, token)
    }
}
