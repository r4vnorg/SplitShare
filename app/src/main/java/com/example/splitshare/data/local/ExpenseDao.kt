package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.splitshare.data.local.entity.ExpenseEntity
import com.example.splitshare.data.local.entity.ExpenseWithSplits
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: ExpenseEntity)

    @Transaction
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun observeExpenses(groupId: String): Flow<List<ExpenseWithSplits>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun observeExpense(expenseId: String): Flow<ExpenseWithSplits?>

    @Transaction
    @Query("SELECT * FROM expenses WHERE syncStatus = :status")
    suspend fun getWithSplitsByStatus(status: SyncStatus): List<ExpenseWithSplits>

    @Query("UPDATE expenses SET syncStatus = :status WHERE id = :expenseId")
    suspend fun updateSyncStatus(expenseId: String, status: SyncStatus)
}
