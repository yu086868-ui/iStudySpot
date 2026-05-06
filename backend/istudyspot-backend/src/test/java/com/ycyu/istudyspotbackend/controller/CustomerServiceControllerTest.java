package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.CustomerServiceMessage;
import com.ycyu.istudyspotbackend.service.CustomerServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerServiceControllerTest {

    @Mock
    private CustomerServiceService customerServiceService;

    @InjectMocks
    private CustomerServiceController customerServiceController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetWelcomeInfo() {
        String expectedWelcomeMessage = "Welcome to customer service!";
        ArrayList<String> expectedRecommendedQuestions = new ArrayList<>();
        expectedRecommendedQuestions.add("How to book a seat?");
        expectedRecommendedQuestions.add("What are the opening hours?");

        when(customerServiceService.getWelcomeMessage()).thenReturn(expectedWelcomeMessage);
        when(customerServiceService.getRecommendedQuestions()).thenReturn(expectedRecommendedQuestions);

        ResponseEntity<Map<String, Object>> response = customerServiceController.getWelcomeInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedWelcomeMessage, response.getBody().get("welcomeMessage"));
        assertEquals(expectedRecommendedQuestions, response.getBody().get("recommendedQuestions"));
        verify(customerServiceService, times(1)).getWelcomeMessage();
        verify(customerServiceService, times(1)).getRecommendedQuestions();
    }

    @Test
    public void testChatWithCustomerService() {
        String sessionId = "session123";
        String message = "Test message";
        String expectedResponse = "Hello, how can I help you?";

        when(customerServiceService.chatWithCustomerService(sessionId, message)).thenReturn(expectedResponse);

        Map<String, String> request = new HashMap<>();
        request.put("sessionId", sessionId);
        request.put("message", message);

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody().get("response"));
        verify(customerServiceService, times(1)).chatWithCustomerService(sessionId, message);
    }

    @Test
    public void testChatWithCustomerServiceEmptySessionId() {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", "");
        request.put("message", "Test message");

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("EMPTY_SESSION_ID", response.getBody().get("error"));
    }

    @Test
    public void testChatWithCustomerServiceNullSessionId() {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", null);
        request.put("message", "Test message");

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("EMPTY_SESSION_ID", response.getBody().get("error"));
    }

    @Test
    public void testChatWithCustomerServiceEmptyMessage() {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", "session123");
        request.put("message", "");

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("EMPTY_MESSAGE", response.getBody().get("error"));
    }

    @Test
    public void testChatWithCustomerServiceNullMessage() {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", "session123");
        request.put("message", null);

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("EMPTY_MESSAGE", response.getBody().get("error"));
    }

    @Test
    public void testChatWithCustomerServiceServiceException() {
        String sessionId = "session123";
        String message = "Test message";

        when(customerServiceService.chatWithCustomerService(sessionId, message))
                .thenThrow(new RuntimeException("Service error"));

        Map<String, String> request = new HashMap<>();
        request.put("sessionId", sessionId);
        request.put("message", message);

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().get("error"));
        assertTrue(response.getBody().get("detail").toString().contains("Service error"));
    }

    @Test
    public void testStreamChatWithCustomerService() throws IOException {
        String sessionId = "session123";
        String message = "Test message";
        SseEmitter expectedEmitter = new SseEmitter();

        when(customerServiceService.streamChatWithCustomerService(sessionId, message)).thenReturn(expectedEmitter);

        Map<String, String> request = new HashMap<>();
        request.put("sessionId", sessionId);
        request.put("message", message);

        SseEmitter emitter = customerServiceController.streamChatWithCustomerService(request);

        assertNotNull(emitter);
        verify(customerServiceService, times(1)).streamChatWithCustomerService(sessionId, message);
    }

    @Test
    public void testStreamChatWithCustomerServiceEmptySessionId() throws IOException {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", "");
        request.put("message", "Test message");

        SseEmitter emitter = customerServiceController.streamChatWithCustomerService(request);

        assertNotNull(emitter);
    }

    @Test
    public void testStreamChatWithCustomerServiceNullSessionId() throws IOException {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", null);
        request.put("message", "Test message");

        SseEmitter emitter = customerServiceController.streamChatWithCustomerService(request);

        assertNotNull(emitter);
    }

    @Test
    public void testStreamChatWithCustomerServiceEmptyMessage() throws IOException {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", "session123");
        request.put("message", "");

        SseEmitter emitter = customerServiceController.streamChatWithCustomerService(request);

        assertNotNull(emitter);
    }

    @Test
    public void testStreamChatWithCustomerServiceNullMessage() throws IOException {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", "session123");
        request.put("message", null);

        SseEmitter emitter = customerServiceController.streamChatWithCustomerService(request);

        assertNotNull(emitter);
    }

    @Test
    public void testGetSessionHistory() {
        String sessionId = "session123";
        List<CustomerServiceMessage> expectedMessages = new ArrayList<>();
        
        CustomerServiceMessage message1 = new CustomerServiceMessage();
        message1.setId("1");
        message1.setSessionId(sessionId);
        message1.setRole("user");
        message1.setContent("Hello");
        message1.setTimestamp(LocalDateTime.now());
        expectedMessages.add(message1);

        CustomerServiceMessage message2 = new CustomerServiceMessage();
        message2.setId("2");
        message2.setSessionId(sessionId);
        message2.setRole("assistant");
        message2.setContent("How can I help you?");
        message2.setTimestamp(LocalDateTime.now());
        expectedMessages.add(message2);

        when(customerServiceService.getSessionHistory(sessionId)).thenReturn(expectedMessages);

        ResponseEntity<Map<String, Object>> response = customerServiceController.getSessionHistory(sessionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessages, response.getBody().get("messages"));
        verify(customerServiceService, times(1)).getSessionHistory(sessionId);
    }

    @Test
    public void testGetSessionHistoryEmpty() {
        String sessionId = "empty-session";
        List<CustomerServiceMessage> expectedMessages = new ArrayList<>();

        when(customerServiceService.getSessionHistory(sessionId)).thenReturn(expectedMessages);

        ResponseEntity<Map<String, Object>> response = customerServiceController.getSessionHistory(sessionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessages, response.getBody().get("messages"));
        verify(customerServiceService, times(1)).getSessionHistory(sessionId);
    }

    @Test
    public void testGetSessionHistoryServiceException() {
        String sessionId = "error-session";

        when(customerServiceService.getSessionHistory(sessionId))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            customerServiceController.getSessionHistory(sessionId);
        });
    }

    @Test
    public void testChatWithCustomerServiceSpecialCharacters() {
        String sessionId = "session123";
        String message = "预订自习室座位！@#$%^&*()";
        String expectedResponse = "好的，我来帮您预订。";

        when(customerServiceService.chatWithCustomerService(sessionId, message)).thenReturn(expectedResponse);

        Map<String, String> request = new HashMap<>();
        request.put("sessionId", sessionId);
        request.put("message", message);

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody().get("response"));
    }

    @Test
    public void testChatWithCustomerServiceLongMessage() {
        String sessionId = "session123";
        String longMessage = "我想了解一下自习室的预订流程，包括如何选择座位、支付方式、取消政策以及积分使用规则。";
        String expectedResponse = "好的，我来为您详细解答。";

        when(customerServiceService.chatWithCustomerService(sessionId, longMessage)).thenReturn(expectedResponse);

        Map<String, String> request = new HashMap<>();
        request.put("sessionId", sessionId);
        request.put("message", longMessage);

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody().get("response"));
    }
}