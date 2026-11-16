package com.example.splitshare.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun formatsWholeKroner() {
        assertEquals("5,00 kr", MoneyFormatter.format(500L))
    }

    @Test
    fun formatsSingleDigitCentsWithLeadingZero() {
        assertEquals("1,05 kr", MoneyFormatter.format(105L))
    }

    @Test
    fun formatsZero() {
        assertEquals("0,00 kr", MoneyFormatter.format(0L))
    }

    @Test
    fun formatsNegativeAmounts() {
        assertEquals("-5,00 kr", MoneyFormatter.format(-500L))
    }
}
