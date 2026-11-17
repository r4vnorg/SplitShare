package com.example.splitshare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.splitshare.data.local.entity.ExpenseEntity
import com.example.splitshare.data.local.entity.ExpenseSplitEntity
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupParticipantEntity
import com.example.splitshare.data.local.entity.PersonEntity

@Database(
    entities = [
        PersonEntity::class,
        GroupEntity::class,
        GroupParticipantEntity::class,
        ExpenseEntity::class,
        ExpenseSplitEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun groupDao(): GroupDao
    abstract fun groupParticipantDao(): GroupParticipantDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseSplitDao(): ExpenseSplitDao
}
