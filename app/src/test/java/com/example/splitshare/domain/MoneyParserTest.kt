package com.example.splitshare.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyParserTest {

    @Test
    fun parsesWholeNumber() {
        assertEquals(500L, MoneyParser.parseToMinorUnits("5"))
    }

    @Test
    fun parsesDecimalWithDot() {
        assertEquals(1234L, MoneyParser.parseToMinorUnits("12.34"))
    }

    @Test
    fun parsesDecimalWithComma() {
        assertEquals(1234L, MoneyParser.parseToMinorUnits("12,34"))
    }

    @Test
    fun parsesSingleDecimalDigitAsTensOfCents() {
        assertEquals(1230L, MoneyParser.parseToMinorUnits("12.3"))
    }

    @Test
    fun rejectsBlank() {
        assertNull(MoneyParser.parseToMinorUnits(""))
    }

    @Test
    fun rejectsNegative() {
        assertNull(MoneyParser.parseToMinorUnits("-5"))
    }

    @Test
    fun rejectsTooManyDecimalDigits() {
        assertNull(MoneyParser.parseToMinorUnits("12.345"))
    }

    @Test
    fun rejectsNonNumeric() {
        assertNull(MoneyParser.parseToMinorUnits("abc"))
    }
}
