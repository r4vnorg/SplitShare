package com.example.splitshare.data

import com.example.splitshare.data.local.GroupDao
import com.example.splitshare.data.local.GroupParticipantDao
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupParticipantEntity
import com.example.splitshare.data.local.toDomain
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupSummary
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.PendingParticipantChange
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomGroupRepository(
    private val groupDao: GroupDao,
    private val groupParticipantDao: GroupParticipantDao,
    private val personRepository: PersonRepository,
    private val profileRepository: ProfileRepository,
    private val syncScheduler: SyncScheduler,
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

        profileRepository.profile.first()?.let { profile ->
            val owner = personRepository.upsertAndGet(profile.name, profile.email, profile.phone)
            groupParticipantDao.upsert(
                GroupParticipantEntity(groupId = group.id, personId = owner.id, addedAt = now)
            )
        }

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
}
