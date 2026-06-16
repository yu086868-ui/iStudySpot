package com.example.scylier.istudyspot.repository

import android.content.Context
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.example.scylier.istudyspot.models.agent.AgentMessageRole
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class AgentConversationSnapshot(
    val id: String = "",
    val title: String = "",
    val sessionId: String? = null,
    val messages: List<AgentMessage> = emptyList(),
    val suggestedPrompts: List<String> = emptyList(),
    val pinnedAt: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

data class AgentConversationSummary(
    val id: String,
    val title: String,
    val preview: String,
    val isPinned: Boolean,
    val updatedAt: Long
)

interface AgentConversationStore {
    fun load(): AgentConversationSnapshot?
    fun save(snapshot: AgentConversationSnapshot)
    fun clear()
    fun listConversations(): List<AgentConversationSummary>
    fun loadConversation(id: String): AgentConversationSnapshot?
    fun saveConversation(snapshot: AgentConversationSnapshot)
    fun pinConversation(id: String)
    fun deleteConversation(id: String)
}

class InMemoryAgentConversationStore : AgentConversationStore {
    private val conversations = linkedMapOf<String, AgentConversationSnapshot>()
    private var currentConversationId: String? = null

    override fun load(): AgentConversationSnapshot? {
        return currentConversationId?.let { conversations[it] }
            ?: conversations.values.maxByOrNull { it.updatedAt }
    }

    override fun save(snapshot: AgentConversationSnapshot) {
        saveConversation(snapshot)
    }

    override fun clear() {
        val currentId = currentConversationId ?: conversations.values.maxByOrNull { it.updatedAt }?.id
        if (currentId != null) {
            deleteConversation(currentId)
        }
    }

    override fun listConversations(): List<AgentConversationSummary> {
        return conversations.values
            .sortedWith(
                compareByDescending<AgentConversationSnapshot> { it.pinnedAt > 0 }
                    .thenByDescending { it.pinnedAt }
                    .thenByDescending { it.updatedAt }
            )
            .map { it.toSummary() }
    }

    override fun loadConversation(id: String): AgentConversationSnapshot? = conversations[id]

    override fun saveConversation(snapshot: AgentConversationSnapshot) {
        val existing = conversations[snapshot.id]
        val normalized = snapshot
            .copy(pinnedAt = snapshot.pinnedAt.takeIf { it > 0 } ?: existing?.pinnedAt ?: 0L)
            .ensureIdentity()
        conversations[normalized.id] = normalized
        currentConversationId = normalized.id
    }

    override fun pinConversation(id: String) {
        val current = conversations[id] ?: return
        conversations[id] = current.copy(pinnedAt = System.currentTimeMillis())
    }

    override fun deleteConversation(id: String) {
        conversations.remove(id)
        if (currentConversationId == id) {
            currentConversationId = conversations.values.maxByOrNull { it.updatedAt }?.id
        }
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
    private val snapshotListType = object : TypeToken<List<AgentConversationSnapshot>>() {}.type

    override fun load(): AgentConversationSnapshot? {
        val currentId = preferences.getString(KEY_CURRENT_ID, null)
        if (!currentId.isNullOrBlank()) {
            loadConversation(currentId)?.let { return it }
        }
        loadSnapshots().firstOrNull()?.let { return it }
        return loadLegacySnapshot()
    }

    override fun save(snapshot: AgentConversationSnapshot) {
        saveConversation(snapshot)
    }

    override fun clear() {
        val currentId = preferences.getString(KEY_CURRENT_ID, null)
        if (!currentId.isNullOrBlank()) {
            deleteConversation(currentId)
            return
        }
        preferences.edit()
            .remove(KEY_CURRENT_ID)
            .remove(KEY_SNAPSHOT)
            .apply()
    }

    override fun listConversations(): List<AgentConversationSummary> {
        return migrateLegacyIfNeeded()
            .sortedWith(
                compareByDescending<AgentConversationSnapshot> { it.pinnedAt > 0 }
                    .thenByDescending { it.pinnedAt }
                    .thenByDescending { it.updatedAt }
            )
            .map { it.toSummary() }
    }

    override fun loadConversation(id: String): AgentConversationSnapshot? {
        return loadSnapshots().firstOrNull { it.id == id }
            ?: loadLegacySnapshot()?.ensureIdentity()?.takeIf { it.id == id }
    }

    override fun saveConversation(snapshot: AgentConversationSnapshot) {
        val existing = loadSnapshots().firstOrNull { it.id == snapshot.id }
        val normalized = snapshot
            .copy(pinnedAt = snapshot.pinnedAt.takeIf { it > 0 } ?: existing?.pinnedAt ?: 0L)
            .ensureIdentity()
        val next = (loadSnapshots().filterNot { it.id == normalized.id } + normalized)
            .sortedWith(
                compareByDescending<AgentConversationSnapshot> { it.pinnedAt > 0 }
                    .thenByDescending { it.pinnedAt }
                    .thenByDescending { it.updatedAt }
            )
            .take(MAX_CONVERSATIONS)
        preferences.edit()
            .putString(KEY_CURRENT_ID, normalized.id)
            .putString(KEY_SNAPSHOT, gson.toJson(normalized, snapshotType))
            .putString(KEY_CONVERSATIONS, gson.toJson(next, snapshotListType))
            .apply()
    }

    override fun pinConversation(id: String) {
        val next = loadSnapshots().map { snapshot ->
            if (snapshot.id == id) snapshot.copy(pinnedAt = System.currentTimeMillis()) else snapshot
        }
        val current = load()
        val currentSnapshot = next.firstOrNull { it.id == current?.id } ?: current
        val editor = preferences.edit()
            .putString(
                KEY_CONVERSATIONS,
                gson.toJson(
                    next.sortedWith(
                        compareByDescending<AgentConversationSnapshot> { it.pinnedAt > 0 }
                            .thenByDescending { it.pinnedAt }
                            .thenByDescending { it.updatedAt }
                    ).take(MAX_CONVERSATIONS),
                    snapshotListType
                )
            )
        if (currentSnapshot != null) {
            editor.putString(KEY_CURRENT_ID, currentSnapshot.id)
            editor.putString(KEY_SNAPSHOT, gson.toJson(currentSnapshot, snapshotType))
        }
        editor.apply()
    }

    override fun deleteConversation(id: String) {
        val next = loadSnapshots().filterNot { it.id == id }
        val nextCurrentId = preferences.getString(KEY_CURRENT_ID, null).takeIf { it != id }
            ?: next.firstOrNull()?.id
        val editor = preferences.edit()
            .putString(KEY_CONVERSATIONS, gson.toJson(next, snapshotListType))

        if (nextCurrentId.isNullOrBlank()) {
            editor.remove(KEY_CURRENT_ID).remove(KEY_SNAPSHOT)
        } else {
            editor.putString(KEY_CURRENT_ID, nextCurrentId)
            next.firstOrNull { it.id == nextCurrentId }?.let { current ->
                editor.putString(KEY_SNAPSHOT, gson.toJson(current, snapshotType))
            }
        }
        editor.apply()
    }

    private fun loadSnapshots(): List<AgentConversationSnapshot> {
        val raw = preferences.getString(KEY_CONVERSATIONS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<AgentConversationSnapshot>>(raw, snapshotListType)
        }.getOrNull().orEmpty()
    }

    private fun loadLegacySnapshot(): AgentConversationSnapshot? {
        val raw = preferences.getString(KEY_SNAPSHOT, null) ?: return null
        return runCatching {
            gson.fromJson<AgentConversationSnapshot>(raw, snapshotType)
        }.getOrNull()
    }

    private fun migrateLegacyIfNeeded(): List<AgentConversationSnapshot> {
        val snapshots = loadSnapshots()
        if (snapshots.isNotEmpty()) {
            return snapshots
        }

        val legacy = loadLegacySnapshot()?.ensureIdentity() ?: return emptyList()
        if (legacy.messages.isEmpty()) {
            return emptyList()
        }
        saveConversation(legacy)
        return listOf(legacy)
    }

    private companion object {
        const val KEY_CURRENT_ID = "current_id"
        const val KEY_SNAPSHOT = "snapshot"
        const val KEY_CONVERSATIONS = "conversations"
        const val MAX_CONVERSATIONS = 12
    }
}

private fun AgentConversationSnapshot.ensureIdentity(): AgentConversationSnapshot {
    val normalizedId = id.takeIf { it.isNotBlank() } ?: "local-${UUID.randomUUID()}"
    val normalizedTitle = title.takeIf { it.isNotBlank() } ?: buildConversationTitle(messages)
    return copy(
        id = normalizedId,
        title = normalizedTitle,
        pinnedAt = pinnedAt.takeIf { it > 0 } ?: 0L,
        updatedAt = updatedAt.takeIf { it > 0 } ?: System.currentTimeMillis()
    )
}

private fun AgentConversationSnapshot.toSummary(): AgentConversationSummary {
    return AgentConversationSummary(
        id = id,
        title = title.takeIf { it.isNotBlank() } ?: buildConversationTitle(messages),
        preview = messages.lastOrNull()?.content?.trim()?.take(60).orEmpty(),
        isPinned = pinnedAt > 0,
        updatedAt = updatedAt
    )
}

private fun buildConversationTitle(messages: List<AgentMessage>): String {
    return messages.firstOrNull { it.role == AgentMessageRole.USER }
        ?.content
        ?.trim()
        ?.take(20)
        ?.takeIf { it.isNotBlank() }
        ?: "新会话"
}
