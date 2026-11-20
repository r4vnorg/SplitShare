package com.example.splitshare.network

import com.example.splitshare.data.AuthProvider
import com.example.splitshare.data.sync.SyncService
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupInfo
import com.example.splitshare.domain.model.ParticipantWithPerson
import com.example.splitshare.domain.model.Person
import java.util.UUID

data class GroupCreateRequest (val name: String, val description: String?, val creatorID: String)
data class AddParticipantRequest(val email: String, val groupId: String, val creatorID: String) //Some fields are redundant but add entropy to hashing

class NetworkSyncService(
    private val authProvider: AuthProvider,
    private val networkClient: NetworkClient,
) : SyncService {

    override suspend fun createGroup(group: Group): NetworkStatus<GroupInfo> {

        val userID = authProvider.getUser()!!
        val createRequest = GroupCreateRequest(
            name = group.name,
            description = group.description,
            creatorID = userID.id
        )

        try {
            val res = networkClient.api.createGroup(
                request = createRequest,
                requestId = ObjectHasher.sha256(createRequest)
            )

            if (res.isSuccessful)
                return NetworkStatus.Success(res.body()!!)
            else if (res.code() in 400..499)
                return NetworkStatus.Error("TODO: Message")
        } catch(_: Exception) { }

        return NetworkStatus.Pending(0)
    }

    override suspend fun addParticipant(groupId: String, email: String): NetworkStatus<ParticipantWithPerson> {

        val userID = authProvider.getUser()!!
        val addRequest = AddParticipantRequest(
            email = email,
            groupId = groupId,
            creatorID = userID.id
        )

        try {
            val res = networkClient.api.addParticipant(
                groupId = groupId,
                request = addRequest,
                requestId = ObjectHasher.sha256(addRequest)
            )

            if (res.isSuccessful)
                return NetworkStatus.Success(res.body()!!)
            else if (res.code() in 400..499)
                return NetworkStatus.Error("TODO: Message")
        } catch(_: Exception) { }

        return NetworkStatus.Pending(0)
    }

    override suspend fun syncExpense(expense: Expense): NetworkStatus<Expense> {
        try {
            val res = networkClient.api.createExpense(
                expense = expense,
                requestId = ObjectHasher.sha256(expense)
            )

            if (res.isSuccessful)
                return NetworkStatus.Success(res.body()!!)
            else if (res.code() in 400..499)
                return NetworkStatus.Error("TODO: Message")
        } catch(_: Exception) { }

        return NetworkStatus.Pending(0)

    }

    override suspend fun sendReminder(groupId: String, toPersonId: String, amountMinor: Long): Boolean = runCatching {
        //api.sendReminder(
        //    groupId = groupId,
        //    request = SendReminderRequest(toPersonId, amountMinor),
        //    requestId = UUID.randomUUID().toString(),
        //)
        true
    }.getOrDefault(false)
}
