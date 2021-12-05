package com.ibrajix.rydar.global

import android.app.Application
import com.ibrajix.rydar.preference.PreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(this)
    }
}