package com.example.splitshare

import android.content.Context
import androidx.room.Room
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.data.PersonRepository
import com.example.splitshare.data.ProfileRepository
import com.example.splitshare.data.RoomExpenseRepository
import com.example.splitshare.data.RoomGroupRepository
import com.example.splitshare.data.RoomPersonRepository
import com.example.splitshare.data.local.AppDatabase
import com.example.splitshare.data.sync.StubSyncService
import com.example.splitshare.data.sync.SyncNotifier
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.data.sync.SyncService

/** Manual dependency injection: one shared instance of each dependency. */
class AppContainer(context: Context) {

    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "splitshare.db").build()

    val profileRepository = ProfileRepository(context)
    val personRepository: PersonRepository = RoomPersonRepository(database.personDao())
    val syncScheduler = SyncScheduler(context)
    val syncService: SyncService = StubSyncService()
    val syncNotifier = SyncNotifier(context)

    val groupRepository: GroupRepository = RoomGroupRepository(
        groupDao = database.groupDao(),
        groupParticipantDao = database.groupParticipantDao(),
        personRepository = personRepository,
        profileRepository = profileRepository,
        syncScheduler = syncScheduler,
    )

    val expenseRepository: ExpenseRepository = RoomExpenseRepository(
        expenseDao = database.expenseDao(),
        expenseSplitDao = database.expenseSplitDao(),
        syncScheduler = syncScheduler,
    )
}
