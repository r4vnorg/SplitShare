package com.example.splitshare.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExpenseValidatorTest {

    @Test
    fun emptyDescriptionIsAccepted() {
        assertNull(ExpenseValidator.descriptionError(""))
    }

    @Test
    fun descriptionOverLimitIsRejected() {
        assertEquals(
            "Description must be 140 characters or fewer",
            ExpenseValidator.descriptionError("a".repeat(141)),
        )
    }

    @Test
    fun nullAmountIsRejected() {
        assertEquals("Enter an amount", ExpenseValidator.amountError(null))
    }

    @Test
    fun zeroAmountIsRejected() {
        assertEquals("Amount must be greater than zero", ExpenseValidator.amountError(0L))
    }

    @Test
    fun negativeAmountIsRejected() {
        assertEquals("Amount must be greater than zero", ExpenseValidator.amountError(-100L))
    }

    @Test
    fun positiveAmountIsAccepted() {
        assertNull(ExpenseValidator.amountError(500L))
    }

    @Test
    fun blankPayerIsRejected() {
        assertEquals("Select who paid", ExpenseValidator.payerError(null))
        assertEquals("Select who paid", ExpenseValidator.payerError(""))
    }

    @Test
    fun nonBlankPayerIsAccepted() {
        assertNull(ExpenseValidator.payerError("person-1"))
    }
}
