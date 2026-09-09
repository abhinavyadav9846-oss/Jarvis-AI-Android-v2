package com.abhinav.jarvis

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val backendUrl =
        "https://jarvis-ai-android-v2.onrender.com/chat"

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var chatText: TextView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(25, 50, 25, 25)
        root.setBackgroundColor(Color.rgb(8, 12, 20))

        val title = TextView(this)
        title.text = "JARVIS AI"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 0, 0, 30)

        chatText = TextView(this)
        chatText.text = "JARVIS:\nHello Abhinav! 👋\n\nAsk me anything..."
        chatText.textSize = 18f
        chatText.setTextColor(Color.WHITE)
        chatText.setPadding(20, 20, 20, 20)

        val scrollParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )

        inputText = EditText(this)
        inputText.hint = "Type your message..."
        inputText.setTextColor(Color.WHITE)
        inputText.setHintTextColor(Color.GRAY)
        inputText.setSingleLine(false)

        sendButton = Button(this)
        sendButton.text = "SEND"

        val bottom = LinearLayout(this)
        bottom.orientation = LinearLayout.HORIZONTAL

        bottom.addView(
            inputText,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        bottom.addView(
            sendButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(title)
        root.addView(chatText, scrollParams)
        root.addView(bottom)

        setContentView(root)

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {

        val message = inputText.text.toString().trim()

        if (message.isEmpty()) {
            return
        }

        chatText.text =
            chatText.text.toString() +
            "\n\nYou:\n$message\n\nJARVIS:\nThinking..."

        inputText.setText("")
        sendButton.isEnabled = false

        executor.execute {

            try {
                val url = URL(backendUrl)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.doOutput = true

                val json = JSONObject()
                json.put("message", message)

                connection.outputStream.use { output ->
                    output.write(json.toString().toByteArray())
                }

                val responseCode = connection.responseCode

                val responseText =
                    if (responseCode in 200..299) {
                        connection.inputStream
                            .bufferedReader()
                            .use { it.readText() }
                    } else {
                        connection.errorStream
                            ?.bufferedReader()
                            ?.use { it.readText() }
                            ?: "Server error: $responseCode"
                    }

                connection.disconnect()

                val reply = try {
                    JSONObject(responseText).optString(
                        "reply",
                        "JARVIS could not understand the response."
                    )
                } catch (e: Exception) {
                    "Server response error."
                }

                handler.post {
                    chatText.text =
                        chatText.text.toString()
                            .replace("Thinking...", reply)

                    sendButton.isEnabled = true
                }

            } catch (e: Exception) {

                handler.post {
                    chatText.text =
                        chatText.text.toString()
                            .replace(
                                "Thinking...",
                                "Connection error. Please try again."
                            )

                    sendButton.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
