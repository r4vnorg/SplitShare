package com.example.splitshare.domain

/** Validation rules for expense input (US6). Returns an error message per field, or null when valid. */
object ExpenseValidator {

    private const val MAX_DESCRIPTION_LENGTH = 140

    /** Description is optional (US6 only requires payer + amount), but still bounded. */
    fun descriptionError(description: String): String? =
        if (description.trim().length > MAX_DESCRIPTION_LENGTH) {
            "Description must be $MAX_DESCRIPTION_LENGTH characters or fewer"
        } else {
            null
        }

    fun amountError(amountMinor: Long?): String? = when {
        amountMinor == null -> "Enter an amount"
        amountMinor <= 0 -> "Amount must be greater than zero"
        else -> null
    }

    fun payerError(payerId: String?): String? =
        if (payerId.isNullOrBlank()) "Select who paid" else null
}
