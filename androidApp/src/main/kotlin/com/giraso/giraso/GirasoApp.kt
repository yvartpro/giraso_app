package com.giraso.giraso

import android.app.Application
import com.giraso.giraso.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GirasoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@GirasoApp)
            modules(appModule)
        }
    }
}
