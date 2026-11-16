package com.example.splitshare.data.local

import androidx.room.Dao
import androidx.room.Insert
import com.example.splitshare.data.local.entity.ExpenseSplitEntity

@Dao
interface ExpenseSplitDao {

    @Insert
    suspend fun insertAll(splits: List<ExpenseSplitEntity>)
}
