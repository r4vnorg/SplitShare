package com.example.splitshare.domain

/** Parses user-entered amounts (kroner, comma or dot decimal separator) into minor units (øre). */
object MoneyParser {

    private val PATTERN = Regex("^\\d+([.,]\\d{1,2})?$")

    /** Returns null if [text] isn't a valid non-negative amount with at most 2 decimal places. */
    fun parseToMinorUnits(text: String): Long? {
        val trimmed = text.trim()
        if (!PATTERN.matches(trimmed)) return null

        val normalized = trimmed.replace(',', '.')
        val parts = normalized.split(".")
        val whole = parts[0].toLongOrNull() ?: return null
        val cents = parts.getOrNull(1)?.padEnd(2, '0')?.toLongOrNull() ?: 0L
        return whole * 100 + cents
    }
}
