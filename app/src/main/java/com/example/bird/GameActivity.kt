package com.example.bird

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    // sizes
    private var width = 0
    private var height = 0
    private var birdSize = 0.0f

    private var isGame = true

    val screenHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // getting size of window
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        width = metrics.widthPixels
        height = metrics.heightPixels

        // setting bird size
        birdSize = height * 0.1f
        bird.layoutParams.height = birdSize.toInt()
        bird.layoutParams.width = birdSize.toInt()

        // initializing objects
        BirdCoordinate.oppressHeight(height)
        // and bird at all
        val param = bird.layoutParams as ViewGroup.MarginLayoutParams
        param.leftMargin = (width * 0.1f).toInt()
        param.bottomMargin = (height / 2 - birdSize / 2).toInt()
        bird.layoutParams = param

        //launch game

        screenHandler.post(object: Runnable{
            override fun run() {
                if(isGame) {
                    onGameUpdate()
                } else {
                    onGamePause()
                }
                screenHandler.postDelayed(this, 50)

            }

        })
    }

    override fun onBackPressed() {
        BirdCoordinate.reset()
        screenHandler.removeCallbacksAndMessages(null)
        finish()
    }

    fun birdJump(view: View) {
        BirdCoordinate.jump()
    }

    fun onGameUpdate(){
        val param = bird.layoutParams as ViewGroup.MarginLayoutParams
        param.bottomMargin = BirdCoordinate.lootNewHeightPoint()
        bird.layoutParams = param
    }

    fun onGamePause(){

    }
}

object BirdCoordinate{
    private const val gravity = -0.0025f
    private const val jumpForce = 0.022f
    private var speed = jumpForce
    private var position = 0.5f // [0;1]
    private var windowHeight = 0

    fun lootNewHeightPoint(): Int {
        speed += gravity
        position += speed
        if(position < 0.0f){
            position = 0.0f
        }
        if(position > 1.0f){
            position = 1.0f
        }
        if(speed > 0.5f){
            speed = 0.5f
        }
        if(speed < -0.5f){
            speed = -0.5f
        }
        return((windowHeight * 0.9f * position).toInt())
    }

    fun oppressHeight(oppressedWindowHeight: Int){
        windowHeight = oppressedWindowHeight
    }

    fun jump(){
        speed = jumpForce
    }

    fun reset(){
        speed = jumpForce
        position = 0.5f
    }
}

