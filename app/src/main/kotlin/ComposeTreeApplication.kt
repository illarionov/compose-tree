package com.example.composetree

import android.app.Application

class ComposeTreeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApplicationComponent.initApplicationComponent(this)
    }
}
