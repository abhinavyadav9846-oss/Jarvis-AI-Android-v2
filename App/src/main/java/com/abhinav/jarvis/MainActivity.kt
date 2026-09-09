package com.abhinav.jarvis

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
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

    private lateinit var chatContainer: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(16, 20, 16, 12)
        root.setBackgroundColor(Color.rgb(5, 9, 16))

        val title = TextView(this)
        title.text = "JARVIS AI"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setPadding(0, 8, 0, 2)

        val status = TextView(this)
        status.text = "● ONLINE"
        status.textSize = 13f
        status.setTextColor(Color.rgb(0, 220, 180))
        status.gravity = Gravity.CENTER
        status.setPadding(0, 0, 0, 15)

        scrollView = ScrollView(this)
        scrollView.setFillViewport(true)

        chatContainer = LinearLayout(this)
        chatContainer.orientation = LinearLayout.VERTICAL
        chatContainer.setPadding(4, 5, 4, 10)

        scrollView.addView(chatContainer)

        addMessage(
            "JARVIS",
            "Hello Abhinav! 👋\n\nI am ready. Ask me anything."
        )

        val bottom = LinearLayout(this)
        bottom.orientation = LinearLayout.HORIZONTAL
        bottom.gravity = Gravity.CENTER_VERTICAL
        bottom.setPadding(0, 10, 0, 0)

        inputText = EditText(this)
        inputText.hint = "Message JARVIS..."
        inputText.setTextColor(Color.WHITE)
        inputText.setHintTextColor(Color.GRAY)
        inputText.setSingleLine(true)
        inputText.textSize = 16f
        inputText.setPadding(18, 0, 18, 0)

        val inputBackground = GradientDrawable()
        inputBackground.setColor(Color.rgb(18, 24, 34))
        inputBackground.cornerRadius = 50f
        inputText.background = inputBackground

        sendButton = Button(this)
        sendButton.text = "SEND"
        sendButton.textSize = 14f
        sendButton.setTextColor(Color.BLACK)

        val buttonBackground = GradientDrawable()
        buttonBackground.setColor(Color.rgb(0, 220, 180))
        buttonBackground.cornerRadius = 50f
        sendButton.background = buttonBackground

        bottom.addView(
            inputText,
            LinearLayout.LayoutParams(
                0,
                58,
                1f
            )
        )

        val buttonParams = LinearLayout.LayoutParams(
            110,
            58
        )
        buttonParams.setMargins(10, 0, 0, 0)

        bottom.addView(sendButton, buttonParams)

        root.addView(title)

        root.addView(status)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(bottom)

        setContentView(root)

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun addMessage(sender: String, message: String) {

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL

        val bubble = TextView(this)

        bubble.text = message
        bubble.textSize = 17f
        bubble.setTextColor(Color.WHITE)
        bubble.setPadding(20, 15, 20, 15)

        val bubbleBackground = GradientDrawable()

        if (sender == "YOU") {

            row.gravity = Gravity.END

            bubbleBackground.setColor(
                Color.rgb(0, 120, 105)
            )

            bubbleBackground.cornerRadius = 35f

        } else {

            row.gravity = Gravity.START

            bubbleBackground.setColor(
                Color.rgb(25, 31, 42)
            )

            bubbleBackground.cornerRadius = 35f
        }

        bubble.background = bubbleBackground

        val bubbleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        bubbleParams.setMargins(0, 6, 0, 6)

        bubble.maxWidth = 850

        row.addView(bubble, bubbleParams)

        chatContainer.addView(row)

        scrollView.post {
            scrollView.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        }
    }

    private fun sendMessage() {

        val message = inputText.text.toString().trim()

        if (message.isEmpty()) {
            return
        }

        addMessage("YOU", message)

        inputText.setText("")

        sendButton.isEnabled = false

        addMessage(
            "JARVIS",
            "Thinking..."
        )

        executor.execute {

            try {

                val url = URL(backendUrl)

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.doOutput = true

                val json = JSONObject()

                json.put(
                    "message",
                    message
                )

                connection.outputStream.use { output ->

                    output.write(
                        json.toString()
                            .toByteArray()
                    )
                }

                val responseCode =
                    connection.responseCode

                val responseText =
                    if (responseCode in 200..299) {

                        connection.inputStream
                            .bufferedReader()
                            .use {
                                it.readText()
                            }

                    } else {

                        connection.errorStream
                            ?.bufferedReader()
                            ?.use {
                                it.readText()
                            }
                            ?: "Server error: $responseCode"
                    }

                connection.disconnect()

                val reply = try {

                    JSONObject(responseText)
                        .optString(
                            "reply",
                            "JARVIS could not understand the response."
                        )

                } catch (e: Exception) {

                    "Server response error."
                }

                handler.post {

                    removeThinking()

                    addMessage(
                        "JARVIS",
                        reply
                    )

                    sendButton.isEnabled = true
                }

            } catch (e: Exception) {

                handler.post {

                    removeThinking()

                    addMessage(
                        "JARVIS",
                        "Connection error. Please try again."
                    )

                    sendButton.isEnabled = true
                }
            }
        }
    }

    private fun removeThinking() {

        if (chatContainer.childCount > 0) {

            chatContainer.removeViewAt(
                chatContainer.childCount - 1
            )
        }
    }

    override fun onDestroy() {

        executor.shutdownNow()

        super.onDestroy()
    }
}
