package com.example.splitshare.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.splitshare.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.profileStore by preferencesDataStore(name = "profile")

/**
 * Stores the user's profile on the device (mocked authentication, US3).
 * One profile per device; logging in means matching its email.
 */
class ProfileRepository(private val context: Context) {

    private object Keys {
        val ID = stringPreferencesKey("id")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
    }

    val profile: Flow<Profile?> = context.profileStore.data.map { prefs ->
        val id = prefs[Keys.ID] ?: return@map null
        Profile(
            id = id,
            name = prefs[Keys.NAME].orEmpty(),
            email = prefs[Keys.EMAIL].orEmpty(),
            phone = prefs[Keys.PHONE].orEmpty(),
        )
    }

    suspend fun save(profile: Profile) {
        context.profileStore.edit { prefs ->
            prefs[Keys.ID] = profile.id
            prefs[Keys.NAME] = profile.name
            prefs[Keys.EMAIL] = profile.email
            prefs[Keys.PHONE] = profile.phone
        }
    }

    /** Returns the stored profile if its email matches, otherwise null. */
    suspend fun findByEmail(email: String): Profile? =
        profile.first()?.takeIf { it.email.equals(email.trim(), ignoreCase = true) }
}
