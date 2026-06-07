package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Todo;

import java.util.List;

public interface TodoService {
    Todo createTodo(Long userId, String title, Integer priority, java.time.LocalDateTime dueTime, Long orderId);
    List<Todo> getTodoList(Long userId, String status);
    Todo updateTodo(Long todoId, Long userId, String title, Integer priority, java.time.LocalDateTime dueTime, Long orderId);
    Todo toggleTodo(Long todoId, Long userId);
    void deleteTodo(Long todoId, Long userId);
}
