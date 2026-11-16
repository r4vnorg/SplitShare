package com.example.splitshare.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.domain.model.SyncStatus

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

        groupRepository.getPendingGroups().forEach { group ->
            val success = runCatching { syncService.syncGroup(group) }.getOrDefault(false)
            when {
                success -> {
                    groupRepository.setGroupSyncStatus(group.id, SyncStatus.SYNCED)
                    syncNotifier.notifySynced("Group synced", "“${group.name}” is now synced.")
                }
                runAttemptCount >= MAX_ATTEMPTS -> groupRepository.setGroupSyncStatus(group.id, SyncStatus.FAILED)
                else -> anyStillPending = true
            }
        }

        groupRepository.getPendingParticipantChanges().forEach { change ->
            val success = runCatching {
                syncService.syncParticipant(change.groupId, change.personId, change.removed)
            }.getOrDefault(false)
            when {
                success -> groupRepository.setParticipantSyncStatus(change.groupId, change.personId, SyncStatus.SYNCED)
                runAttemptCount >= MAX_ATTEMPTS ->
                    groupRepository.setParticipantSyncStatus(change.groupId, change.personId, SyncStatus.FAILED)
                else -> anyStillPending = true
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
