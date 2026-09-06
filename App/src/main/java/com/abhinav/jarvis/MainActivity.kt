package com.abhinav.jarvis

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private val backendUrl =
        "https://jarvis-ai-android-v2.onrender.com/chat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Main screen
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 24)
            setBackgroundColor(Color.rgb(8, 12, 20))
        }

        // JARVIS title
        val title = TextView(this).apply {
            text = "JARVIS"
            textSize = 34f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Online status
        val status = TextView(this).apply {
            text = "●  ONLINE"
            textSize = 14f
            setTextColor(Color.rgb(80, 220, 150))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 32)
        }

        root.addView(
            status,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Chat / response area
        val chat = TextView(this).apply {
            text = "Hello Abhinav!\n\nHow can I help you?"
            textSize = 22f
            setTextColor(Color.WHITE)
            setPadding(0, 24, 0, 24)
        }

        root.addView(
            chat,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Bottom input row
        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Text input
        val input = EditText(this).apply {
            hint = "Ask JARVIS…"
            textSize = 16f
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.rgb(28, 35, 48))
        }

        inputRow.addView(
            input,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        // Send button
        val sendButton = Button(this).apply {
            text = "SEND"
        }

        sendButton.setOnClickListener {

            val message = input.text.toString().trim()

            if (message.isNotEmpty()) {

                chat.text =
                    "You: $message\n\n" +
                    "JARVIS: Thinking..."

                sendButton.isEnabled = false

                sendMessageToBackend(
                    message,
                    chat,
                    sendButton
                )

                input.text.clear()
            }
        }

        inputRow.addView(
            sendButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            inputRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Show screen
        setContentView(root)
    }

    private fun sendMessageToBackend(
        message: String,
        chat: TextView,
        sendButton: Button
    ) {

        Thread {

            var connection: HttpURLConnection? = null

            try {

                val url = URL(backendUrl)

                connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 30000
                connection.readTimeout = 60000

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                connection.doOutput = true

                // Request JSON
                val jsonRequest = JSONObject()

                jsonRequest.put(
                    "message",
                    message
                )

                // Send request
                connection.outputStream.use { outputStream ->

                    outputStream.write(
                        jsonRequest
                            .toString()
                            .toByteArray(Charsets.UTF_8)
                    )
                }

                val responseCode =
                    connection.responseCode

                val responseStream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val responseText =
                    responseStream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: ""

                if (responseCode in 200..299) {

                    val jsonResponse =
                        JSONObject(responseText)

                    val reply =
                        jsonResponse.optString(
                            "reply",
                            "JARVIS received an empty response."
                        )

                    runOnUiThread {

                        chat.text =
                            "You: $message\n\n" +
                            "JARVIS: $reply"

                        sendButton.isEnabled = true
                    }

                } else {

                    runOnUiThread {

                        chat.text =
                            "You: $message\n\n" +
                            "JARVIS: Backend error.\n\n" +
                            "HTTP $responseCode"

                        sendButton.isEnabled = true
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    chat.text =
                        "You: $message\n\n" +
                        "JARVIS: Connection failed.\n\n" +
                        "${e.message}"

                    sendButton.isEnabled = true
                }

            } finally {

                connection?.disconnect()
            }

        }.start()
    }
}
