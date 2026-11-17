package com.example.splitshare.domain.model

/** An expense recorded in a group (US6), split among members (US7). Amount is in minor units (øre). */
data class Expense(
    val id: String,
    val groupId: String,
    val description: String,
    val amountMinor: Long,
    val payerId: String,
    val createdAt: Long,
    val syncStatus: SyncStatus,
    val splits: List<ExpenseSplit>,
)
