package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Card;
import com.ycyu.istudyspotbackend.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private Card createCard() {
        Card card = new Card();
        card.setUuid("test-uuid-123");
        card.setRarity("SR");
        card.setBorderTheme("gold");
        card.setCardTheme("励志");
        card.setThemeCategory("励志成长");
        card.setMarkdown("**测试卡片**");
        card.setImageUrl("/uploads/cards/test.png");
        card.setCreateTime(LocalDateTime.now());
        card.setStudyDuration(120);
        return card;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
    }

    @Test
    void testGenerateCard_Success() throws Exception {
        Card card = createCard();
        when(cardService.generateCard(anyString(), anyInt())).thenReturn(card);

        Map<String, Object> request = new HashMap<>();
        request.put("userID", "test-user");
        request.put("studyDuration", 120);

        mockMvc.perform(post("/api/wx/card/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.card.uuid").value("test-uuid-123"));
    }

    @Test
    void testGenerateCard_MissingUserId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("userID", "");
        request.put("studyDuration", 120);

        mockMvc.perform(post("/api/wx/card/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGenerateCard_InvalidDuration() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("userID", "test-user");
        request.put("studyDuration", 0);

        mockMvc.perform(post("/api/wx/card/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetCardDetail_Found() throws Exception {
        Card card = createCard();
        when(cardService.getCardByUuid(anyString())).thenReturn(card);

        mockMvc.perform(get("/api/wx/card/detail")
                .param("id", "test-uuid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.card.uuid").value("test-uuid-123"));
    }

    @Test
    void testGetCardDetail_NotFound() throws Exception {
        when(cardService.getCardByUuid(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/wx/card/detail")
                .param("id", "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCardList() throws Exception {
        Card card = createCard();
        when(cardService.getCardsByUserId(anyString())).thenReturn(List.of(card));

        mockMvc.perform(get("/api/wx/card/list")
                .param("userID", "test-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.list.length()").value(1));
    }
}