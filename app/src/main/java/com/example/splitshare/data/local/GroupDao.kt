package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupWithParticipantCount
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Insert
    suspend fun insert(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(group: GroupEntity)

    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun observe(groupId: String): Flow<GroupEntity?>

    @Query(
        """
        SELECT g.id, g.name, g.description, g.createdAt, g.syncStatus, COUNT(gp.personId) AS participantCount
        FROM groups g
        LEFT JOIN group_participants gp ON gp.groupId = g.id AND gp.removedAt IS NULL
        GROUP BY g.id
        ORDER BY g.createdAt DESC
        """
    )
    fun observeAllWithParticipantCount(): Flow<List<GroupWithParticipantCount>>

    @Query("SELECT * FROM groups WHERE syncStatus = :status")
    suspend fun getByStatus(status: SyncStatus): List<GroupEntity>

    @Query("UPDATE groups SET syncStatus = :status WHERE id = :groupId")
    suspend fun updateSyncStatus(groupId: String, status: SyncStatus)

    @Query("DELETE FROM groups")
    suspend fun flushCache();
}
