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

    /**
     * US12: who owes [currentPersonId] money. Empty unless the current user is a net creditor;
     * otherwise every other member with a negative net balance, mapped to the amount they owe.
     * Group-level, not pairwise (pairwise "who owes whom" is US9's settlement view).
     */
    fun debtorsOf(currentPersonId: String, memberIds: List<String>, expenses: List<Expense>): Map<String, Long> {
        if (netBalance(currentPersonId, expenses) <= 0L) return emptyMap()
        return memberIds
            .filter { it != currentPersonId }
            .associateWith { netBalance(it, expenses) }
            .filterValues { it < 0L }
            .mapValues { -it.value }
    }
}
