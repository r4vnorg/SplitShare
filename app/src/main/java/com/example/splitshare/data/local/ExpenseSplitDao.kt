package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.splitshare.data.local.entity.ExpenseSplitEntity

@Dao
interface ExpenseSplitDao {

    @Insert
    suspend fun insertAll(splits: List<ExpenseSplitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(splits: List<ExpenseSplitEntity>)

    @Query("DELETE FROM expense_splits")
    suspend fun flushCache()
}
