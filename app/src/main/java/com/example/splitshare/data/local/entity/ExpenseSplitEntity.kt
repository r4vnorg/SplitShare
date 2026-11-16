package com.example.splitshare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** One member's share of an expense. Rows for one expenseId sum to that expense's amountMinor. */
@Entity(
    tableName = "expense_splits",
    primaryKeys = ["expenseId", "personId"],
    foreignKeys = [
        ForeignKey(entity = ExpenseEntity::class, parentColumns = ["id"], childColumns = ["expenseId"]),
        ForeignKey(entity = PersonEntity::class, parentColumns = ["id"], childColumns = ["personId"]),
    ],
    indices = [Index("personId")],
)
data class ExpenseSplitEntity(
    val expenseId: String,
    val personId: String,
    val shareMinor: Long,
)
