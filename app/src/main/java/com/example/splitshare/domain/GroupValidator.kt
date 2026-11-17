package com.example.splitshare.domain

/** Validation rules for group creation input (US4). Returns an error message per field, or null when valid. */
object GroupValidator {

    private const val MAX_NAME_LENGTH = 60
    private const val MAX_DESCRIPTION_LENGTH = 280

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Name is required"
        name.trim().length > MAX_NAME_LENGTH -> "Name must be $MAX_NAME_LENGTH characters or fewer"
        else -> null
    }

    /** Description is optional, but still bounded. */
    fun descriptionError(description: String): String? =
        if (description.trim().length > MAX_DESCRIPTION_LENGTH) {
            "Description must be $MAX_DESCRIPTION_LENGTH characters or fewer"
        } else {
            null
        }
}
