package com.example.neirocombain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val logo_LO = findViewById<LinearLayout>(R.id.Logo_lay)
        logo_LO.alpha = 0f
        logo_LO.animate().setDuration(1500).alpha(1f).withEndAction {
            val i = Intent(this, MainActivity::class.java)
            this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(i)
            finish()


        }
    }
}