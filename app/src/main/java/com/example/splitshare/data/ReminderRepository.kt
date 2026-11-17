package com.example.splitshare.data

import com.example.splitshare.data.sync.SyncScheduler

/** US12: asks for a payment reminder to be delivered to a group member who owes money. */
interface ReminderRepository {
    suspend fun sendReminder(groupId: String, personId: String, personName: String, amountMinor: Long)
}

/** Queues the reminder for background delivery (local-first, retried with backoff) via [SyncScheduler]. */
class SchedulingReminderRepository(private val syncScheduler: SyncScheduler) : ReminderRepository {
    override suspend fun sendReminder(groupId: String, personId: String, personName: String, amountMinor: Long) {
        syncScheduler.scheduleReminder(groupId, personId, personName, amountMinor)
    }
}
