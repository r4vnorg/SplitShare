package com.example.splitshare.domain

import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BalanceCalculatorTest {

    private fun expense(payerId: String, amountMinor: Long, shares: Map<String, Long>) = Expense(
        id = "expense-${payerId}-$amountMinor",
        groupId = "group-1",
        description = "test",
        amountMinor = amountMinor,
        payerId = payerId,
        createdAt = 0L,
        syncStatus = SyncStatus.PENDING,
        splits = shares.map { (personId, shareMinor) -> ExpenseSplit(personId, shareMinor) },
    )

    @Test
    fun payerWhoDoesNotOweIsOwedTheFullAmount() {
        val expenses = listOf(expense(payerId = "alice", amountMinor = 300L, shares = mapOf("bob" to 300L)))
        assertEquals(300L, BalanceCalculator.netBalance("alice", expenses))
    }

    @Test
    fun personWhoOwesAndDidNotPayHasNegativeBalance() {
        val expenses = listOf(expense(payerId = "alice", amountMinor = 300L, shares = mapOf("bob" to 300L)))
        assertEquals(-300L, BalanceCalculator.netBalance("bob", expenses))
    }

    @Test
    fun payerWhoAlsoOwesTheirOwnShareNetsOutPartially() {
        val expenses = listOf(
            expense(payerId = "alice", amountMinor = 300L, shares = mapOf("alice" to 100L, "bob" to 200L))
        )
        assertEquals(200L, BalanceCalculator.netBalance("alice", expenses))
    }

    @Test
    fun multipleExpensesNetToZeroWhenSettledUp() {
        val expenses = listOf(
            expense(payerId = "alice", amountMinor = 200L, shares = mapOf("alice" to 100L, "bob" to 100L)),
            expense(payerId = "bob", amountMinor = 200L, shares = mapOf("alice" to 100L, "bob" to 100L)),
        )
        assertEquals(0L, BalanceCalculator.netBalance("alice", expenses))
        assertEquals(0L, BalanceCalculator.netBalance("bob", expenses))
    }

    @Test
    fun personNotInvolvedInAnyExpenseHasZeroBalance() {
        val expenses = listOf(expense(payerId = "alice", amountMinor = 300L, shares = mapOf("bob" to 300L)))
        assertEquals(0L, BalanceCalculator.netBalance("carol", expenses))
    }
}
