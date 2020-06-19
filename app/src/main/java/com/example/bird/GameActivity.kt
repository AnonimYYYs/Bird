package com.example.bird

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.random.Random
import androidx.core.view.marginLeft

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
        Columns.init(this, game_layout, width, height)
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
                screenHandler.postDelayed(this, 25)

            }

        })
    }

    override fun onBackPressed() {
        if(isGame){
            onGameStopped()
        } else {
            onGameResume()
        }
        isGame = !isGame
    }

    fun birdJump(view: View) {
        if(isGame) {
            BirdCoordinate.jump()
        }
    }

    fun onGameUpdate(){
        val param = bird.layoutParams as ViewGroup.MarginLayoutParams
        param.bottomMargin = BirdCoordinate.lootNewHeightPoint()
        bird.layoutParams = param
        Columns.moveColumns()
    }

    private fun onGameStopped(){
        val params = pause_layout.layoutParams as ViewGroup.MarginLayoutParams
        params.width = width
        params.height = height
        pause_layout.layoutParams = params
        game_layout.bringChildToFront(pause_layout)
    }

    fun onGamePause(){

    }

    private fun onGameResume(){
        val params = pause_layout.layoutParams as ViewGroup.MarginLayoutParams
        params.width = 0
        params.height = 0
        pause_layout.layoutParams = params
    }

    fun onButtonExit(view: View) {

    }

    fun onButtonResume(view: View) {
        isGame = true
        onGameResume()
    }

    override fun finish() {
        BirdCoordinate.reset()
        Columns.reset()
        super.finish()
    }
}

object BirdCoordinate{
    private const val gravity = -0.00125f
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

object Columns {

    // values of columns
    private const val moveSpeed = 6 // bigger value - faster columns
    private const val columnsRate = 1.5 // bigger value - more columns
    private const val holeSize = 0.3f

    private var columnWidth: Int = 0

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    // контекст для создания imageView
    private lateinit var context: Context
    // layout на который добавим imageView
    private lateinit var layout: RelativeLayout
    // список имаджей
    private var columns = arrayListOf<ImageView>()

    fun init(c: Context, l: RelativeLayout, width: Int, height: Int){
        layout = l
        context = c
        screenWidth = width
        screenHeight = height
        columnWidth = (height * 0.12f).toInt()
    }

    private fun addColumn(){
        val imageViewUp = ImageView(context)
        val imageViewDown = ImageView(context)
        imageViewUp.setImageResource(R.color.colorPrimary)
        imageViewDown.setImageResource(R.color.colorPrimary)

        val params = ViewGroup.MarginLayoutParams(0, 0)

        // общие параметры
        params.leftMargin = screenWidth
        params.width = columnWidth

        // upper height
        params.height = (0.1f * screenHeight + (0.8f - holeSize) * screenHeight * Random.nextFloat()).toInt()
        imageViewUp.layoutParams = params
        layout.addView(imageViewUp)
        columns.add(imageViewUp)

        // down height
        params.topMargin = (params.height + screenHeight * holeSize).toInt()
        params.height = screenHeight - params.topMargin
        imageViewDown.layoutParams = params
        layout.addView(imageViewDown)
        columns.add(imageViewDown)



        // todo add bottom align
    }

    fun moveColumns(){
        if(columns.isEmpty()){
            addColumn()
        }
        if (columns.last().marginLeft <= (screenWidth - (screenHeight / columnsRate)).toInt()) {
            addColumn()
        }
        var colIterator = columns.iterator()
        for(col in colIterator){
            val params = col.layoutParams as ViewGroup.MarginLayoutParams
            params.leftMargin -= moveSpeed
            col.layoutParams = params
            if(col.marginLeft <= -columnWidth){
                layout.removeView(col)
                colIterator.remove()
            }
        }
    }

    fun reset(){
        val colIterator = columns.iterator()
        for(col in colIterator){
            layout.removeView(col)
            colIterator.remove()
        }
    }
}