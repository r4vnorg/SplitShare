package com.example.splitshare.data.local

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.splitshare.data.local.entity.ExpenseEntity
import com.example.splitshare.data.local.entity.ExpenseSplitEntity
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExpenseDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        runBlocking {
            db.groupDao().insert(GroupEntity(id = "g1", name = "Cabin trip", description = "", createdAt = 1L))
            db.personDao().upsert(PersonEntity(id = "payer", name = "Alice", email = "alice@example.com", phone = ""))
            db.personDao().upsert(PersonEntity(id = "other", name = "Bob", email = "bob@example.com", phone = ""))
        }
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun expenseIsObservedWithItsSplits() = runBlocking {
        db.expenseDao().insert(
            ExpenseEntity(id = "e1", groupId = "g1", description = "Groceries", amountMinor = 200L, payerId = "payer", createdAt = 1L)
        )
        db.expenseSplitDao().insertAll(
            listOf(ExpenseSplitEntity("e1", "payer", 100L), ExpenseSplitEntity("e1", "other", 100L))
        )

        val expense = db.expenseDao().observeExpense("e1").first()

        assertEquals(200L, expense?.expense?.amountMinor)
        assertEquals(2, expense?.splits?.size)
        assertEquals(SyncStatus.PENDING, expense?.expense?.syncStatus)
    }

    @Test
    fun pendingExpensesAreQueryableWithTheirSplits() = runBlocking {
        db.expenseDao().insert(
            ExpenseEntity(id = "e1", groupId = "g1", description = "Groceries", amountMinor = 200L, payerId = "payer", createdAt = 1L)
        )
        db.expenseSplitDao().insertAll(listOf(ExpenseSplitEntity("e1", "payer", 200L)))

        val pending = db.expenseDao().getWithSplitsByStatus(SyncStatus.PENDING)

        assertEquals(1, pending.size)
        assertEquals(1, pending.single().splits.size)
    }

    @Test
    fun updateSyncStatusPersists() = runBlocking {
        db.expenseDao().insert(
            ExpenseEntity(id = "e1", groupId = "g1", description = "Groceries", amountMinor = 200L, payerId = "payer", createdAt = 1L)
        )

        db.expenseDao().updateSyncStatus("e1", SyncStatus.FAILED)

        assertEquals(SyncStatus.FAILED, db.expenseDao().observeExpense("e1").first()?.expense?.syncStatus)
    }
}
