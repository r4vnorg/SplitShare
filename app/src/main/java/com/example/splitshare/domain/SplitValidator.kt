package com.example.splitshare.domain

/** Validation rules for choosing how to split an expense (US7). */
object SplitValidator {

    fun selectionError(selectedIds: List<String>): String? =
        if (selectedIds.isEmpty()) "Select at least one member" else null

    /** Custom per-member shares must be non-negative and sum exactly to the expense total. */
    fun customSplitError(totalMinor: Long, shares: Map<String, Long>): String? = when {
        shares.isEmpty() -> "Select at least one member"
        shares.values.any { it < 0 } -> "Shares cannot be negative"
        shares.values.sum() != totalMinor -> "Shares must add up to the total amount"
        else -> null
    }
}
