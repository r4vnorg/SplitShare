package com.example.splitshare.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.data.local.GroupParticipantDao
import com.example.splitshare.data.local.toEntity
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupInfo
import com.example.splitshare.domain.model.ParticipantWithPerson
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.network.NetworkStatus

/**
 * Sends every locally-queued (PENDING) write through [syncService]. Only ever processes PENDING
 * rows, never FAILED ones — that's what distinguishes "still auto-retrying" from "gave up, needs
 * a manual Retry" (AGENTS.md: background retry + feedback). Processes parents before children
 * (groups, then participants/expenses) since a real backend would need the group to exist first.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val syncService: SyncService,
    private val syncNotifier: SyncNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        var anyStillPending = false

        groupRepository.getPendingCreatedGroups().forEach { group ->
            when (val res = syncService.createGroup(group)) {
                is NetworkStatus.Success<GroupInfo> -> {
                    groupRepository.replaceTemporaryGroup(group, res.data)
                    syncNotifier.notifySynced("Group synced", "“${group.name}” is now synced.")
                }
                is NetworkStatus.Error ->
                    groupRepository.setGroupSyncStatus(group, SyncStatus.FAILED)
                is NetworkStatus.Pending ->
                    anyStillPending = true
            }
        }

        groupRepository.getPendingParticipantAdditions().forEach { change ->
            val res = syncService.addParticipant(
                groupId = change.participant.groupId,
                email = change.person.email
            )
            when (res) {
                is NetworkStatus.Success<ParticipantWithPerson> -> {
                    groupRepository.updateTemporaryParticipant(change.participant, res.data)
                    //syncNotifier.notifySynced("Group synced", "“${group.name}” is now synced.")
                }
                is NetworkStatus.Error ->
                    groupRepository.setParticipantSyncStatus(change.participant, SyncStatus.FAILED)
                is NetworkStatus.Pending ->
                    anyStillPending = true
            }
        }

        expenseRepository.getPendingExpenses().forEach { expense ->
            val success = runCatching { syncService.syncExpense(expense) }.getOrDefault(false)
            when {
                success -> {
                    expenseRepository.setExpenseSyncStatus(expense.id, SyncStatus.SYNCED)
                    syncNotifier.notifySynced("Expense synced", "“${expense.description}” is now synced.")
                }
                runAttemptCount >= MAX_ATTEMPTS -> expenseRepository.setExpenseSyncStatus(expense.id, SyncStatus.FAILED)
                else -> anyStillPending = true
            }
        }

        return if (anyStillPending) Result.retry() else Result.success()
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
