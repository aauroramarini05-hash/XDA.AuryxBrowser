package com.xdustatom.auryxbrowser

import android.app.Application
import com.xdustatom.auryxbrowser.utils.PreferencesManager

class AuryxApplication : Application() {
    
    lateinit var preferencesManager: PreferencesManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesManager = PreferencesManager(this)
    }
    
    companion object {
        lateinit var instance: AuryxApplication
            private set
    }
}
