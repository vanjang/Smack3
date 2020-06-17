package com.cochipcho.smack3.Controller

import android.app.Application
import com.cochipcho.smack3.Utilities.SharedPrefs

class App: Application() {

    companion object { // singleton inside specific class
        lateinit var prefs: SharedPrefs
    }


    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)

        super.onCreate()
    }

}