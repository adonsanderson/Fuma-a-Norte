package com.example.fumacanorte

import android.app.Application
import com.example.fumacanorte.di.dataModule
import com.example.fumacanorte.di.databaseModule
import com.example.fumacanorte.di.domainModule
import com.example.fumacanorte.di.networkModule
import com.example.fumacanorte.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FumacaNorteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FumacaNorteApplication)
            modules(
                networkModule,
                databaseModule,
                dataModule,
                domainModule,
                presentationModule
            )
        }
    }
}
