package com.example.splitshare

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.splitshare.data.sync.SyncWorker

/** No Hilt in this repo (manual DI, see AppContainer) - hands workers their dependencies by hand. */
class SplitShareWorkerFactory(private val container: AppContainer) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        SyncWorker::class.java.name -> SyncWorker(
            appContext,
            workerParameters,
            container.groupRepository,
            container.expenseRepository,
            container.syncService,
            container.syncNotifier,
        )
        else -> null
    }
}
