package com.example.splitshare.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.splitshare.data.cahce.ProfileCache
import com.example.splitshare.domain.model.Profile

@Database(
    entities = [Profile::class],
    version = 1
)
abstract class DatabaseProvider : RoomDatabase() {
    abstract fun userCache(): ProfileCache
}