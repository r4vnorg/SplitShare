package com.example.splitshare.domain.model

/** One member's share of an expense (US7), in minor units (øre). */
data class ExpenseSplit(
    val personId: String,
    val shareMinor: Long,
)
