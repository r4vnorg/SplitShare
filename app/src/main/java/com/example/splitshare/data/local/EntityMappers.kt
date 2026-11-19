package com.example.splitshare.data.local

import com.example.splitshare.data.local.entity.ExpenseWithSplits
import com.example.splitshare.data.local.entity.GroupEntity
import com.example.splitshare.data.local.entity.GroupWithParticipantCount
import com.example.splitshare.data.local.entity.ParticipantWithPerson
import com.example.splitshare.data.local.entity.PersonEntity
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.ExpenseSplit
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupSummary
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.Person

fun PersonEntity.toDomain(): Person = Person(id = id, name = name, email = email, phone = phone)

fun GroupEntity.toDomain(): Group = Group(id = id, name = name, description = description, createdAt = createdAt)

fun GroupWithParticipantCount.toDomain(): GroupSummary =
    GroupSummary(id = id, name = name, participantCount = participantCount)

fun ParticipantWithPerson.toDomain(): Participant =
    Participant(person = person.toDomain(), isActive = participant.removedAt == null)

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

fun Person.toEntity(): PersonEntity = PersonEntity(id = id, name = name, email = email, phone = phone)