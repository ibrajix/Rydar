package com.ibrajix.rydar.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.ibrajix.rydar.databinding.ActivitySplashScreenBinding
import com.ibrajix.rydar.preference.PreferenceHelper
import com.ibrajix.rydar.utils.Constants.SPLASH_SCREEN_TIME


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private var activityIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hide the status bar
        @Suppress("DEPRECATION")
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView(){
        startAnimation()
        goToNextActivity()
    }

    private fun startAnimation(){

        YoYo.with(Techniques.Landing)
            .duration(3000)
            .repeat(0)
            .playOn(binding.txtAppName)
    }

    private fun goToNextActivity(){

        activityIntent = if (!PreferenceHelper.hasUserSeenIntro){
            Intent(this, IntroActivity::class.java)
        } else {
            Intent(this, ContainerActivity::class.java)
        }


        //delay for 5 seconds and move to next activity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(activityIntent)
            finish()}
            , SPLASH_SCREEN_TIME)

    }

}