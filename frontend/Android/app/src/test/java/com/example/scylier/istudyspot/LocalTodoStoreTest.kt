package com.example.scylier.istudyspot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.scylier.istudyspot.repository.LocalTodoStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class LocalTodoStoreTest {

    private lateinit var context: Context
    private lateinit var store: LocalTodoStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("local_todo_store", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        store = LocalTodoStore(context)
    }

    @Test
    fun createAndLoadTodosShouldTrimTitleAndFilterByUser() {
        val created = store.createTodo(
            userId = 7L,
            title = "  Finish report  ",
            priority = 1,
            dueTime = "2026-06-20 18:00:00",
            orderId = 100L
        )
        store.createTodo(
            userId = 8L,
            title = "Other user todo",
            priority = 2,
            dueTime = null,
            orderId = null
        )

        val todos = store.loadTodos(7L)
        assertEquals(1, todos.size)
        assertEquals(created.id, todos.first().id)
        assertEquals("Finish report", todos.first().title)
        assertEquals("pending", todos.first().status)
        assertEquals(100L, todos.first().orderId)
    }

    @Test
    fun updateTodoShouldChangeFieldsAndKeepOwnership() {
        val todo = store.createTodo(
            userId = 5L,
            title = "Draft",
            priority = 3,
            dueTime = null,
            orderId = null
        )

        val updated = store.updateTodo(
            userId = 5L,
            todoId = todo.id,
            title = "  Final draft  ",
            priority = 1,
            dueTime = "2026-06-21 09:00:00",
            orderId = 11L
        )

        assertEquals(todo.id, updated.id)
        assertEquals("Final draft", updated.title)
        assertEquals(1, updated.priority)
        assertEquals("2026-06-21 09:00:00", updated.dueTime)
        assertEquals(11L, updated.orderId)
        assertTrue(updated.updatedAt.orEmpty().isNotBlank())
    }

    @Test
    fun toggleTodoShouldSwitchBetweenPendingAndCompleted() {
        val todo = store.createTodo(
            userId = 3L,
            title = "Read chapter",
            priority = 2,
            dueTime = null,
            orderId = null
        )

        val completed = store.toggleTodo(3L, todo.id)
        assertEquals("completed", completed.status)
        assertTrue(completed.completedAt.orEmpty().isNotBlank())

        val reopened = store.toggleTodo(3L, todo.id)
        assertEquals("pending", reopened.status)
        assertNull(reopened.completedAt)
    }

    @Test
    fun deleteTodoShouldOnlyDeleteMatchingUserRecord() {
        val todo1 = store.createTodo(1L, "Todo 1", 2, null, null)
        val todo2 = store.createTodo(2L, "Todo 2", 2, null, null)

        store.deleteTodo(1L, todo1.id)

        assertTrue(store.loadTodos(1L).isEmpty())
        assertEquals(todo2.id, store.loadTodos(2L).single().id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateTodoShouldThrowWhenTodoNotFound() {
        store.updateTodo(
            userId = 99L,
            todoId = 123456L,
            title = "Missing",
            priority = 2,
            dueTime = null,
            orderId = null
        )
    }
}
