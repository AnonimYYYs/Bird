package com.example.bird

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.core.view.marginTop
import kotlin.math.PI
import kotlin.math.atan

data class Column (val upper: ImageView, val lower: ImageView)

data class Hole (val upper: Int, val lower: Int)

const val COLUMN_SPEED = 6  // bigger value - faster columns

// todo remove later
var info = "0"

class GameActivity : AppCompatActivity() {

    // sizes
    private var width = 0
    private var height = 0
    private var birdSize = 0.0f

    private var isGame = true
    private var isPause = false

    private var totalScore = 0

    val screenHandler = Handler(Looper.getMainLooper())

    // override functions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // getting data from main screen intent
        totalScore = intent.extras?.getInt("topScore") ?: 0

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
        BirdCoordinate.oppressBirdCenter((width * 0.1f + birdSize / 2).toInt())
        Columns.init(this, game_layout, width, height)

        // and bird at all
        val param = bird.layoutParams as ViewGroup.MarginLayoutParams
        param.leftMargin = (width * 0.1f).toInt()
        param.bottomMargin = (height / 2 - birdSize / 2).toInt()
        bird.layoutParams = param

        //launch game

        screenHandler.post(object: Runnable{
            override fun run() {
                if(isGame && !isPause) {
                    onGameUpdate()
                } else {
                    onGamePause()
                }
                screenHandler.postDelayed(this, 25)

            }

        })
    }

    override fun onBackPressed() {
        if(!isPause){
            onGameStopped()
        } else {
            onGameResume()
        }
        isPause = !isPause

    }

    override fun finish() {
        // resetting bird and columns
        totalScore += BirdCoordinate.lootBirdScore()
        BirdCoordinate.reset()
        Columns.reset()

        // giving back to main menu total score
        val answerIntent = Intent()
        answerIntent.putExtra("newScore", totalScore)
        setResult(Activity.RESULT_OK, answerIntent)
        super.finish()
    }

    override fun onUserLeaveHint() {
        isPause = true
        onGameStopped()
        super.onUserLeaveHint()
    }

    // button functions
    fun onBirdJump(view: View) {
        if(!isPause && isGame) {
            BirdCoordinate.jump()
        }
    }

    fun onButtonExit(view: View) {
        finish()
    }

    fun onButtonResume(view: View) {
        onGameResume()
        isPause = false
    }

    fun onToMainMenu(view: View){
        finish()
    }

    fun onGameRestart(view: View){
        totalScore += BirdCoordinate.lootBirdScore()
        BirdCoordinate.reset()
        Columns.reset()
        onGameRestarted()
    }

    // other functions
    // -- draw pause screen
    private fun onGameStopped(){
        val params = pause_layout.layoutParams as ViewGroup.MarginLayoutParams
        params.width = width
        params.height = height
        pause_layout.layoutParams = params
        game_layout.bringChildToFront(pause_layout)
        jump_button.isEnabled = false
    }

    private fun onGameResume(){
        val params = pause_layout.layoutParams as ViewGroup.MarginLayoutParams
        params.width = 0
        params.height = 0
        pause_layout.layoutParams = params
        jump_button.isEnabled = true
    }

    // -- draw endgame screen
    private fun onGameEnded(){
        val params = endgame_layout.layoutParams as ViewGroup.MarginLayoutParams
        params.width = width
        params.height = height
        endgame_layout.layoutParams = params
        game_layout.bringChildToFront(endgame_layout)
        jump_button.isEnabled = false
    }

    private fun onGameRestarted(){
        val params = endgame_layout.layoutParams as ViewGroup.MarginLayoutParams
        params.width = 0
        params.height = 0
        endgame_layout.layoutParams = params
        game_layout.bringChildToFront(endgame_layout)
        jump_button.isEnabled = true
        isGame = true
        isPause = false
    }

    // -- game loop body
    fun onGameUpdate(){
        // bird
        val param = bird.layoutParams as ViewGroup.MarginLayoutParams
        param.bottomMargin = BirdCoordinate.lootNewHeightPoint()
        bird.layoutParams = param
        // columns
        Columns.moveColumns()
        if(BirdCoordinate.isCollided()){
            isGame = false
            onGameEnded()
        }

        //score_view.text = (BirdCoordinate.lootScore()).toString() todo(make later)
        bird.rotation = BirdCoordinate.lootAngle()
        info = "${bird.rotation}"
        score_view.text = info
    }

    fun onGamePause(){

    }
}

