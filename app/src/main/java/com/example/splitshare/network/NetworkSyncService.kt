package com.example.splitshare.network

import com.example.splitshare.data.AuthProvider
import com.example.splitshare.data.sync.SyncService
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupInfo
import com.example.splitshare.domain.model.Person
import java.util.UUID

data class GroupCreateRequest (val name: String, val description: String?, val creatorID: String)

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

    override suspend fun syncParticipant(groupId: String, personId: String, removed: Boolean): Boolean = runCatching {
        if (removed) {
            //api.removeParticipant(groupId, personId, UUID.randomUUID().toString())
        } else {
            // Adding a participant requires a Person payload; the person must already exist
            // in the directory. We use a minimal Person object — the backend uses the ID.
            //api.addParticipant(groupId, Person(personId, "", "", ""), UUID.randomUUID().toString())
        }
        true
    }.getOrDefault(false)

    override suspend fun syncExpense(expense: Expense): Boolean = runCatching {
        //api.createExpense(expense, UUID.randomUUID().toString())
        true
    }.getOrDefault(false)

    override suspend fun sendReminder(groupId: String, toPersonId: String, amountMinor: Long): Boolean = runCatching {
        //api.sendReminder(
        //    groupId = groupId,
        //    request = SendReminderRequest(toPersonId, amountMinor),
        //    requestId = UUID.randomUUID().toString(),
        //)
        true
    }.getOrDefault(false)
}
