package com.example.splitshare.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/** US12: delivers one queued reminder, retrying with backoff, and tells the user the outcome (NFR-K3). */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val syncService: SyncService,
    private val syncNotifier: SyncNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val groupId = inputData.getString(KEY_GROUP_ID) ?: return Result.failure()
        val personId = inputData.getString(KEY_PERSON_ID) ?: return Result.failure()
        val name = inputData.getString(KEY_PERSON_NAME).orEmpty()
        val amountMinor = inputData.getLong(KEY_AMOUNT, 0L)

        val success = runCatching { syncService.sendReminder(groupId, personId, amountMinor) }.getOrDefault(false)
        return when {
            success -> {
                syncNotifier.notifySynced("Reminder sent", "$name was reminded.")
                Result.success()
            }
            runAttemptCount >= MAX_ATTEMPTS -> {
                syncNotifier.notifySynced("Reminder failed", "Could not remind $name. Open the group and try again.")
                Result.failure()
            }
            else -> Result.retry()
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 5
        private const val KEY_GROUP_ID = "groupId"
        private const val KEY_PERSON_ID = "personId"
        private const val KEY_PERSON_NAME = "personName"
        private const val KEY_AMOUNT = "amountMinor"

        fun inputData(groupId: String, personId: String, personName: String, amountMinor: Long): Data = workDataOf(
            KEY_GROUP_ID to groupId,
            KEY_PERSON_ID to personId,
            KEY_PERSON_NAME to personName,
            KEY_AMOUNT to amountMinor,
        )
    }
}
