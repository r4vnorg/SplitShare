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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GroupParticipantDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        runBlocking {
            db.groupDao().insert(GroupEntity(id = "g1", name = "Cabin trip", description = "", createdAt = 1L))
            db.personDao().insert(PersonEntity(id = "p1", name = "Alice", email = "alice@example.com", phone = ""))
        }
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun activeParticipantIsObservedWithPersonDetails() = runBlocking {
        db.groupParticipantDao().upsert(GroupParticipantEntity(groupId = "g1", personId = "p1", addedAt = 1L))

        val active = db.groupParticipantDao().observeActiveWithPerson("g1").first()

        assertEquals("Alice", active.single().person.name)
        assertNull(active.single().participant.removedAt)
    }

    @Test
    fun softDeleteRemovesFromActiveListButKeepsTheRow() = runBlocking {
        db.groupParticipantDao().upsert(GroupParticipantEntity(groupId = "g1", personId = "p1", addedAt = 1L))

        db.groupParticipantDao().softDelete("g1", "p1", removedAt = 5L, status = SyncStatus.PENDING)

        assertTrue(db.groupParticipantDao().observeActiveWithPerson("g1").first().isEmpty())
        assertEquals(5L, db.groupParticipantDao().find("g1", "p1")?.removedAt)
    }

    @Test
    fun pendingChangesAreQueryableByStatus() = runBlocking {
        db.groupParticipantDao().upsert(GroupParticipantEntity(groupId = "g1", personId = "p1", addedAt = 1L))

        val pending = db.groupParticipantDao().getByStatus(SyncStatus.PENDING)

        assertEquals(1, pending.size)
    }
}
