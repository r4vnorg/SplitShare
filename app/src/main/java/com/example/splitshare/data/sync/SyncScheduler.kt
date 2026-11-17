package com.example.splitshare.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules [SyncWorker] runs. Lives in `data`, not `ui` (AGENTS.md: ui never touches WorkManager directly). */
class SyncScheduler(private val context: Context) {

    /** Call after any local write. Coalesces a burst of writes into one chain via APPEND_OR_REPLACE. */
    fun scheduleSync() {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, buildRequest())
    }

    /** Manual retry (Expense Detail's Retry button): bypasses any pending backoff wait. */
    fun retryNow() {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, buildRequest())
    }

    /**
     * US12: queues one reminder. WorkManager persists it (offline-safe) and retries with backoff.
     * KEEP makes a repeated tap on the same debtor a no-op while the first reminder is still queued.
     */
    fun scheduleReminder(groupId: String, personId: String, personName: String, amountMinor: Long) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(ReminderWorker.inputData(groupId, personId, personName, amountMinor))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("reminder_${groupId}_$personId", ExistingWorkPolicy.KEEP, request)
    }

    private fun buildRequest(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

    private companion object {
        const val UNIQUE_WORK_NAME = "sync_pending_changes"
    }
}
