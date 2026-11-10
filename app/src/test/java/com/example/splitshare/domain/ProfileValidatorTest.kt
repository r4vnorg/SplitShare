package com.example.splitshare.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidatorTest {

    @Test
    fun blankNameIsRejected() {
        assertEquals("Name is required", ProfileValidator.nameError("   "))
    }

    @Test
    fun nonBlankNameIsAccepted() {
        assertNull(ProfileValidator.nameError("Ali"))
    }

    @Test
    fun blankEmailIsRejected() {
        assertEquals("Email is required", ProfileValidator.emailError(""))
    }

    @Test
    fun malformedEmailIsRejected() {
        assertNotNull(ProfileValidator.emailError("ali@sdu"))
        assertNotNull(ProfileValidator.emailError("ali.sdu.dk"))
    }

    @Test
    fun validEmailWithSurroundingSpacesIsAccepted() {
        assertNull(ProfileValidator.emailError("  ali@student.sdu.dk "))
    }

    @Test
    fun emptyPhoneIsAccepted() {
        assertNull(ProfileValidator.phoneError(""))
    }

    @Test
    fun validPhoneIsAccepted() {
        assertNull(ProfileValidator.phoneError("+45 12 34 56 78"))
    }

    @Test
    fun malformedPhoneIsRejected() {
        assertNotNull(ProfileValidator.phoneError("12ab"))
    }
}
