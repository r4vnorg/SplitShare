package com.example.splitshare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.splitshare.domain.model.SyncStatus

/**
 * A person's membership in a group. [removedAt] is a soft delete: hard-deleting would orphan
 * [ExpenseSplitEntity] rows that still reference this person, breaking expense history.
 */
@Entity(
    tableName = "group_participants",
    primaryKeys = ["groupId", "personId"],
    foreignKeys = [
        ForeignKey(entity = GroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"]),
        ForeignKey(entity = PersonEntity::class, parentColumns = ["id"], childColumns = ["personId"]),
    ],
    indices = [Index("personId")],
)
data class GroupParticipantEntity(
    val groupId: String,
    val personId: String,
    val addedAt: Long,
    val removedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
)
