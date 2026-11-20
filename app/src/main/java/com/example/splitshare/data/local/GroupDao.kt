package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupEntityWithParticipants
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

    @Transaction
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun observeAllWithParticipants(): Flow<List<GroupEntityWithParticipants>>

    @Query("SELECT * FROM groups WHERE syncStatus = :status")
    suspend fun getByStatus(status: SyncStatus): List<GroupEntity>

    @Query("UPDATE groups SET syncStatus = :status WHERE id = :groupId")
    suspend fun updateSyncStatus(groupId: String, status: SyncStatus)

    @Query("""
        UPDATE groups 
        SET id = :newId,
            name = :newName, 
            description = :newDescription, 
            createdAt = :createdAt,
            syncStatus = :syncStatus
        WHERE id = :templateId
    """)
    suspend fun replaceTemplate(templateId: String, newId: String, newName: String, newDescription: String,
                                createdAt: Long, syncStatus: SyncStatus)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Query("DELETE FROM groups")
    suspend fun flushCache()
}
