package com.example.splitshare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.splitshare.domain.model.SyncStatus

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
)
