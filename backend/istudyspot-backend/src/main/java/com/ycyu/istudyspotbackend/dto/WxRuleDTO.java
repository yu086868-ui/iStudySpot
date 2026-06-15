package com.ycyu.istudyspotbackend.dto;

import com.ycyu.istudyspotbackend.entity.Rule;

public class WxRuleDTO {
    private String id;
    private String category;
    private String title;
    private String content;
    private Integer priority;

    public static WxRuleDTO fromEntity(Rule rule) {
        WxRuleDTO dto = new WxRuleDTO();
        dto.setId(rule.getId() != null ? rule.getId().toString() : null);
        dto.setCategory(rule.getCategory());
        dto.setTitle(rule.getTitle());
        dto.setContent(rule.getContent());
        dto.setPriority(rule.getPriority());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
}
