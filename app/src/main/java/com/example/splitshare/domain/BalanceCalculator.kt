package com.example.splitshare.domain

import com.example.splitshare.domain.model.Expense

/**
 * Computes a person's net balance within a group (needed now by US5's remove-participant rule:
 * a member can only be removed directly when their balance is zero; the Balances tab itself is
 * a later story, but the underlying calculation already has to exist).
 */
object BalanceCalculator {

    /** Positive = the group owes [personId]; negative = they owe the group; zero = settled up. */
    fun netBalance(personId: String, expenses: List<Expense>): Long {
        var balance = 0L
        for (expense in expenses) {
            if (expense.payerId == personId) {
                balance += expense.amountMinor
            }
            balance -= expense.splits.firstOrNull { it.personId == personId }?.shareMinor ?: 0L
        }
        return balance
    }
}
