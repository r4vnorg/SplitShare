package com.example.splitshare.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupParticipantEntity
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GroupDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndObserveRoundTrips() = runBlocking {
        db.groupDao().insert(GroupEntity(id = "g1", name = "Cabin trip", description = "Weekend away", createdAt = 1L))

        val observed = db.groupDao().observe("g1").first()

        assertEquals("Cabin trip", observed?.name)
        assertEquals(SyncStatus.PENDING, observed?.syncStatus)
    }

    @Test
    fun participantCountExcludesRemovedParticipants() = runBlocking {
        db.groupDao().insert(GroupEntity(id = "g1", name = "Cabin trip", description = "", createdAt = 1L))
        db.personDao().upsert(PersonEntity(id = "p1", name = "Alice", email = "alice@example.com", phone = ""))
        db.personDao().upsert(PersonEntity(id = "p2", name = "Bob", email = "bob@example.com", phone = ""))
        db.groupParticipantDao().upsert(GroupParticipantEntity(groupId = "g1", personId = "p1", addedAt = 1L))
        db.groupParticipantDao().upsert(
            GroupParticipantEntity(groupId = "g1", personId = "p2", addedAt = 1L, removedAt = 2L)
        )

        val summaries = db.groupDao().observeAllWithParticipants().first()

        assertEquals(1, summaries.single().participants.size)
    }

    @Test
    fun updateSyncStatusPersists() = runBlocking {
        db.groupDao().insert(GroupEntity(id = "g1", name = "Cabin trip", description = "", createdAt = 1L))

        db.groupDao().updateSyncStatus("g1", SyncStatus.SYNCED)

        assertEquals(SyncStatus.SYNCED, db.groupDao().observe("g1").first()?.syncStatus)
    }
}
