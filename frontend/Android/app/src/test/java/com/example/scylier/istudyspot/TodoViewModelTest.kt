package com.example.scylier.istudyspot

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.scylier.istudyspot.repository.LocalTodoStore
import com.example.scylier.istudyspot.utils.ConfigManager
import com.example.scylier.istudyspot.viewmodel.TodoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class TodoViewModelTest {

    private lateinit var application: Application
    private lateinit var configManager: ConfigManager
    private lateinit var store: LocalTodoStore
    private lateinit var viewModel: TodoViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        configManager = ConfigManager.getInstance(application)
        configManager.clearAll()
        configManager.saveUserId("42")
        application.getSharedPreferences("local_todo_store", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        store = LocalTodoStore(application)
        viewModel = TodoViewModel(application, store, configManager)
    }

    @Test
    fun loadTodosShouldReadLocalStore() {
        val first = store.createTodo(42L, "One", 1, null, null)
        store.createTodo(42L, "Two", 2, null, null)
        store.createTodo(7L, "Other", 2, null, null)
        store.toggleTodo(42L, first.id)

        viewModel.loadTodos()

        assertEquals(2, viewModel.state.value.todos.size)
        assertEquals(1, viewModel.state.value.pendingTodos.size)
        assertEquals(1, viewModel.state.value.completedTodos.size)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun createUpdateToggleDeleteShouldModifyLocalState() {
        viewModel.createTodo("  Learn  ", 1, "2026-06-18 10:00:00", 99L)
        val created = viewModel.state.value.todos.first()
        assertEquals("Learn", created.title)
        assertEquals(99L, created.orderId)
        assertEquals("创建成功", viewModel.state.value.successMessage)

        viewModel.clearSuccessMessage()
        assertNull(viewModel.state.value.successMessage)

        viewModel.updateTodo(created.id, "Updated", 2, null, null)
        assertEquals("保存成功", viewModel.state.value.successMessage)

        viewModel.toggleTodo(created.id)
        assertEquals(1, viewModel.state.value.completedTodos.size)

        viewModel.deleteTodo(created.id)
        assertTrue(viewModel.state.value.todos.isEmpty())
    }

    @Test
    fun validationAndMissingTodoShouldSetError() {
        viewModel.createTodo("")
        assertEquals("标题不能为空", viewModel.state.value.error)

        viewModel.createTodo("a".repeat(101))
        assertEquals("标题不能超过100个字符", viewModel.state.value.error)

        viewModel.toggleTodo(999L)
        assertEquals("待办不存在", viewModel.state.value.error)

        viewModel.updateTodo(999L, "Name", 2)
        assertEquals("待办不存在", viewModel.state.value.error)
    }
}
