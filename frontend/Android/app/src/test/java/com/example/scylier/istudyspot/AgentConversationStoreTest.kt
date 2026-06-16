package com.example.scylier.istudyspot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.example.scylier.istudyspot.models.agent.AgentMessageRole
import com.example.scylier.istudyspot.repository.AgentConversationSnapshot
import com.example.scylier.istudyspot.repository.InMemoryAgentConversationStore
import com.example.scylier.istudyspot.repository.SharedPreferencesAgentConversationStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class AgentConversationStoreTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("agent_conversation_store", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun inMemoryStoreShouldSortPinnedConversationFirst() {
        val store = InMemoryAgentConversationStore()
        store.saveConversation(snapshot("a", "First", updatedAt = 100L))
        store.saveConversation(snapshot("b", "Second", updatedAt = 200L))

        store.pinConversation("a")

        val summaries = store.listConversations()
        assertEquals("a", summaries.first().id)
        assertTrue(summaries.first().isPinned)
    }

    @Test
    fun inMemoryStoreClearShouldRemoveCurrentConversation() {
        val store = InMemoryAgentConversationStore()
        store.saveConversation(snapshot("a", "First", updatedAt = 100L))
        store.saveConversation(snapshot("b", "Second", updatedAt = 200L))

        store.clear()

        assertEquals(1, store.listConversations().size)
        assertEquals("a", store.listConversations().single().id)
    }

    @Test
    fun sharedPreferencesStoreShouldPersistAndLoadConversation() {
        val store = SharedPreferencesAgentConversationStore(context)
        val snapshot = snapshot("persisted", "Need seat list", updatedAt = 1234L)

        store.saveConversation(snapshot)

        val loaded = store.loadConversation("persisted")
        assertNotNull(loaded)
        assertEquals("persisted", loaded?.id)
        assertEquals("Need seat list", loaded?.title)
        assertEquals("Need seat list", store.listConversations().single().title)
    }

    @Test
    fun sharedPreferencesStoreDeleteShouldAdvanceCurrentConversation() {
        val store = SharedPreferencesAgentConversationStore(context)
        store.saveConversation(snapshot("first", "First", updatedAt = 100L))
        store.saveConversation(snapshot("second", "Second", updatedAt = 200L))

        store.deleteConversation("second")

        assertEquals("first", store.load()?.id)
        assertEquals(1, store.listConversations().size)
    }

    @Test
    fun sharedPreferencesStoreShouldMigrateLegacySnapshot() {
        val preferences = context.getSharedPreferences("agent_conversation_store", Context.MODE_PRIVATE)
        preferences.edit()
            .putString(
                "snapshot",
                """
                {
                  "sessionId":"legacy-session",
                  "messages":[{"id":"m1","role":"USER","content":"legacy prompt","blocks":[],"results":[],"isError":false,"timestamp":1}],
                  "suggestedPrompts":["查看预约规则"],
                  "updatedAt":321
                }
                """.trimIndent()
            )
            .apply()

        val store = SharedPreferencesAgentConversationStore(context)
        val summaries = store.listConversations()

        assertEquals(1, summaries.size)
        assertTrue(summaries.first().id.isNotBlank())
        assertEquals("legacy prompt", summaries.first().title)
        assertFalse(summaries.first().isPinned)
        assertNotNull(store.load())
    }

    @Test
    fun sharedPreferencesStoreClearWithoutCurrentShouldRemoveLegacySnapshot() {
        val preferences = context.getSharedPreferences("agent_conversation_store", Context.MODE_PRIVATE)
        preferences.edit()
            .putString("snapshot", """{"messages":[],"suggestedPrompts":[],"updatedAt":1}""")
            .apply()

        val store = SharedPreferencesAgentConversationStore(context)
        store.clear()

        assertNull(store.load())
    }

    private fun snapshot(
        id: String,
        firstUserMessage: String,
        updatedAt: Long
    ): AgentConversationSnapshot {
        return AgentConversationSnapshot(
            id = id,
            title = firstUserMessage,
            sessionId = "$id-session",
            messages = listOf(
                AgentMessage(
                    id = "$id-message",
                    role = AgentMessageRole.USER,
                    content = firstUserMessage
                )
            ),
            suggestedPrompts = listOf("查看预约规则"),
            updatedAt = updatedAt
        )
    }
}
