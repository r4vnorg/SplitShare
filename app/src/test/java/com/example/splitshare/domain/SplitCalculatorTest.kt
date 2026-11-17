package com.example.splitshare.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun evenlyDivisibleAmountSplitsEqually() {
        val result = SplitCalculator.equalSplit(300L, listOf("a", "b", "c"))
        assertEquals(mapOf("a" to 100L, "b" to 100L, "c" to 100L), result)
    }

    @Test
    fun remainderGoesToFirstMembersInOrder() {
        val result = SplitCalculator.equalSplit(100L, listOf("a", "b", "c"))
        // 100 / 3 = 33 remainder 1 -> first member gets the extra øre
        assertEquals(mapOf("a" to 34L, "b" to 33L, "c" to 33L), result)
        assertEquals(100L, result.values.sum())
    }

    @Test
    fun singleMemberGetsTheWholeAmount() {
        val result = SplitCalculator.equalSplit(777L, listOf("a"))
        assertEquals(mapOf("a" to 777L), result)
    }

    @Test
    fun emptyMemberListThrows() {
        assertThrows(IllegalArgumentException::class.java) {
            SplitCalculator.equalSplit(100L, emptyList())
        }
    }
}
