package com.example.splitshare.data

import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupDetailData
import com.example.splitshare.domain.model.GroupSummary
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.PendingParticipantChange
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.network.NetworkBoundResource
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    fun observeGroups(): Flow<List<GroupSummary>>

    fun observeGroup(groupId: String): Flow<Group?>

    /**
     * Local-first flow with automatic network polling for the full group detail.
     *
     * Combines group, participants, and expenses into a single [GroupDetailData] flow.
     * The group is fetched from the network with automatic retries and polled every
     * [pollIntervalMillis]. Participants and expenses are read from the local cache
     * (updated by their own [ExpenseRepository.observeWithSync] or local writes).
     *
     * The UI sees [NetworkBoundResource.Success] with fresh group data on a successful
     * fetch, or [NetworkBoundResource.Stale] with cached data when the network is unavailable.
     */
    fun observeDetailData(
        groupId: String,
        pollIntervalMillis: Long = 30_000,
    ): Flow<GroupDetailData>

    /** Active (non-removed) participants only. */
    fun observeParticipants(groupId: String): Flow<List<Participant>>

    /** Creates the group and auto-adds the device owner (from their Profile) as the first participant. */
    suspend fun createGroup(name: String, description: String): Group

    /** Adds [person] to the group, or reactivates them if they were previously removed. */
    suspend fun addParticipant(groupId: String, person: Person)

    /**
     * Soft-deletes the participant. The caller must already have verified their balance is zero
     * (AGENTS.md reliability design keeps that business rule in the ViewModel, not here).
     */
    suspend fun removeParticipant(groupId: String, personId: String)

    // Worker-only: background sync plumbing.
    suspend fun getPendingGroups(): List<Group>
    suspend fun setGroupSyncStatus(groupId: String, status: SyncStatus)
    suspend fun getPendingParticipantChanges(): List<PendingParticipantChange>
    suspend fun setParticipantSyncStatus(groupId: String, personId: String, status: SyncStatus)
}
