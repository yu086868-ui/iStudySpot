package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.entity.Todo;
import com.ycyu.istudyspotbackend.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TodoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private ObjectMapper objectMapper;

    private Todo testTodo;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(todoController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

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
    void testGetTodoList() throws Exception {
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoService.getTodoList(1L, null)).thenReturn(todos);

        mockMvc.perform(get("/api/todos")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].title").value("复习高数"));

        verify(todoService, times(1)).getTodoList(1L, null);
    }

    @Test
    void testGetTodoListWithStatus() throws Exception {
        List<Todo> todos = Arrays.asList(testTodo);
        when(todoService.getTodoList(1L, "pending")).thenReturn(todos);

        mockMvc.perform(get("/api/todos")
                .param("status", "pending")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(todoService, times(1)).getTodoList(1L, "pending");
    }

    @Test
    void testCreateTodo() throws Exception {
        when(todoService.createTodo(eq(1L), eq("做作业"), eq(2), isNull(), isNull())).thenReturn(testTodo);

        Map<String, Object> body = new HashMap<>();
        body.put("title", "做作业");
        body.put("priority", 2);

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建成功"));

        verify(todoService, times(1)).createTodo(eq(1L), eq("做作业"), eq(2), isNull(), isNull());
    }

    @Test
    void testCreateTodoWithDueTime() throws Exception {
        when(todoService.createTodo(eq(1L), eq("复习"), eq(1), any(LocalDateTime.class), isNull())).thenReturn(testTodo);

        Map<String, Object> body = new HashMap<>();
        body.put("title", "复习");
        body.put("priority", 1);
        body.put("dueTime", "2026-06-05 18:00:00");

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(todoService, times(1)).createTodo(eq(1L), eq("复习"), eq(1), any(LocalDateTime.class), isNull());
    }

    @Test
    void testCreateTodoEmptyTitle() throws Exception {
        when(todoService.createTodo(eq(1L), eq(""), anyInt(), isNull(), isNull()))
                .thenThrow(new RuntimeException("待办标题不能为空"));

        Map<String, Object> body = new HashMap<>();
        body.put("title", "");
        body.put("priority", 2);

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("待办标题不能为空"));
    }

    @Test
    void testUpdateTodo() throws Exception {
        Todo updated = new Todo();
        updated.setId(1L);
        updated.setTitle("复习线代");
        updated.setPriority(2);
        updated.setStatus("pending");
        when(todoService.updateTodo(eq(1L), eq(1L), eq("复习线代"), eq(2), isNull(), isNull())).thenReturn(updated);

        Map<String, Object> body = new HashMap<>();
        body.put("title", "复习线代");
        body.put("priority", 2);

        mockMvc.perform(put("/api/todos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"));

        verify(todoService, times(1)).updateTodo(eq(1L), eq(1L), eq("复习线代"), eq(2), isNull(), isNull());
    }

    @Test
    void testUpdateTodoNotFound() throws Exception {
        when(todoService.updateTodo(eq(999L), eq(1L), eq("test"), eq(2), isNull(), isNull()))
                .thenThrow(new RuntimeException("待办不存在"));

        Map<String, Object> body = new HashMap<>();
        body.put("title", "test");
        body.put("priority", 2);

        mockMvc.perform(put("/api/todos/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("待办不存在"));
    }

    @Test
    void testToggleTodo() throws Exception {
        Todo completed = new Todo();
        completed.setId(1L);
        completed.setStatus("completed");
        when(todoService.toggleTodo(1L, 1L)).thenReturn(completed);

        mockMvc.perform(put("/api/todos/1/toggle")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"));

        verify(todoService, times(1)).toggleTodo(1L, 1L);
    }

    @Test
    void testToggleTodoWrongUser() throws Exception {
        when(todoService.toggleTodo(1L, 2L))
                .thenThrow(new RuntimeException("无权操作此待办"));

        mockMvc.perform(put("/api/todos/1/toggle")
                .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无权操作此待办"));
    }

    @Test
    void testDeleteTodo() throws Exception {
        doNothing().when(todoService).deleteTodo(1L, 1L);

        mockMvc.perform(delete("/api/todos/1")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(todoService, times(1)).deleteTodo(1L, 1L);
    }

    @Test
    void testDeleteTodoNotFound() throws Exception {
        doThrow(new RuntimeException("待办不存在")).when(todoService).deleteTodo(999L, 1L);

        mockMvc.perform(delete("/api/todos/999")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("待办不存在"));
    }

    @Test
    void testCreateTodoWithOrderId() throws Exception {
        when(todoService.createTodo(eq(1L), eq("自习任务"), eq(1), isNull(), eq(10L))).thenReturn(testTodo);

        Map<String, Object> body = new HashMap<>();
        body.put("title", "自习任务");
        body.put("priority", 1);
        body.put("orderId", 10);

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建成功"));

        verify(todoService, times(1)).createTodo(eq(1L), eq("自习任务"), eq(1), isNull(), eq(10L));
    }
}
