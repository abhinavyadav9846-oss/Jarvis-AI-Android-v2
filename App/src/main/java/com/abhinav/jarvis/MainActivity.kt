package com.abhinav.jarvis

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)

        text.text = "JARVIS\n\nHello Abhinav!"
        text.textSize = 28f
        text.setTextColor(Color.WHITE)
        text.setBackgroundColor(Color.rgb(8, 12, 20))
        text.setPadding(40, 80, 40, 40)

        setContentView(text)
    }
}
