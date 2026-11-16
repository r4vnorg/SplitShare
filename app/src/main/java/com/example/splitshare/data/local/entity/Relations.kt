package com.example.splitshare.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.splitshare.domain.model.SyncStatus

/** A group participant joined with their [PersonEntity] details, for Manage Members / balance checks. */
data class ParticipantWithPerson(
    @Embedded val participant: GroupParticipantEntity,
    @Relation(parentColumn = "personId", entityColumn = "id") val person: PersonEntity,
)

/** An expense joined with its splits, for the Transactions tab / Expense Detail / balance checks. */
data class ExpenseWithSplits(
    @Embedded val expense: ExpenseEntity,
    @Relation(parentColumn = "id", entityColumn = "expenseId") val splits: List<ExpenseSplitEntity>,
)

/** Group List row: group fields plus a computed active-participant count. */
data class GroupWithParticipantCount(
    val id: String,
    val name: String,
    val description: String,
    val createdAt: Long,
    val syncStatus: SyncStatus,
    val participantCount: Int,
)
