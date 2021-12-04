package com.ibrajix.rydar.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ibrajix.rydar.databinding.ActivityIntroBinding
import com.ibrajix.rydar.preference.PreferenceHelper
import com.ibrajix.rydar.utils.GeneralUtility.transparentStatusBar

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar()
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView(){
        //has seen this activity
        PreferenceHelper.hasUserSeenIntro = true
        handleClicks()
    }

    private fun handleClicks(){

        //on click button get started
        binding.btnGetStarted.setOnClickListener {
            val intent = Intent(this, ContainerActivity::class.java)
            startActivity(intent)
        }


    }

}