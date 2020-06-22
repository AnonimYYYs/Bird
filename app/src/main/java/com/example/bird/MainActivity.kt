package com.example.bird

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_shop.*

class MainActivity : AppCompatActivity() {

    private var totalScore = 12

    // code for getting result of new score from game intent
    private val newScoreRequestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        score_text.text = "Total score: $totalScore"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == newScoreRequestCode){
            if (data != null) {
                if(resultCode == Activity.RESULT_OK){
                    totalScore = data.getIntExtra("newScore", 0)
                }
            }
        }

        score_text.text = "Total score: $totalScore"
    }

    fun launchGame(view: View) {
        val gameIntent = Intent(this, GameActivity::class.java)
        gameIntent.putExtra("topScore", totalScore)
        startActivityForResult(gameIntent, newScoreRequestCode)
    }

    fun goToShop(view: View) {
        val shopIntent = Intent(this, ShopActivity::class.java)
        shopIntent.putExtra("allMoney", totalScore)
        startActivityForResult(shopIntent, newScoreRequestCode)
    }
}

class ShopActivity : AppCompatActivity() {

    var money = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        money = intent.extras?.getInt("allMoney") ?: 0
        upd8money()
    }

    override fun finish() {
        val answerIntent = Intent()
        answerIntent.putExtra("newScore", money)
        setResult(Activity.RESULT_OK, answerIntent)
        super.finish()
    }

    private fun upd8money(){
        coins_text.text = "money: $money"
    }

    fun spendOne(view: View) {
        if(money >= 1){
            money -= 1
        }
        upd8money()
    }

    fun spendTen(view: View) {
        if(money >= 10){
            money -= 10
        }
        upd8money()
    }
}