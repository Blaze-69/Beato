package com.example.beato

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.example.beatmakingapp.R

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context: Context = this
        val intent2 = Intent().setClass(context, TrackActivity::class.java)
        intent2.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent2)
        val intent = Intent().setClass(context, PatternActivity::class.java)
        val message = "default : first"
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        intent.putExtra("msgFromParent", message)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}