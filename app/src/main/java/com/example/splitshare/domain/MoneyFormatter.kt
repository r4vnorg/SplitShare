package com.example.splitshare.domain

import kotlin.math.abs

/** Formats minor units (øre) for display, e.g. 12345 -> "123,45 kr". Never uses Double. */
object MoneyFormatter {

    fun format(amountMinor: Long): String {
        val sign = if (amountMinor < 0) "-" else ""
        val absMinor = abs(amountMinor)
        val whole = absMinor / 100
        val cents = absMinor % 100
        return "$sign$whole,${cents.toString().padStart(2, '0')} kr"
    }

    /** Non-negative amount as plain "123.45" (dot separator, no "kr") - round-trips through [MoneyParser]. */
    fun formatPlain(amountMinor: Long): String {
        val whole = amountMinor / 100
        val cents = amountMinor % 100
        return "$whole.${cents.toString().padStart(2, '0')}"
    }
}
