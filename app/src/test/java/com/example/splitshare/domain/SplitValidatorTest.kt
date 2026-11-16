package com.example.splitshare.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SplitValidatorTest {

    @Test
    fun emptySelectionIsRejected() {
        assertEquals("Select at least one member", SplitValidator.selectionError(emptyList()))
    }

    @Test
    fun nonEmptySelectionIsAccepted() {
        assertNull(SplitValidator.selectionError(listOf("a")))
    }

    @Test
    fun emptySharesAreRejected() {
        assertEquals("Select at least one member", SplitValidator.customSplitError(100L, emptyMap()))
    }

    @Test
    fun negativeShareIsRejected() {
        assertEquals(
            "Shares cannot be negative",
            SplitValidator.customSplitError(100L, mapOf("a" to -10L, "b" to 110L)),
        )
    }

    @Test
    fun sharesNotSummingToTotalAreRejected() {
        assertEquals(
            "Shares must add up to the total amount",
            SplitValidator.customSplitError(100L, mapOf("a" to 40L, "b" to 40L)),
        )
    }

    @Test
    fun sharesSummingToTotalAreAccepted() {
        assertNull(SplitValidator.customSplitError(100L, mapOf("a" to 60L, "b" to 40L)))
    }
}
