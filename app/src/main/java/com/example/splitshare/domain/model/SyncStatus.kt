package com.example.splitshare.domain.model

/** Local-first write status (AGENTS.md reliability design): PENDING until background sync confirms it. */
enum class SyncStatus { PENDING, SYNCED, FAILED }
