package com.example.splitshare.data

import android.util.Log
import com.example.splitshare.data.local.ExpenseDao
import com.example.splitshare.data.local.GroupDao
import com.example.splitshare.data.local.GroupParticipantDao
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupParticipantEntity
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.local.toEntity
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupDetailData
import com.example.splitshare.domain.model.GroupInfo
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.PendingParticipantChange
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import com.example.splitshare.network.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.UUID

class RoomGroupRepository(
    private val authProvider: AuthProvider,
    private val groupDao: GroupDao,
    private val groupParticipantDao: GroupParticipantDao,
    private val expenseDao: ExpenseDao,
    private val personRepository: PersonRepository,
    private val syncScheduler: SyncScheduler,
    private val networkClient: NetworkClient,
) : GroupRepository {

    override fun observeGroups(): Flow<List<GroupInfo>> {

        val userId = runBlocking { authProvider.getUser()!!.id } //TODO: rewrite with fixed Auth

        return networkClient.networkBoundContentWithPolling(
            cache = {
                groupDao.observeAllWithParticipants()
                    .map { list -> list.map { it.toDomain() } }
            },
            fetch = { networkClient.api.fetchUserGroups(userId) },
            save =  { list -> list.forEach {
                    Log.e("TEST", it.group.name)
                    groupDao.upsert(it.group.toEntity(SyncStatus.SYNCED))
                    it.participants.forEach {
                        participant -> groupParticipantDao.upsert(participant.toEntity())
                    }
                }
            }
        );
    }

    override fun observeGroup(groupId: String): Flow<Group?> =
        groupDao.observe(groupId).map { it?.toDomain() }

    override fun observeParticipants(groupId: String): Flow<List<Participant>> =
        groupParticipantDao.observeActiveWithPerson(groupId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun createGroup(name: String, description: String) {

        /* Just insert, Sync takes care of rest */
        groupDao.insert(GroupEntity(
            id = UUID.randomUUID().toString(),  //Temporary
            name = name,
            description = description,
            createdAt = 0,
            syncStatus = SyncStatus.SOFT_CREATE
        ))

        syncScheduler.requestSync()
    }


    override suspend fun addParticipant(groupId: String, person: Person) {
        groupParticipantDao.upsert(
            GroupParticipantEntity(groupId = groupId, personId = person.id, addedAt = System.currentTimeMillis())
        )
        syncScheduler.requestSync()
    }

    override suspend fun removeParticipant(groupId: String, personId: String) {
        groupParticipantDao.softDelete(
            groupId = groupId,
            personId = personId,
            removedAt = System.currentTimeMillis(),
            status = SyncStatus.PENDING,
        )
        syncScheduler.requestSync()
    }

    override suspend fun getPendingCreatedGroups(): List<Group> =
        groupDao.getByStatus(SyncStatus.SOFT_CREATE).map { it.toDomain() }

    override suspend fun replaceTemporaryGroup(old: Group, new: GroupInfo) {
        groupDao.upsert(new.group.toEntity(SyncStatus.SYNCED))
        groupDao.deleteGroup(old.id)
        new.participants.forEach { groupParticipantDao.upsert(it.toEntity()) }
    }

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
    ): Flow<GroupDetailData> = flow{}/*networkClient.networkBoundContentWithPolling(
        cache = { groupDao.observe(groupId) },
        fetch = { networkClient.api.fetchGroup(groupId) },
        save = { networkGroup -> saveGroup(networkGroup, groupId) },
        pollIntervalMillis = pollIntervalMillis,
    ).map { resource ->
        when (resource) {
        //    is .Success<*> -> (resource as NetworkBoundResource.Success<*>).data as? GroupEntity
        //    is NetworkBoundResource.Stale<*> -> (resource as NetworkBoundResource.Stale<*>).data as? GroupEntity
            else -> null
        }
    }.combine(
        groupParticipantDao.observeActiveWithPerson(groupId).map { it.map { row -> row.toDomain() } },
    ) { group, participants ->
        group to participants
    }.combine(
        expenseDao.observeExpenses(groupId).map { it.map { es -> es.toDomain() } },
    ) { (group, participants), expenses ->
        GroupDetailData(group = group, participants = participants, expenses = expenses)
    }*/

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
