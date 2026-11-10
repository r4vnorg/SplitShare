package com.example.splitshare.domain

/** Validation rules for profile input. Returns an error message per field, or null when valid. */
object ProfileValidator {

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val phoneRegex = Regex("^\\+?[0-9 ]{8,15}$")

    fun nameError(name: String): String? =
        if (name.isBlank()) "Name is required" else null

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !emailRegex.matches(email.trim()) -> "Enter a valid email"
        else -> null
    }

    /** Phone is optional, but must look like a phone number if given. */
    fun phoneError(phone: String): String? =
        if (phone.isNotBlank() && !phoneRegex.matches(phone.trim())) "Enter a valid phone number" else null
}
