package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Todo;
import com.ycyu.istudyspotbackend.mapper.TodoMapper;
import com.ycyu.istudyspotbackend.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private TodoMapper todoMapper;

    @Override
    public Todo createTodo(Long userId, String title, Integer priority, LocalDateTime dueTime, Long orderId) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("待办标题不能为空");
        }
        if (title.length() > 100) {
            throw new RuntimeException("待办标题不能超过100个字符");
        }

        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setTitle(title.trim());
        todo.setPriority(priority != null ? priority : 2);
        todo.setStatus("pending");
        todo.setDueTime(dueTime);
        todo.setOrderId(orderId);
        todoMapper.insert(todo);
        return todo;
    }

    @Override
    public List<Todo> getTodoList(Long userId, String status) {
        if (status != null && !status.isEmpty()) {
            return todoMapper.findByUserIdAndStatus(userId, status);
        }
        return todoMapper.findByUserId(userId);
    }

    @Override
    public Todo updateTodo(Long todoId, Long userId, String title, Integer priority, LocalDateTime dueTime, Long orderId) {
        Todo todo = todoMapper.findById(todoId);
        if (todo == null) {
            throw new RuntimeException("待办不存在");
        }
        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此待办");
        }
        if (title != null && !title.trim().isEmpty()) {
            if (title.length() > 100) {
                throw new RuntimeException("待办标题不能超过100个字符");
            }
            todo.setTitle(title.trim());
        }
        if (priority != null) {
            todo.setPriority(priority);
        }
        todo.setDueTime(dueTime);
        todo.setOrderId(orderId);
        todoMapper.update(todo);
        return todoMapper.findById(todoId);
    }

    @Override
    public Todo toggleTodo(Long todoId, Long userId) {
        Todo todo = todoMapper.findById(todoId);
        if (todo == null) {
            throw new RuntimeException("待办不存在");
        }
        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此待办");
        }

        String newStatus = "completed".equals(todo.getStatus()) ? "pending" : "completed";
        LocalDateTime completedAt = "completed".equals(newStatus) ? LocalDateTime.now() : null;
        todoMapper.updateStatus(todoId, newStatus, completedAt);

        return todoMapper.findById(todoId);
    }

    @Override
    public void deleteTodo(Long todoId, Long userId) {
        Todo todo = todoMapper.findById(todoId);
        if (todo == null) {
            throw new RuntimeException("待办不存在");
        }
        if (!todo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此待办");
        }
        todoMapper.deleteById(todoId);
    }
}
