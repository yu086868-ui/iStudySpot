package com.example.scylier.istudyspot.repository

import android.content.Context
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class AgentConversationSnapshot(
    val sessionId: String? = null,
    val messages: List<AgentMessage> = emptyList(),
    val suggestedPrompts: List<String> = emptyList()
)

interface AgentConversationStore {
    fun load(): AgentConversationSnapshot?
    fun save(snapshot: AgentConversationSnapshot)
    fun clear()
}

class InMemoryAgentConversationStore(
    private var snapshot: AgentConversationSnapshot? = null
) : AgentConversationStore {
    override fun load(): AgentConversationSnapshot? = snapshot

    override fun save(snapshot: AgentConversationSnapshot) {
        this.snapshot = snapshot
    }

    override fun clear() {
        snapshot = null
    }
}

class SharedPreferencesAgentConversationStore(
    context: Context,
    private val gson: Gson = Gson()
) : AgentConversationStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "agent_conversation_store",
        Context.MODE_PRIVATE
    )
    private val snapshotType = object : TypeToken<AgentConversationSnapshot>() {}.type

    override fun load(): AgentConversationSnapshot? {
        val raw = preferences.getString(KEY_SNAPSHOT, null) ?: return null
        return runCatching {
            gson.fromJson<AgentConversationSnapshot>(raw, snapshotType)
        }.getOrNull()
    }

    override fun save(snapshot: AgentConversationSnapshot) {
        preferences.edit()
            .putString(KEY_SNAPSHOT, gson.toJson(snapshot, snapshotType))
            .apply()
    }

    override fun clear() {
        preferences.edit().remove(KEY_SNAPSHOT).apply()
    }

    private companion object {
        const val KEY_SNAPSHOT = "snapshot"
    }
}
