package com.example.splitshare.data

import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupDetailData
import com.example.splitshare.domain.model.GroupInfo
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.PendingParticipantChange
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.network.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    fun observeGroups(): Flow<List<GroupInfo>>

    fun observeGroup(groupId: String): Flow<Group?>


    fun observeDetailData(
        groupId: String,
        pollIntervalMillis: Long = 30_000,
    ): Flow<GroupDetailData>

    /** Active (non-removed) participants only. */
    fun observeParticipants(groupId: String): Flow<List<Participant>>

    /** Creates the group and auto-adds the device owner (from their Profile) as the first participant. */
    suspend fun createGroup(name: String, description: String)

    /** Adds [person] to the group, or reactivates them if they were previously removed. */
    suspend fun addParticipant(groupId: String, person: Person)

    /**
     * Soft-deletes the participant. The caller must already have verified their balance is zero
     * (AGENTS.md reliability design keeps that business rule in the ViewModel, not here).
     */
    suspend fun removeParticipant(groupId: String, personId: String)

    // Worker-only: background sync plumbing.
    suspend fun getPendingCreatedGroups(): List<Group>

    suspend fun replaceTemporaryGroup(old: Group, new: GroupInfo)

    suspend fun setGroupSyncStatus(groupId: String, status: SyncStatus)
    suspend fun getPendingParticipantChanges(): List<PendingParticipantChange>
    suspend fun setParticipantSyncStatus(groupId: String, personId: String, status: SyncStatus)
}
