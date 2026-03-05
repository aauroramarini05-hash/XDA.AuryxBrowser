package com.xdustatom.auryxbrowser

import android.app.Application
import android.content.Context
import com.xdustatom.auryxbrowser.utils.LocaleHelper
import com.xdustatom.auryxbrowser.utils.PreferencesManager

class AuryxApplication : Application() {
    
    lateinit var preferencesManager: PreferencesManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesManager = PreferencesManager(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(base))
    }
    
    companion object {
        lateinit var instance: AuryxApplication
            private set
    }
}
