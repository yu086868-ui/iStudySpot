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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void testChatWithCustomerServiceEmptyMessage() {
        Map<String, String> request = new HashMap<>();
        request.put("sessionId", "session123");
        request.put("message", "");

        ResponseEntity<Map<String, Object>> response = customerServiceController.chatWithCustomerService(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("EMPTY_MESSAGE", response.getBody().get("error"));
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
    public void testGetSessionHistory() {
        String sessionId = "session123";
        List<CustomerServiceMessage> expectedMessages = new ArrayList<>();
        CustomerServiceMessage message1 = new CustomerServiceMessage();
        message1.setId("1");
        message1.setContent("Hello");
        expectedMessages.add(message1);
        CustomerServiceMessage message2 = new CustomerServiceMessage();
        message2.setId("2");
        message2.setContent("How can I help you?");
        expectedMessages.add(message2);

        when(customerServiceService.getSessionHistory(sessionId)).thenReturn(expectedMessages);

        ResponseEntity<Map<String, Object>> response = customerServiceController.getSessionHistory(sessionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedMessages, response.getBody().get("messages"));
        verify(customerServiceService, times(1)).getSessionHistory(sessionId);
    }
}