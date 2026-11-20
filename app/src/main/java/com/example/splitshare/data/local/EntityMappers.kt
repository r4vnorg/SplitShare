package com.example.splitshare.data.local

import com.example.splitshare.data.local.entity.ExpenseWithSplits
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupEntityWithParticipants
import com.example.splitshare.data.local.entity.GroupParticipantEntity
import com.example.splitshare.data.local.entity.ParticipantWithPerson
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupInfo
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.ParticipantEntity
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.SyncStatus

fun PersonEntity.toDomain(): Person = Person(id = id, name = name, email = email, phone = phone)

fun GroupEntity.toDomain(): Group = Group(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    syncStatus = syncStatus
)

fun GroupEntityWithParticipants.toDomain(): GroupInfo =
    GroupInfo(
        group = group.toDomain(),
        participants = participants.map { it.toDomain() }
    )


fun ParticipantWithPerson.toDomain(): Participant =
    Participant(person = person.toDomain(), isActive = participant.removedAt == null)

fun GroupParticipantEntity.toDomain(): ParticipantEntity =
    ParticipantEntity(
        groupId = groupId,
        personId = personId,
        addedAt = addedAt,
        removedAt = removedAt,
        syncStatus = syncStatus
    )


fun ExpenseWithSplits.toDomain(): Expense = Expense(
    id = expense.id,
    groupId = expense.groupId,
    description = expense.description,
    amountMinor = expense.amountMinor,
    payerId = expense.payerId,
    createdAt = expense.createdAt,
    syncStatus = expense.syncStatus,
    splits = splits.map { ExpenseSplit(personId = it.personId, shareMinor = it.shareMinor) },
)

fun Person.toEntity(): PersonEntity = PersonEntity(
    id = id,
    name = name,
    email = email,
    phone = phone
)

fun Group.toEntity(): GroupEntity = GroupEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    syncStatus = syncStatus
)

fun Group.toEntity(syncStatus: SyncStatus): GroupEntity = GroupEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    syncStatus = syncStatus
)

fun ParticipantEntity.toEntity() : GroupParticipantEntity = GroupParticipantEntity(
    groupId = groupId,
    personId = personId,
    addedAt = addedAt,
    removedAt = removedAt,
    syncStatus = SyncStatus.PENDING
)