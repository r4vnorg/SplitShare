package com.example.splitshare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.splitshare.domain.model.SyncStatus

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(entity = GroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"])],
    indices = [Index("groupId")],
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val description: String,
    val amountMinor: Long,
    /** personId of whoever paid. */
    val payerId: String,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    /** Unused until US8 (receipt attachment); nullable now to avoid a later migration. */
    val receiptUri: String? = null,
)
