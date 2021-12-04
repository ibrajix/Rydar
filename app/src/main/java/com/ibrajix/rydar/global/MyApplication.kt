package com.ibrajix.rydar.global

import android.app.Application
import com.ibrajix.rydar.preference.PreferenceHelper

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(this)
    }

}