package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Card;
import com.ycyu.istudyspotbackend.mapper.CardMapper;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardServiceImplTest {

    @Mock
    private CardMapper cardMapper;

    @Mock
    private DeepSeekService deepSeekService;

    @InjectMocks
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cardService, "storagePath", "./target/test-uploads/cards");
        ReflectionTestUtils.setField(cardService, "baseUrl", "/api/wx/card/image");
    }

    @Test
    void testGetCardByUuid() {
        Card card = new Card();
        card.setUuid("test-uuid");
        card.setUserId("user1");
        card.setRarity("SR");

        when(cardMapper.selectCardByUuid("test-uuid")).thenReturn(card);

        Card result = cardService.getCardByUuid("test-uuid");

        assertNotNull(result);
        assertEquals("test-uuid", result.getUuid());
        assertEquals("SR", result.getRarity());
        verify(cardMapper).selectCardByUuid("test-uuid");
    }

    @Test
    void testGetCardByUuid_NotFound() {
        when(cardMapper.selectCardByUuid("nonexistent")).thenReturn(null);

        Card result = cardService.getCardByUuid("nonexistent");

        assertNull(result);
    }

    @Test
    void testGetCardsByUserId() {
        Card card1 = new Card();
        card1.setUuid("uuid-1");
        card1.setCreateTime(LocalDateTime.now().minusHours(1));

        Card card2 = new Card();
        card2.setUuid("uuid-2");
        card2.setCreateTime(LocalDateTime.now());

        when(cardMapper.selectCardsByUserId("user1")).thenReturn(Arrays.asList(card1, card2));

        List<Card> result = cardService.getCardsByUserId("user1");

        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be sorted by createTime descending
        assertEquals("uuid-2", result.get(0).getUuid());
        assertEquals("uuid-1", result.get(1).getUuid());
    }

    @Test
    void testGetCardsByUserId_Empty() {
        when(cardMapper.selectCardsByUserId("user1")).thenReturn(Collections.emptyList());

        List<Card> result = cardService.getCardsByUserId("user1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCardsByUserId_SingleCard() {
        Card card = new Card();
        card.setUuid("uuid-1");
        card.setCreateTime(LocalDateTime.now());

        when(cardMapper.selectCardsByUserId("user1")).thenReturn(new ArrayList<>(List.of(card)));

        List<Card> result = cardService.getCardsByUserId("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGenerateCard() {
        when(deepSeekService.generateCardContent(anyString(), anyString()))
                .thenReturn("# 测试标题\n> 测试引用\n---\n测试正文内容");

        Card result = cardService.generateCard("user1", 45);

        assertNotNull(result);
        assertNotNull(result.getUuid());
        assertNotNull(result.getCardId());
        assertEquals("user1", result.getUserId());
        assertEquals(45, result.getStudyDuration());
        assertNotNull(result.getRarity());
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getBorderTheme());
        assertNotNull(result.getCardTheme());
        assertNotNull(result.getThemeCategory());
        assertEquals("# 测试标题\n> 测试引用\n---\n测试正文内容", result.getMarkdown());
        assertNotNull(result.getImageUrl());

        verify(deepSeekService).generateCardContent(anyString(), anyString());
        verify(cardMapper).insertCard(any(Card.class));
    }

    @Test
    void testGenerateCard_DifferentDurations() {
        when(deepSeekService.generateCardContent(anyString(), anyString()))
                .thenReturn("Content");

        // Test short duration
        Card shortCard = cardService.generateCard("user1", 5);
        assertNotNull(shortCard.getRarity());

        // Test medium duration
        Card mediumCard = cardService.generateCard("user2", 45);
        assertNotNull(mediumCard.getRarity());

        // Test long duration
        Card longCard = cardService.generateCard("user3", 200);
        assertNotNull(longCard.getRarity());
    }

    @Test
    void testGenerateCardData() {
        when(deepSeekService.generateCardContent(anyString(), anyString()))
                .thenReturn("# 数据\n> 引用\n---\n内容");

        Map<String, Object> result = cardService.generateCardData("SR");

        assertNotNull(result);
        assertTrue(result.containsKey("markdown"));
        assertTrue(result.containsKey("imageUrl"));
        assertEquals("# 数据\n> 引用\n---\n内容", result.get("markdown"));
        assertNotNull(result.get("imageUrl"));

        verify(deepSeekService).generateCardContent(eq("SR"), anyString());
    }

    @Test
    void testGenerateCardData_AllRarities() {
        when(deepSeekService.generateCardContent(anyString(), anyString()))
                .thenReturn("Content");

        String[] rarities = {"N", "R", "SR", "SSR", "UR", "LR"};
        for (String rarity : rarities) {
            Map<String, Object> result = cardService.generateCardData(rarity);
            assertNotNull(result);
            assertTrue(result.containsKey("markdown"));
            assertTrue(result.containsKey("imageUrl"));
            verify(deepSeekService, atLeastOnce()).generateCardContent(eq(rarity), anyString());
        }
    }

    @Test
    void testGenerateCardStream() throws Exception {
        CountDownLatch completeLatch = new CountDownLatch(1);
        AtomicReference<Map<String, Object>> completeData = new AtomicReference<>();
        List<Map<String, Object>> receivedData = new ArrayList<>();

        doAnswer(invocation -> {
            String model = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = invocation.getArgument(1);
            java.util.function.Consumer<String> onMessage = invocation.getArgument(2);
            Runnable onComplete = invocation.getArgument(3);

            // Simulate streaming chunks
            onMessage.accept("{\"type\": \"delta\", \"content\": \"# 标题\"}");
            onMessage.accept("{\"type\": \"delta\", \"content\": \"\\n> 引用\"}");
            onMessage.accept("{\"type\": \"end\"}");
            onComplete.run();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        cardService.generateCardStream("user1", 60,
            data -> receivedData.add(data),
            data -> {
                completeData.set(data);
                completeLatch.countDown();
            },
            error -> {
                completeLatch.countDown();
            });

        boolean completed = completeLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed);

        assertNotNull(completeData.get());
        assertTrue((Boolean) completeData.get().get("success"));
        assertEquals("generate success", completeData.get().get("message"));

        @SuppressWarnings("unchecked")
        Map<String, Object> card = (Map<String, Object>) completeData.get().get("card");
        assertNotNull(card);
        assertNotNull(card.get("uuid"));
        assertNotNull(card.get("rarity"));
        assertNotNull(card.get("themeCategory"));
        assertNotNull(card.get("markdown"));
        assertNotNull(card.get("imageURL"));

        verify(cardMapper).insertCard(any(Card.class));
    }

    @Test
    void testGenerateCardStream_WithError() throws Exception {
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        doThrow(new RuntimeException("API Error"))
            .when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        cardService.generateCardStream("user1", 60,
            data -> {},
            data -> {},
            error -> {
                errorRef.set(error);
                errorLatch.countDown();
            });

        boolean completed = errorLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed);
        assertNotNull(errorRef.get());
        assertTrue(errorRef.get() instanceof RuntimeException);
    }

    @Test
    void testGenerateCardStream_ShortDuration() throws Exception {
        CountDownLatch completeLatch = new CountDownLatch(1);
        AtomicReference<Map<String, Object>> completeData = new AtomicReference<>();

        doAnswer(invocation -> {
            java.util.function.Consumer<String> onMessage = invocation.getArgument(2);
            Runnable onComplete = invocation.getArgument(3);
            onMessage.accept("{\"type\": \"delta\", \"content\": \"content\"}");
            onMessage.accept("{\"type\": \"end\"}");
            onComplete.run();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        cardService.generateCardStream("user1", 5,
            data -> {},
            data -> {
                completeData.set(data);
                completeLatch.countDown();
            },
            error -> completeLatch.countDown());

        boolean completed = completeLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed);
        assertNotNull(completeData.get());

        @SuppressWarnings("unchecked")
        Map<String, Object> card = (Map<String, Object>) completeData.get().get("card");
        assertNotNull(card);
    }

    @Test
    void testGenerateCardStream_LongDuration() throws Exception {
        CountDownLatch completeLatch = new CountDownLatch(1);
        AtomicReference<Map<String, Object>> completeData = new AtomicReference<>();

        doAnswer(invocation -> {
            java.util.function.Consumer<String> onMessage = invocation.getArgument(2);
            Runnable onComplete = invocation.getArgument(3);
            onMessage.accept("{\"type\": \"delta\", \"content\": \"epic\"}");
            onMessage.accept("{\"type\": \"end\"}");
            onComplete.run();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        cardService.generateCardStream("user1", 300,
            data -> {},
            data -> {
                completeData.set(data);
                completeLatch.countDown();
            },
            error -> completeLatch.countDown());

        boolean completed = completeLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed);
        assertNotNull(completeData.get());

        @SuppressWarnings("unchecked")
        Map<String, Object> card = (Map<String, Object>) completeData.get().get("card");
        assertNotNull(card);
    }

    @Test
    void testGenerateCardStream_TextChunks() throws Exception {
        CountDownLatch completeLatch = new CountDownLatch(1);
        List<Map<String, Object>> receivedData = new ArrayList<>();

        doAnswer(invocation -> {
            java.util.function.Consumer<String> onMessage = invocation.getArgument(2);
            Runnable onComplete = invocation.getArgument(3);
            onMessage.accept("{\"type\": \"delta\", \"content\": \"chunk1\"}");
            onMessage.accept("{\"type\": \"delta\", \"content\": \"chunk2\"}");
            onMessage.accept("{\"type\": \"delta\", \"content\": \"chunk3\"}");
            onMessage.accept("{\"type\": \"end\"}");
            onComplete.run();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        cardService.generateCardStream("user1", 60,
            data -> {
                if ("text".equals(data.get("type"))) {
                    receivedData.add(data);
                }
            },
            data -> completeLatch.countDown(),
            error -> completeLatch.countDown());

        boolean completed = completeLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed);
        assertTrue(receivedData.size() >= 3);
    }
}