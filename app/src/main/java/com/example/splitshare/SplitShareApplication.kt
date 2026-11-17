package com.example.splitshare

import android.app.Application
import androidx.work.Configuration

class SplitShareApplication : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(SplitShareWorkerFactory(container))
            .build()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.syncNotifier.createChannel()
    }
}