object BirdCoordinate{
    private const val gravity = -0.00125f
    private const val jumpForce = 0.022f
    private var speed = jumpForce
    private var position = 0.5f // [0;1]
    private var birdCenter = 0
    private var windowHeight = 0
    private var score = 0
    private var isInColumn = false
    private var isAboveCenter = false

    // that's doing every tick
    fun lootNewHeightPoint(): Int {

        // oppressing new bird point
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

        // oppressing score if center of bird crossed center of current column
        val colCenter = Columns.lootCurrColumnCenter()
        if(colCenter == 0){
            isInColumn = false
        } else {
            if(!isInColumn){
                isInColumn = true
                isAboveCenter = false
            } else {
                if(!isAboveCenter && (colCenter < birdCenter)){
                    isAboveCenter = true
                    score++
                }
            }
        }

        info = "${speed * windowHeight}"

        return((windowHeight * 0.9f * position).toInt())
    }

    fun lootBirdScore(): Int {
        return score
    }

    fun oppressHeight(oppressedWindowHeight: Int){
        windowHeight = oppressedWindowHeight
    }

    fun oppressBirdCenter(oppressedCenter: Int){
        birdCenter = oppressedCenter
    }

    fun jump(){
        speed = jumpForce
    }

    fun reset(){
        speed = jumpForce
        position = 0.5f
        score = 0
    }

    fun isCollided(): Boolean{
        var isCollided = false
        if(Columns.isBirdBetween()){
            val hole = Columns.lootHoleSize()
            if(hole.upper <= windowHeight * (position * 0.9f + 0.1f) || hole.lower >= position * windowHeight * 0.9f){
                isCollided = true
            }
        }
        return(isCollided)
    }

    fun lootAngle(): Float {
        return (-atan((speed * windowHeight * 0.5f) / COLUMN_SPEED) * 180 / PI).toFloat()
    }
}

object Columns {

    // values of columns
    private const val moveSpeed = COLUMN_SPEED
    private const val columnsRate = 1.5 // bigger value - more columns
    private const val holeSize = 0.37f

    private var columnWidth: Int = 0

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    // контекст для создания imageView
    private lateinit var context: Context
    // layout на который добавим imageView
    private lateinit var layout: RelativeLayout
    // список имаджей
    private var columns = arrayListOf<Column>()

    // для дыры, номер колонны которая сейчас где птичка
    private var currColumn: Column? = null

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

        // down height
        params.topMargin = (params.height + screenHeight * holeSize).toInt()
        params.height = screenHeight - params.topMargin
        imageViewDown.layoutParams = params
        layout.addView(imageViewDown)
        val newColumn = Column(imageViewUp, imageViewDown)
        columns.add(newColumn)

    }

    fun moveColumns(){
        if(columns.isEmpty()){
            addColumn()
        }
        if (columns.last().upper.marginLeft <= (screenWidth - (screenHeight / columnsRate)).toInt()) {
            addColumn()
        }
        val colIterator = columns.iterator()
        for(col in colIterator){
            val paramsUpper = col.upper.layoutParams as ViewGroup.MarginLayoutParams
            val paramsLower = col.lower.layoutParams as ViewGroup.MarginLayoutParams
            paramsUpper.leftMargin -= moveSpeed
            paramsLower.leftMargin -= moveSpeed
            col.upper.layoutParams = paramsUpper
            col.lower.layoutParams = paramsLower
            if(col.upper.marginLeft <= -columnWidth){
                layout.removeView(col.upper)
                layout.removeView(col.lower)
                colIterator.remove()
            }
        }
    }

    fun reset(){
        val colIterator = columns.iterator()
        for(col in colIterator){
            layout.removeView(col.upper)
            layout.removeView(col.lower)
            colIterator.remove()
        }
    }

    fun isBirdBetween(): Boolean {
        var isBetween = false
        currColumn = null
        for(col in columns){
            if(!((col.upper.marginLeft > (screenHeight + screenWidth) * 0.1f) || (col.upper.marginLeft + columnWidth < screenWidth * 0.1f))){
                isBetween = true
                currColumn = col
                break
            }
        }
        return isBetween
    }

    fun lootHoleSize(): Hole {
        return if(currColumn != null){
            Hole(screenHeight - currColumn!!.upper.height, screenHeight - currColumn!!.lower.marginTop)
        } else {
            Hole(0, 0)
        }
    }

    fun lootCurrColumnCenter(): Int {
        return if(currColumn != null){
            currColumn!!.upper.marginLeft + (currColumn!!.upper.width / 2)
        } else {
            0
        }
    }
}