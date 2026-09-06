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

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 24)
            setBackgroundColor(Color.rgb(8, 12, 20))
        }

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

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val input = EditText(this).apply {
            hint = "Ask JARVIS..."
            textSize = 16f
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setPadding(20, 16, 20, 16)
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

        val sendButton = Button(this).apply {
            text = "SEND"
        }

        sendButton.setOnClickListener {

            val message = input.text.toString().trim()

            if (message.isEmpty()) {
                return@setOnClickListener
            }

            chat.text =
                "You: $message\n\nJARVIS: Thinking..."

            sendButton.isEnabled = false
            input.isEnabled = false

            Thread {

                var connection: HttpURLConnection? = null

                try {

                    val url = URL(backendUrl)

                    connection =
                        url.openConnection() as HttpURLConnection

                    connection.requestMethod = "POST"
                    connection.connectTimeout = 30000
                    connection.readTimeout = 60000
                    connection.doOutput = true

                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                    )

                    connection.setRequestProperty(
                        "Accept",
                        "application/json"
                    )

                    val request =
                        JSONObject().apply {
                            put("message", message)
                        }

                    connection.outputStream.use { stream ->
                        stream.write(
                            request.toString()
                                .toByteArray(Charsets.UTF_8)
                        )
                    }

                    val code = connection.responseCode

                    val stream =
                        if (code in 200..299) {
                            connection.inputStream
                        } else {
                            connection.errorStream
                        }

                    val response =
                        stream?.bufferedReader()?.use {
                            it.readText()
                        } ?: ""

                    if (code in 200..299) {

                        val json =
                            JSONObject(response)

                        val reply =
                            json.optString(
                                "reply",
                                "No reply received."
                            )

                        runOnUiThread {

                            chat.text =
                                "You: $message\n\n" +
                                "JARVIS: $reply"

                            sendButton.isEnabled = true
                            input.isEnabled = true
                        }

                    } else {

                        runOnUiThread {

                            chat.text =
                                "You: $message\n\n" +
                                "JARVIS: Server error ($code)"

                            sendButton.isEnabled = true
                            input.isEnabled = true
                        }
                    }

                } catch (e: Exception) {

                    runOnUiThread {

                        chat.text =
                            "You: $message\n\n" +
                            "JARVIS: Connection failed.\n\n" +
                            "Please try again."

                        sendButton.isEnabled = true
                        input.isEnabled = true
                    }

                } finally {

                    connection?.disconnect()
                }

            }.start()

            input.text.clear()
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

        setContentView(root)
    }
}
