package com.example.splitshare.domain.model

/** Combined data for the Group Detail screen. All fields come from the local cache. */
data class GroupDetailData(
    val group: Group?,
    val participants: List<ParticipantWithPerson>,
    val expenses: List<Expense>,
)
