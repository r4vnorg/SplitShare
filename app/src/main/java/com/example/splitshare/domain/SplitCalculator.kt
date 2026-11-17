package com.example.splitshare.domain

/** Computes equal expense splits (US7). Pure, deterministic, works in minor units (øre). */
object SplitCalculator {

    /**
     * Splits [totalMinor] evenly across [memberIds]. When it doesn't divide evenly, the first
     * `totalMinor % memberIds.size` members (in the given order) get one extra øre, so the
     * returned shares always sum exactly to [totalMinor].
     */
    fun equalSplit(totalMinor: Long, memberIds: List<String>): Map<String, Long> {
        require(memberIds.isNotEmpty()) { "At least one member is required to split an expense" }
        val memberCount = memberIds.size
        val base = totalMinor / memberCount
        val remainder = (totalMinor % memberCount).toInt()
        return memberIds.mapIndexed { index, personId ->
            personId to (base + if (index < remainder) 1 else 0)
        }.toMap()
    }
}
