package com.chomusukestudio.projectrocketc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.chomusukestudio.projectrocketc.Surrounding.BasicSurrounding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        heightInPixel = displayMetrics.heightPixels.toFloat()
        widthInPixel = displayMetrics.widthPixels.toFloat()

        val chomusukeView = findViewById<ImageView>(R.id.chomusukeView)
        chomusukeView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_splash_image))

        Executors.newSingleThreadExecutor().submit {
            BasicSurrounding.fillUpPlanetShapes()

            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}