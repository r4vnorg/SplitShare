package com.example.splitshare.data

import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupSummary
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.PendingParticipantChange
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/** In-memory test double for ViewModel tests; avoids needing a real Room database. */
class FakeGroupRepository : GroupRepository {

    val createdGroups = mutableListOf<Group>()

    private val groupsFlow = MutableStateFlow<List<GroupSummary>>(emptyList())

    fun emitGroups(groups: List<GroupSummary>) {
        groupsFlow.value = groups
    }

    override fun observeGroups(): Flow<List<GroupSummary>> = groupsFlow

    override fun observeGroup(groupId: String): Flow<Group?> = flowOf(null)

    override fun observeParticipants(groupId: String): Flow<List<Participant>> = flowOf(emptyList())

    override suspend fun createGroup(name: String, description: String): Group {
        val group = Group(id = "group-${createdGroups.size}", name = name, description = description, createdAt = 0L)
        createdGroups += group
        return group
    }

    override suspend fun addParticipant(groupId: String, person: Person) = Unit

    override suspend fun removeParticipant(groupId: String, personId: String) = Unit

    override suspend fun getPendingGroups(): List<Group> = emptyList()

    override suspend fun setGroupSyncStatus(groupId: String, status: SyncStatus) = Unit

    override suspend fun getPendingParticipantChanges(): List<PendingParticipantChange> = emptyList()

    override suspend fun setParticipantSyncStatus(groupId: String, personId: String, status: SyncStatus) = Unit
}
