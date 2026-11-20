package com.example.splitshare.network

import com.example.splitshare.data.AuthRequest
import com.example.splitshare.data.RegisterRequest
import com.example.splitshare.domain.model.Expense
import com.example.splitshare.domain.model.Group
import com.example.splitshare.domain.model.GroupInfo
import com.example.splitshare.domain.model.Participant
import com.example.splitshare.domain.model.Person
import com.example.splitshare.domain.model.PersonRef
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkAPI {

    // ── AUTH ──────────────────────────────────────────────────────────────
    @POST("login")
    suspend fun auth(@Body authReq: AuthRequest) : Response<Person>

    @POST("register")
    suspend fun register(@Body registerReq: RegisterRequest) : Response<Person>

    // ── People (directory) ────────────────────────────────────────────────

    @GET("people/{id}")
    suspend fun fetchPerson(@Path("id") id: String): Person

    @PUT("people/{id}")
    suspend fun updatePerson(@Path("id") id: String, @Body person: Person, @Query("request_id") requestId: String): Person

    @DELETE("people/{id}")
    suspend fun deletePerson(@Path("id") id: String, @Query("request_id") requestId: String)

    @GET("people/{id}/groups")
    suspend fun fetchUserGroups(@Path("id") id: String): List<GroupInfo>

    // ── Groups ────────────────────────────────────────────────────────────

    @GET("groups/{id}")
    suspend fun fetchGroup(@Path("id") id: String): Group

    @POST("groups")
    suspend fun createGroup(@Body request: GroupCreateRequest, @Query("request_id") requestId: String): Response<GroupInfo>

    @PUT("groups/{id}")
    suspend fun updateGroup(@Path("id") id: String, @Body group: Group, @Query("request_id") requestId: String): Group

    @DELETE("groups/{id}")
    suspend fun deleteGroup(@Path("id") id: String, @Query("request_id") requestId: String)

    // ── Participants ──────────────────────────────────────────────────────

    @GET("groups/{groupId}/participants")
    suspend fun fetchParticipants(@Path("groupId") groupId: String): Array<Participant>

    @POST("groups/{groupId}/participants")
    suspend fun addParticipant(@Path("groupId") groupId: String, @Body person: Person, @Query("request_id") requestId: String): Participant

    @DELETE("groups/{groupId}/participants/{personId}")
    suspend fun removeParticipant(@Path("groupId") groupId: String, @Path("personId") personId: String, @Query("request_id") requestId: String)

    // ── Expenses ──────────────────────────────────────────────────────────

    @GET("groups/{groupId}/expenses")
    suspend fun fetchExpenses(@Path("groupId") groupId: String): Array<Expense>

    @GET("expenses/{id}")
    suspend fun fetchExpense(@Path("id") id: String): Expense

    @POST("expenses")
    suspend fun createExpense(@Body expense: Expense, @Query("request_id") requestId: String): Expense

    @PUT("expenses/{id}")
    suspend fun updateExpense(@Path("id") id: String, @Body expense: Expense, @Query("request_id") requestId: String): Expense

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String, @Query("request_id") requestId: String)

    // ── Reminders ─────────────────────────────────────────────────────────

    @POST("groups/{groupId}/reminders")
    suspend fun sendReminder(
        @Path("groupId") groupId: String,
        @Body request: SendReminderRequest,
        @Query("request_id") requestId: String,
    )
}

/** Request payload for the reminder endpoint. */
data class SendReminderRequest(
    val toPersonId: String,
    val amountMinor: Long,
)