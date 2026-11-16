package com.example.splitshare.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GroupValidatorTest {

    @Test
    fun blankNameIsRejected() {
        assertEquals("Name is required", GroupValidator.nameError("   "))
    }

    @Test
    fun nonBlankNameIsAccepted() {
        assertNull(GroupValidator.nameError("Cabin trip"))
    }

    @Test
    fun nameOverSixtyCharsIsRejected() {
        assertEquals(
            "Name must be 60 characters or fewer",
            GroupValidator.nameError("a".repeat(61)),
        )
    }

    @Test
    fun emptyDescriptionIsAccepted() {
        assertNull(GroupValidator.descriptionError(""))
    }

    @Test
    fun descriptionOverTwoEightyCharsIsRejected() {
        assertEquals(
            "Description must be 280 characters or fewer",
            GroupValidator.descriptionError("a".repeat(281)),
        )
    }
}
