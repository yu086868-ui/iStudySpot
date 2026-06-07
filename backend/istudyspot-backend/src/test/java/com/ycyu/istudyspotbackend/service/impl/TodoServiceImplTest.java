package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Todo;
import com.ycyu.istudyspotbackend.mapper.TodoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceImplTest {

    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoServiceImpl todoService;

    private Todo testTodo;

    @BeforeEach
    void setUp() {
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setUserId(1L);
        testTodo.setTitle("复习高数");
        testTodo.setPriority(1);
        testTodo.setStatus("pending");
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateTodo() {
        when(todoMapper.insert(any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0);
            todo.setId(1L);
            return 1;
        });

        Todo result = todoService.createTodo(1L, "复习高数", 1, null, null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("复习高数", result.getTitle());
        assertEquals(1, result.getPriority());
        assertEquals("pending", result.getStatus());

        verify(todoMapper, times(1)).insert(any(Todo.class));
    }

    @Test
    void testCreateTodoWithDefaultPriority() {
        when(todoMapper.insert(any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0);
            todo.setId(2L);
            return 1;
        });

        Todo result = todoService.createTodo(1L, "做作业", null, null, null);

        assertNotNull(result);
        assertEquals(2, result.getPriority());

        verify(todoMapper, times(1)).insert(any(Todo.class));
    }

    @Test
    void testCreateTodoEmptyTitle() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            todoService.createTodo(1L, "", 2, null, null);
        });
        assertEquals("待办标题不能为空", exception.getMessage());

        verify(todoMapper, never()).insert(any(Todo.class));
    }

    @Test
    void testCreateTodoTitleTooLong() {
        String longTitle = "a".repeat(101);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            todoService.createTodo(1L, longTitle, 2, null, null);
        });
        assertEquals("待办标题不能超过100个字符", exception.getMessage());

        verify(todoMapper, never()).insert(any(Todo.class));
    }

    @Test
    void testGetTodoListAll() {
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoMapper.findByUserId(1L)).thenReturn(todos);

        List<Todo> result = todoService.getTodoList(1L, null);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(todoMapper, times(1)).findByUserId(1L);
        verify(todoMapper, never()).findByUserIdAndStatus(anyLong(), anyString());
    }

    @Test
    void testGetTodoListByStatus() {
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoMapper.findByUserIdAndStatus(1L, "pending")).thenReturn(todos);

        List<Todo> result = todoService.getTodoList(1L, "pending");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(todoMapper, times(1)).findByUserIdAndStatus(1L, "pending");
        verify(todoMapper, never()).findByUserId(anyLong());
    }

    @Test
    void testToggleTodoPendingToCompleted() {
        when(todoMapper.findById(1L)).thenReturn(testTodo);
        when(todoMapper.updateStatus(eq(1L), eq("completed"), any(LocalDateTime.class))).thenReturn(1);

        Todo completed = new Todo();
        completed.setId(1L);
        completed.setStatus("completed");
        when(todoMapper.findById(1L)).thenReturn(testTodo);
        // First call returns pending todo, second returns completed
        when(todoMapper.findById(1L)).thenReturn(testTodo, completed);

        Todo result = todoService.toggleTodo(1L, 1L);

        verify(todoMapper, times(1)).updateStatus(eq(1L), eq("completed"), any(LocalDateTime.class));
    }

    @Test
    void testToggleTodoCompletedToPending() {
        testTodo.setStatus("completed");
        when(todoMapper.findById(1L)).thenReturn(testTodo);
        when(todoMapper.updateStatus(eq(1L), eq("pending"), isNull())).thenReturn(1);

        todoService.toggleTodo(1L, 1L);

        verify(todoMapper, times(1)).updateStatus(eq(1L), eq("pending"), isNull());
    }

    @Test
    void testToggleTodoNotFound() {
        when(todoMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            todoService.toggleTodo(999L, 1L);
        });
        assertEquals("待办不存在", exception.getMessage());
    }

    @Test
    void testToggleTodoWrongUser() {
        when(todoMapper.findById(1L)).thenReturn(testTodo);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            todoService.toggleTodo(1L, 2L);
        });
        assertEquals("无权操作此待办", exception.getMessage());
    }

    @Test
    void testUpdateTodo() {
        when(todoMapper.findById(1L)).thenReturn(testTodo);
        when(todoMapper.update(any(Todo.class))).thenReturn(1);

        Todo updated = new Todo();
        updated.setId(1L);
        updated.setTitle("复习线代");
        updated.setPriority(2);
        when(todoMapper.findById(1L)).thenReturn(testTodo, updated);

        Todo result = todoService.updateTodo(1L, 1L, "复习线代", 2, null, null);

        verify(todoMapper, times(1)).update(any(Todo.class));
    }

    @Test
    void testUpdateTodoNotFound() {
        when(todoMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            todoService.updateTodo(999L, 1L, "test", 2, null, null);
        });
        assertEquals("待办不存在", exception.getMessage());
    }

    @Test
    void testDeleteTodo() {
        when(todoMapper.findById(1L)).thenReturn(testTodo);
        when(todoMapper.deleteById(1L)).thenReturn(1);

        todoService.deleteTodo(1L, 1L);

        verify(todoMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTodoNotFound() {
        when(todoMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            todoService.deleteTodo(999L, 1L);
        });
        assertEquals("待办不存在", exception.getMessage());
    }

    @Test
    void testDeleteTodoWrongUser() {
        when(todoMapper.findById(1L)).thenReturn(testTodo);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            todoService.deleteTodo(1L, 2L);
        });
        assertEquals("无权操作此待办", exception.getMessage());
    }
}
