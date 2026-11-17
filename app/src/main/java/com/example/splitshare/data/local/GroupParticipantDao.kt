package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.splitshare.data.local.entity.GroupParticipantEntity
import com.example.splitshare.data.local.entity.ParticipantWithPerson
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupParticipantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(participant: GroupParticipantEntity)

    @Query("SELECT * FROM group_participants WHERE groupId = :groupId AND personId = :personId LIMIT 1")
    suspend fun find(groupId: String, personId: String): GroupParticipantEntity?

    @Transaction
    @Query("SELECT * FROM group_participants WHERE groupId = :groupId AND removedAt IS NULL")
    fun observeActiveWithPerson(groupId: String): Flow<List<ParticipantWithPerson>>

    @Query(
        "UPDATE group_participants SET removedAt = :removedAt, syncStatus = :status " +
            "WHERE groupId = :groupId AND personId = :personId"
    )
    suspend fun softDelete(groupId: String, personId: String, removedAt: Long, status: SyncStatus)

    @Query("SELECT * FROM group_participants WHERE syncStatus = :status")
    suspend fun getByStatus(status: SyncStatus): List<GroupParticipantEntity>

    @Query(
        "UPDATE group_participants SET syncStatus = :status WHERE groupId = :groupId AND personId = :personId"
    )
    suspend fun updateSyncStatus(groupId: String, personId: String, status: SyncStatus)
}
