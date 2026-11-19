package com.example.splitshare.data

import com.example.splitshare.data.local.ExpenseDao
import com.example.splitshare.data.local.GroupDao
import com.example.splitshare.data.local.GroupParticipantDao
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupParticipantEntity
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupDetailData
import com.example.splitshare.domain.model.GroupSummary
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.PendingParticipantChange
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.network.NetworkBoundResource
import com.example.splitshare.network.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomGroupRepository(
    private val groupDao: GroupDao,
    private val groupParticipantDao: GroupParticipantDao,
    private val expenseDao: ExpenseDao,
    private val personRepository: PersonRepository,
    private val syncScheduler: SyncScheduler,
    private val networkClient: NetworkClient,
) : GroupRepository {

    override fun observeGroups(): Flow<List<GroupSummary>> =
        groupDao.observeAllWithParticipantCount().map { groups -> groups.map { it.toDomain() } }

    override fun observeGroup(groupId: String): Flow<Group?> =
        groupDao.observe(groupId).map { it?.toDomain() }

    override fun observeParticipants(groupId: String): Flow<List<Participant>> =
        groupParticipantDao.observeActiveWithPerson(groupId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun createGroup(name: String, description: String): Group {
        val now = System.currentTimeMillis()
        val group = GroupEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description.trim(),
            createdAt = now,
        )
        groupDao.insert(group)

        //personRepository.profile.first()?.let { profile ->
        //    val owner = personRepository.upsertAndGet(profile.name, profile.email, profile.phone)
        //    groupParticipantDao.upsert(
        //        GroupParticipantEntity(groupId = group.id, personId = owner.id, addedAt = now)
        //    )
        //}

        syncScheduler.scheduleSync()
        return group.toDomain()
    }

    override suspend fun addParticipant(groupId: String, person: Person) {
        groupParticipantDao.upsert(
            GroupParticipantEntity(groupId = groupId, personId = person.id, addedAt = System.currentTimeMillis())
        )
        syncScheduler.scheduleSync()
    }

    override suspend fun removeParticipant(groupId: String, personId: String) {
        groupParticipantDao.softDelete(
            groupId = groupId,
            personId = personId,
            removedAt = System.currentTimeMillis(),
            status = SyncStatus.PENDING,
        )
        syncScheduler.scheduleSync()
    }

    override suspend fun getPendingGroups(): List<Group> =
        groupDao.getByStatus(SyncStatus.PENDING).map { it.toDomain() }

    override suspend fun setGroupSyncStatus(groupId: String, status: SyncStatus) =
        groupDao.updateSyncStatus(groupId, status)

    override suspend fun getPendingParticipantChanges(): List<PendingParticipantChange> =
        groupParticipantDao.getByStatus(SyncStatus.PENDING).map {
            PendingParticipantChange(groupId = it.groupId, personId = it.personId, removed = it.removedAt != null)
        }

    override suspend fun setParticipantSyncStatus(groupId: String, personId: String, status: SyncStatus) =
        groupParticipantDao.updateSyncStatus(groupId, personId, status)

    // ── Network-synced observe ────────────────────────────────────────────

    /**
     * Combines group polling (with network retry + automatic polling) with
     * live participants and expenses from the local cache.
     *
     * The group is fetched from the network every [pollIntervalMillis] and saved
     * to the local cache. Participants and expenses are read from the cache —
     * they are kept fresh by their own observeWithSync calls or local writes.
     *
     * Any change in group, participants, or expenses triggers a re-emit.
     */
    override fun observeDetailData(
        groupId: String,
        pollIntervalMillis: Long,
    ): Flow<GroupDetailData> = networkClient.networkBoundResourceWithPolling(
        cache = { groupDao.observe(groupId) },
        fetch = { networkClient.api.fetchGroup(groupId) },
        save = { networkGroup -> saveGroup(networkGroup, groupId) },
        pollIntervalMillis = pollIntervalMillis,
    ).map { resource ->
        when (resource) {
            is NetworkBoundResource.Success<*> -> (resource as NetworkBoundResource.Success<*>).data as? GroupEntity
            is NetworkBoundResource.Stale<*> -> (resource as NetworkBoundResource.Stale<*>).data as? GroupEntity
            else -> null
        }?.toDomain()
    }.combine(
        groupParticipantDao.observeActiveWithPerson(groupId).map { it.map { row -> row.toDomain() } },
    ) { group, participants ->
        group to participants
    }.combine(
        expenseDao.observeExpenses(groupId).map { it.map { es -> es.toDomain() } },
    ) { (group, participants), expenses ->
        GroupDetailData(group = group, participants = participants, expenses = expenses)
    }

    /**
     * Saves a group fetched from the network to the local cache.
     * Preserves the existing sync status if the group already exists (to avoid resetting
     * PENDING/FAILED status on a read-only poll).
     */
    private suspend fun saveGroup(networkGroup: Group, groupId: String) {
        // Check if the group already exists to preserve its sync status
        val existing = groupDao.observe(groupId).first()
        val entity = GroupEntity(
            id = networkGroup.id,
            name = networkGroup.name,
            description = networkGroup.description,
            createdAt = networkGroup.createdAt,
            syncStatus = existing?.syncStatus ?: SyncStatus.SYNCED,
        )
        groupDao.upsert(entity)
    }
}
