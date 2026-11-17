package com.example.splitshare.data

/** Records reminders instead of scheduling WorkManager jobs. */
class FakeReminderRepository : ReminderRepository {

    data class Sent(val groupId: String, val personId: String, val personName: String, val amountMinor: Long)

    val sent = mutableListOf<Sent>()

    override suspend fun sendReminder(groupId: String, personId: String, personName: String, amountMinor: Long) {
        sent += Sent(groupId, personId, personName, amountMinor)
    }
}
