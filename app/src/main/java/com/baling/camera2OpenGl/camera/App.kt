package com.baling.camera2OpenGl.camera

import android.app.Application

class App : Application() {
    companion object {
        lateinit var mApp: App
        fun getInstance(): App {
            return mApp
        }
    }

    override fun onCreate() {
        super.onCreate()
        mApp = this
    }
}