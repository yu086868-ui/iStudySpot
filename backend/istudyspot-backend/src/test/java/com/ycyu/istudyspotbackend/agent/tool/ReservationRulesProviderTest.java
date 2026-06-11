package com.ycyu.istudyspotbackend.agent.tool;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationRulesProviderTest {

    @Test
    void rulesShouldLoadFromAiRulesResource() {
        ReservationRulesProvider provider = new ReservationRulesProvider();

        Map<String, Object> rules = provider.getRules();

        assertEquals(7, rules.get("maxAdvanceDays"));
        assertEquals(2, rules.get("maxDailyReservations"));
        assertEquals(4, rules.get("maxDurationHours"));
        assertEquals(30, rules.get("minDurationMinutes"));
        assertEquals(15, rules.get("cancellationDeadlineMinutes"));
        assertEquals(5, rules.get("noShowPenalty"));
    }
}
