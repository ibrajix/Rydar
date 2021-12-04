package com.ibrajix.rydar.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.text.TextUtils

import android.provider.Settings.SettingNotFoundException

import android.os.Build
import android.location.LocationManager

object GeneralUtility {

    //transparent status bar
    fun Activity.transparentStatusBar() {
        val decor = window.decorView
        decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.TRANSPARENT
    }


    fun isGPSEnabled(mContext: Context): Boolean {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}