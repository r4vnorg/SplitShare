package com.example.splitshare

import android.content.Context
import androidx.room.Room
import com.example.splitshare.data.AuthProvider
import com.example.splitshare.data.ExpenseRepository
import com.example.splitshare.data.GroupRepository
import com.example.splitshare.data.PersonRepository
import com.example.splitshare.data.ReminderRepository
import com.example.splitshare.data.SchedulingReminderRepository
import com.example.splitshare.data.RoomExpenseRepository
import com.example.splitshare.data.RoomGroupRepository
import com.example.splitshare.data.RoomPersonRepository
import com.example.splitshare.data.local.AppDatabase
import com.example.splitshare.data.sync.SyncNotifier
import com.example.splitshare.data.sync.SyncScheduler
import com.example.splitshare.data.sync.SyncService
import com.example.splitshare.network.NetworkClient
import com.example.splitshare.network.NetworkSyncService

/** Manual dependency injection: one shared instance of each dependency. */
class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "splitshare.db")
        .fallbackToDestructiveMigration(true)
        .build()

    val networkClient = NetworkClient("http://10.0.2.2:8080/")

    val personRepository: PersonRepository = RoomPersonRepository(database.personDao(), networkClient)
    val syncScheduler = SyncScheduler(context)

    val authProvider = AuthProvider(
        authDao = database.userAuthDao(),
        peopleDao = database.personDao(),
        flushDatabases = { flushDatabases() },
        networkClient = networkClient
    )

    // Network layer with automatic retries (RetryInterceptor) built in

    // Real network-backed sync service (swappable with StubSyncService for testing)
    val syncService: SyncService = NetworkSyncService(authProvider, networkClient)
    val syncNotifier = SyncNotifier(context)
    val reminderRepository: ReminderRepository = SchedulingReminderRepository(syncScheduler)

    val groupRepository: GroupRepository = RoomGroupRepository(
        authProvider = authProvider,
        groupDao = database.groupDao(),
        groupParticipantDao = database.groupParticipantDao(),
        expenseDao = database.expenseDao(),
        personDao = database.personDao(),
        syncScheduler = syncScheduler,
        networkClient = networkClient,
        personRepository = personRepository
    )

    val expenseRepository: ExpenseRepository = RoomExpenseRepository(
        expenseDao = database.expenseDao(),
        expenseSplitDao = database.expenseSplitDao(),
        syncScheduler = syncScheduler,
        networkClient = networkClient,
    )

    suspend fun flushDatabases() {
        database.expenseSplitDao().flushCache()
        database.groupParticipantDao().flushCache()
        database.expenseDao().flushCache()
        database.userAuthDao().flushCache()
        database.groupDao().flushCache()
        database.personDao().flushCache()
    }
}
