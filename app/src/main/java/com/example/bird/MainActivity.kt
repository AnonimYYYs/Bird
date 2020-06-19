package com.example.bird

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun launchGame(view: View) {
        val gameIntent = Intent(this, GameActivity::class.java)
        startActivity(gameIntent)
    }
}