package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.Todo;
import com.ycyu.istudyspotbackend.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @GetMapping
    public Result<List<Todo>> getTodoList(
            @RequestParam(required = false) String status,
            @RequestAttribute Long userId) {
        List<Todo> todos = todoService.getTodoList(userId, status);
        return Result.success("success", todos);
    }

    @PostMapping
    public Result<Todo> createTodo(
            @RequestBody Map<String, Object> body,
            @RequestAttribute Long userId) {
        try {
            String title = (String) body.get("title");
            Integer priority = body.get("priority") != null ? ((Number) body.get("priority")).intValue() : null;
            LocalDateTime dueTime = parseDateTime(body.get("dueTime"));
            Long orderId = body.get("orderId") != null ? ((Number) body.get("orderId")).longValue() : null;

            Todo todo = todoService.createTodo(userId, title, priority, dueTime, orderId);
            return Result.success("创建成功", todo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Todo> updateTodo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestAttribute Long userId) {
        try {
            String title = (String) body.get("title");
            Integer priority = body.get("priority") != null ? ((Number) body.get("priority")).intValue() : null;
            LocalDateTime dueTime = parseDateTime(body.get("dueTime"));
            Long orderId = body.get("orderId") != null ? ((Number) body.get("orderId")).longValue() : null;

            Todo todo = todoService.updateTodo(id, userId, title, priority, dueTime, orderId);
            return Result.success("更新成功", todo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle")
    public Result<Todo> toggleTodo(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        try {
            Todo todo = todoService.toggleTodo(id, userId);
            return Result.success("操作成功", todo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTodo(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        try {
            todoService.deleteTodo(id, userId);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        try {
            return LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(value.toString());
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
