package com.example.agendapx.data

import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

object NetworkUtils {

    private const val CONNECT_TIMEOUT = 30_000
    private const val READ_TIMEOUT = 60_000

    fun hacerGet(urlTexto: String): String {
        val connection = URL(urlTexto).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = CONNECT_TIMEOUT
        connection.readTimeout = READ_TIMEOUT
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun hacerPost(urlTexto: String, jsonBody: JSONObject): String {
        val connection = URL(urlTexto).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
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
    }
}
