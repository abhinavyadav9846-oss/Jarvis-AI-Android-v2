package com.abhinav.jarvis

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : Activity() {

    private val backendUrl =
        "https://jarvis-ai-android-v2.onrender.com/chat"

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------- MAIN SCREEN ----------

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.setPadding(32, 48, 32, 24)
        root.setBackgroundColor(Color.rgb(8, 12, 20))

        // Title
        val title = TextView(this)

        title.text = "JARVIS"
        title.textSize = 34f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Status
        val status = TextView(this)

        status.text = "● ONLINE"
        status.textSize = 14f
        status.setTextColor(Color.rgb(80, 220, 150))
        status.gravity = Gravity.CENTER
        status.setPadding(0, 10, 0, 25)

        root.addView(
            status,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Chat area
        val chat = TextView(this)

        chat.text = "Hello Abhinav!\n\nHow can I help you?"
        chat.textSize = 21f
        chat.setTextColor(Color.WHITE)
        chat.setPadding(0, 25, 0, 25)

        root.addView(
            chat,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Input
        val input = EditText(this)

        input.hint = "Ask JARVIS..."
        input.textSize = 16f
        input.setSingleLine(true)
        input.setTextColor(Color.WHITE)
        input.setHintTextColor(Color.GRAY)
        input.setPadding(20, 15, 20, 15)
        input.setBackgroundColor(Color.rgb(28, 35, 48))

        root.addView(
            input,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Send button
        val sendButton = Button(this)

        sendButton.text = "SEND"

        root.addView(
            sendButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // ---------- SEND ----------

        sendButton.setOnClickListener {

            val message = input.text.toString().trim()

            if (message.isEmpty()) {
                chat.text = "Please type something first."
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
                    connection.connectTimeout = 20000
                    connection.readTimeout = 60000
                    connection.doOutput = true

                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                    )

                    connection.setRequestProperty(
                        "Accept",
                        "application/json"
                    )

                    val request =
                        JSONObject()

                    request.put(
                        "message",
                        message
                    )

                    connection.outputStream.use { output ->

                        output.write(
                            request
                                .toString()
                                .toByteArray(Charsets.UTF_8)
                        )

                        output.flush()
                    }

                    val responseCode =
                        connection.responseCode

                    val stream =
                        if (responseCode in 200..299) {
                            connection.inputStream
                        } else {
                            connection.errorStream
                        }

                    val reader =
                        BufferedReader(
                            InputStreamReader(stream)
                        )

                    val responseText =
                        reader.use {
                            it.readText()
                        }

                    if (responseCode in 200..299) {

                        val json =
                            JSONObject(responseText)

                        val reply =
                            json.optString(
                                "reply",
                                "No reply received."
                            )

                        mainHandler.post {

                            chat.text =
                                "You: $message\n\n" +
                                "JARVIS: $reply"

                            sendButton.isEnabled = true
                            input.isEnabled = true
                        }

                    } else {

                        mainHandler.post {

                            chat.text =
                                "You: $message\n\n" +
                                "JARVIS: Server error\n" +
                                "HTTP $responseCode"

                            sendButton.isEnabled = true
                            input.isEnabled = true
                        }
                    }

                } catch (e: Exception) {

                    mainHandler.post {

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

        // ---------- SHOW APP ----------

        setContentView(root)
    }

    override fun onDestroy() {
        super.onDestroy()

        mainHandler.removeCallbacksAndMessages(null)
    }
}
