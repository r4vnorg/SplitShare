package com.example.splitshare

import android.content.Context
import com.example.splitshare.data.ProfileRepository

/** Manual dependency injection: one shared instance of each dependency. */
class AppContainer(context: Context) {
    val profileRepository = ProfileRepository(context)
}
