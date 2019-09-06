package com.b3.development.b3runtime

import android.app.Application
import com.b3.development.b3runtime.di.b3RuntimeModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class B3RuntimeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@B3RuntimeApp)
            modules(b3RuntimeModule)
        }
    }
}