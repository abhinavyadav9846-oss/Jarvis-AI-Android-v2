package com.abhinav.jarvis

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Hello Abhinav!\n\nJARVIS is starting..."
            textSize = 24f
            setPadding(40, 100, 40, 40)
        }

        setContentView(textView)
    }
}
