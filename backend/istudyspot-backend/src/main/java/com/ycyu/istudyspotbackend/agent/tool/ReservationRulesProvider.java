package com.ycyu.istudyspotbackend.agent.tool;

import com.ycyu.istudyspotbackend.ai.AiRules;
import com.ycyu.istudyspotbackend.ai.AiRulesRegistry;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ReservationRulesProvider {

    public Map<String, Object> getRules() {
        AiRules.ReservationRules source = AiRulesRegistry.getRules().getAgent().getReservationRules();
        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("maxAdvanceDays", source.getMaxAdvanceDays());
        rules.put("maxDailyReservations", source.getMaxDailyReservations());
        rules.put("maxDurationHours", source.getMaxDurationHours());
        rules.put("minDurationMinutes", source.getMinDurationMinutes());
        rules.put("cancellationDeadlineMinutes", source.getCancellationDeadlineMinutes());
        rules.put("noShowPenalty", source.getNoShowPenalty());
        return Map.copyOf(rules);
    }
}
