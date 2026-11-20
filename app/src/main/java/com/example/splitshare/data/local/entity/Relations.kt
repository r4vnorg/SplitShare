package com.example.splitshare.data.local.entity

import android.app.Person
import androidx.room.Embedded
import androidx.room.Relation
import com.example.splitshare.domain.model.Participant
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

/** Flat row for a group joined with one active participant and their person details. */
data class GroupEntityWithParticipants(
    @Embedded val group: GroupEntity,
    @Relation(parentColumn = "id", entityColumn = "groupId") val participants: List<GroupParticipantEntity>,
)

data class UserWithPerson(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val profile: PersonEntity   // single object = one-to-one
)