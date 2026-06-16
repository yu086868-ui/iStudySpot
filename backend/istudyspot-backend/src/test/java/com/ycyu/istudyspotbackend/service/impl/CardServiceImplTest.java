package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Card;
import com.ycyu.istudyspotbackend.mapper.CardMapper;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {

    @Mock
    private CardMapper cardMapper;

    @Mock
    private DeepSeekService deepSeekService;

    @InjectMocks
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cardService, "storagePath", "./uploads/cards");
        ReflectionTestUtils.setField(cardService, "baseUrl", "/api/wx/card/image");
    }

    @Test
    void testGenerateCard() {
        when(deepSeekService.generateCardContent(anyString(), anyString()))
                .thenReturn("# 坚持\n> 坚持是通往成功的桥梁\n---\n学习需要持之以恒...");

        doAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return 1;
        }).when(cardMapper).insertCard(any(Card.class));

        Card card = cardService.generateCard("user123", 45);

        assertNotNull(card);
        assertNotNull(card.getUuid());
        assertNotNull(card.getCardId());
        assertEquals("user123", card.getUserId());
        assertEquals(45, card.getStudyDuration());
        assertNotNull(card.getRarity());
        assertNotNull(card.getMarkdown());
        assertNotNull(card.getImageUrl());
        assertNotNull(card.getCreateTime());
        assertNotNull(card.getBorderTheme());
        assertNotNull(card.getCardTheme());
        assertNotNull(card.getThemeCategory());

        verify(deepSeekService, times(1)).generateCardContent(anyString(), anyString());
        verify(cardMapper, times(1)).insertCard(any(Card.class));
    }

    @Test
    void testGenerateCardWithShortDuration() {
        when(deepSeekService.generateCardContent(anyString(), anyString()))
                .thenReturn("# 努力\n> 天道酬勤\n---\n努力是成功的基石...");

        doAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return 1;
        }).when(cardMapper).insertCard(any(Card.class));

        Card card = cardService.generateCard("user123", 5);

        assertNotNull(card);
        assertEquals("N", card.getRarity());
        assertEquals("白/灰", card.getBorderTheme());
        assertEquals("普通", card.getCardTheme());
    }

    @Test
    void testGenerateCardWithLongDuration() {
        when(deepSeekService.generateCardContent(anyString(), anyString()))
                .thenReturn("# 卓越\n> 追求卓越，成功自然随之而来\n---\n持续学习是一种习惯...");

        doAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return 1;
        }).when(cardMapper).insertCard(any(Card.class));

        Card card = cardService.generateCard("user123", 200);

        assertNotNull(card);
        assertEquals(200, card.getStudyDuration());
        assertNotNull(card.getRarity());
    }

    @Test
    void testGetCardByUuid() {
        Card expectedCard = new Card();
        expectedCard.setUuid("test-uuid");
        expectedCard.setUserId("user123");
        expectedCard.setRarity("SR");

        when(cardMapper.selectCardByUuid("test-uuid")).thenReturn(expectedCard);

        Card card = cardService.getCardByUuid("test-uuid");

        assertNotNull(card);
        assertEquals("test-uuid", card.getUuid());
        assertEquals("user123", card.getUserId());
        assertEquals("SR", card.getRarity());

        verify(cardMapper, times(1)).selectCardByUuid("test-uuid");
    }

    @Test
    void testGetCardByUuidNotFound() {
        when(cardMapper.selectCardByUuid("nonexistent")).thenReturn(null);

        Card card = cardService.getCardByUuid("nonexistent");

        assertNull(card);
        verify(cardMapper, times(1)).selectCardByUuid("nonexistent");
    }

    @Test
    void testGetCardsByUserId() {
        Card card1 = new Card();
        card1.setUuid("uuid-1");
        card1.setUserId("user123");
        card1.setCreateTime(java.time.LocalDateTime.now().minusHours(1));

        Card card2 = new Card();
        card2.setUuid("uuid-2");
        card2.setUserId("user123");
        card2.setCreateTime(java.time.LocalDateTime.now());

        when(cardMapper.selectCardsByUserId("user123")).thenReturn(Arrays.asList(card1, card2));

        List<Card> cards = cardService.getCardsByUserId("user123");

        assertNotNull(cards);
        assertEquals(2, cards.size());
        // 应按时间倒序排列
        assertEquals("uuid-2", cards.get(0).getUuid());
        assertEquals("uuid-1", cards.get(1).getUuid());

        verify(cardMapper, times(1)).selectCardsByUserId("user123");
    }

    @Test
    void testGetCardsByUserIdEmpty() {
        when(cardMapper.selectCardsByUserId("user123")).thenReturn(Arrays.asList());

        List<Card> cards = cardService.getCardsByUserId("user123");

        assertNotNull(cards);
        assertTrue(cards.isEmpty());
        verify(cardMapper, times(1)).selectCardsByUserId("user123");
    }

    @Test
    void testGenerateCardData() {
        when(deepSeekService.generateCardContent(eq("SR"), anyString()))
                .thenReturn("# 哲理\n> 思而悟道\n---\n思考带来智慧...");

        Map<String, Object> result = cardService.generateCardData("SR");

        assertNotNull(result);
        assertTrue(result.containsKey("markdown"));
        assertTrue(result.containsKey("imageUrl"));
        assertEquals("# 哲理\n> 思而悟道\n---\n思考带来智慧...", result.get("markdown"));

        verify(deepSeekService, times(1)).generateCardContent(eq("SR"), anyString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGenerateCardStreamInitData() {
        Consumer<Map<String, Object>> onData = mock(Consumer.class);
        Consumer<Map<String, Object>> onComplete = mock(Consumer.class);
        Consumer<Throwable> onError = mock(Consumer.class);

        doAnswer(invocation -> {
            Consumer<String> chunkCallback = invocation.getArgument(2);
            chunkCallback.accept("测试内容");
            Runnable onCompleteCallback = invocation.getArgument(3);
            onCompleteCallback.run();
            return null;
        }).when(deepSeekService).streamChat(
                eq("deepseek-chat"), anyList(), any(Consumer.class), any(Runnable.class), any(Consumer.class));

        cardService.generateCardStream("user123", 30, onData, onComplete, onError);

        verify(onData, timeout(5000).atLeast(1)).accept(anyMap());
    }
}