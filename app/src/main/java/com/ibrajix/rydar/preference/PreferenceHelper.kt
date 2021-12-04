package com.ibrajix.rydar.preference

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {

    private const val NAME = "session"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    //SharedPreferences variables
    private val hasSeenIntro = Pair("hasSeenIntro", false)


    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    //an inline function to put variable and save it
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    //check if buyer has seen intro
    var hasUserSeenIntro: Boolean
        get() = preferences.getBoolean(hasSeenIntro.first, hasSeenIntro.second)
        set(value) = preferences.edit {
            it.putBoolean(hasSeenIntro.first, value)
        }

}