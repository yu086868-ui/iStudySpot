package com.ycyu.istudyspotbackend.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ycyu.istudyspotbackend.entity.AICharacter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRules {
    private Conversation conversation = new Conversation();
    private List<CharacterRule> characters = new ArrayList<>();
    private CustomerService customerService = new CustomerService();
    private Agent agent = new Agent();

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation != null ? conversation : new Conversation();
    }

    public List<CharacterRule> getCharacters() {
        return characters;
    }

    public void setCharacters(List<CharacterRule> characters) {
        this.characters = characters != null ? characters : new ArrayList<>();
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService != null ? customerService : new CustomerService();
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent != null ? agent : new Agent();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Conversation {
        private int historyLimit = 10;
        private String defaultCharacterId = "customer_service";
        private String fallbackReply = "我先帮你整理一下思路。你可以再具体描述一下你的学习目标、卡住的点，或者你想解决的自习室使用问题。";
        private List<String> systemGuidelines = new ArrayList<>();
        private List<String> responseDirectives = new ArrayList<>();

        public int getHistoryLimit() {
            return historyLimit;
        }

        public void setHistoryLimit(int historyLimit) {
            this.historyLimit = historyLimit > 0 ? historyLimit : 10;
        }

        public String getDefaultCharacterId() {
            return defaultCharacterId;
        }

        public void setDefaultCharacterId(String defaultCharacterId) {
            this.defaultCharacterId = defaultCharacterId;
        }

        public String getFallbackReply() {
            return fallbackReply;
        }

        public void setFallbackReply(String fallbackReply) {
            this.fallbackReply = fallbackReply;
        }

        public List<String> getSystemGuidelines() {
            return Collections.unmodifiableList(systemGuidelines);
        }

        public void setSystemGuidelines(List<String> systemGuidelines) {
            this.systemGuidelines = systemGuidelines != null ? systemGuidelines : new ArrayList<>();
        }

        public List<String> getResponseDirectives() {
            return Collections.unmodifiableList(responseDirectives);
        }

        public void setResponseDirectives(List<String> responseDirectives) {
            this.responseDirectives = responseDirectives != null ? responseDirectives : new ArrayList<>();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CharacterRule {
        private String id;
        private String name;
        private String persona;
        private String speakingStyle;
        private String fallbackReply;
        private List<String> specialties = new ArrayList<>();
        private List<String> rules = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPersona() {
            return persona;
        }

        public void setPersona(String persona) {
            this.persona = persona;
        }

        public String getSpeakingStyle() {
            return speakingStyle;
        }

        public void setSpeakingStyle(String speakingStyle) {
            this.speakingStyle = speakingStyle;
        }

        public String getFallbackReply() {
            return fallbackReply;
        }

        public void setFallbackReply(String fallbackReply) {
            this.fallbackReply = fallbackReply;
        }

        public List<String> getSpecialties() {
            return Collections.unmodifiableList(specialties);
        }

        public void setSpecialties(List<String> specialties) {
            this.specialties = specialties != null ? specialties : new ArrayList<>();
        }

        public List<String> getRules() {
            return Collections.unmodifiableList(rules);
        }

        public void setRules(List<String> rules) {
            this.rules = rules != null ? rules : new ArrayList<>();
        }

        public AICharacter toCharacter() {
            return new AICharacter(id, name, persona, speakingStyle);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerService {
        private String assistantName = "小i";
        private String welcomeMessage = "";
        private String fallbackReply = "我这边暂时没有稳定拿到答案。你可以换一种问法，或者先查看规则页与订单页信息；如果问题紧急，建议联系人工客服。";
        private List<String> recommendedQuestions = new ArrayList<>();
        private List<String> serviceFacts = new ArrayList<>();
        private List<String> responseRules = new ArrayList<>();

        public String getAssistantName() {
            return assistantName;
        }

        public void setAssistantName(String assistantName) {
            this.assistantName = assistantName;
        }

        public String getWelcomeMessage() {
            return welcomeMessage;
        }

        public void setWelcomeMessage(String welcomeMessage) {
            this.welcomeMessage = welcomeMessage;
        }

        public String getFallbackReply() {
            return fallbackReply;
        }

        public void setFallbackReply(String fallbackReply) {
            this.fallbackReply = fallbackReply;
        }

        public List<String> getRecommendedQuestions() {
            return Collections.unmodifiableList(recommendedQuestions);
        }

        public void setRecommendedQuestions(List<String> recommendedQuestions) {
            this.recommendedQuestions = recommendedQuestions != null ? recommendedQuestions : new ArrayList<>();
        }

        public List<String> getServiceFacts() {
            return Collections.unmodifiableList(serviceFacts);
        }

        public void setServiceFacts(List<String> serviceFacts) {
            this.serviceFacts = serviceFacts != null ? serviceFacts : new ArrayList<>();
        }

        public List<String> getResponseRules() {
            return Collections.unmodifiableList(responseRules);
        }

        public void setResponseRules(List<String> responseRules) {
            this.responseRules = responseRules != null ? responseRules : new ArrayList<>();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Agent {
        private ReservationRules reservationRules = new ReservationRules();
        private List<String> safetyRules = new ArrayList<>();
        private List<String> privacyRules = new ArrayList<>();
        private List<String> toolRules = new ArrayList<>();

        public ReservationRules getReservationRules() {
            return reservationRules;
        }

        public void setReservationRules(ReservationRules reservationRules) {
            this.reservationRules = reservationRules != null ? reservationRules : new ReservationRules();
        }

        public List<String> getSafetyRules() {
            return Collections.unmodifiableList(safetyRules);
        }

        public void setSafetyRules(List<String> safetyRules) {
            this.safetyRules = safetyRules != null ? safetyRules : new ArrayList<>();
        }

        public List<String> getPrivacyRules() {
            return Collections.unmodifiableList(privacyRules);
        }

        public void setPrivacyRules(List<String> privacyRules) {
            this.privacyRules = privacyRules != null ? privacyRules : new ArrayList<>();
        }

        public List<String> getToolRules() {
            return Collections.unmodifiableList(toolRules);
        }

        public void setToolRules(List<String> toolRules) {
            this.toolRules = toolRules != null ? toolRules : new ArrayList<>();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReservationRules {
        private int maxAdvanceDays = 7;
        private int maxDailyReservations = 2;
        private int maxDurationHours = 4;
        private int minDurationMinutes = 30;
        private int cancellationDeadlineMinutes = 15;
        private int noShowPenalty = 5;

        public int getMaxAdvanceDays() {
            return maxAdvanceDays;
        }

        public void setMaxAdvanceDays(int maxAdvanceDays) {
            this.maxAdvanceDays = positiveOrDefault(maxAdvanceDays, 7);
        }

        public int getMaxDailyReservations() {
            return maxDailyReservations;
        }

        public void setMaxDailyReservations(int maxDailyReservations) {
            this.maxDailyReservations = positiveOrDefault(maxDailyReservations, 2);
        }

        public int getMaxDurationHours() {
            return maxDurationHours;
        }

        public void setMaxDurationHours(int maxDurationHours) {
            this.maxDurationHours = positiveOrDefault(maxDurationHours, 4);
        }

        public int getMinDurationMinutes() {
            return minDurationMinutes;
        }

        public void setMinDurationMinutes(int minDurationMinutes) {
            this.minDurationMinutes = positiveOrDefault(minDurationMinutes, 30);
        }

        public int getCancellationDeadlineMinutes() {
            return cancellationDeadlineMinutes;
        }

        public void setCancellationDeadlineMinutes(int cancellationDeadlineMinutes) {
            this.cancellationDeadlineMinutes = positiveOrDefault(cancellationDeadlineMinutes, 15);
        }

        public int getNoShowPenalty() {
            return noShowPenalty;
        }

        public void setNoShowPenalty(int noShowPenalty) {
            this.noShowPenalty = positiveOrDefault(noShowPenalty, 5);
        }

        private int positiveOrDefault(int value, int fallback) {
            return value > 0 ? value : fallback;
        }
    }
}
