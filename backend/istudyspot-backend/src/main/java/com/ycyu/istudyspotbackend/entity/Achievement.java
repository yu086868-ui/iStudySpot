package com.ycyu.istudyspotbackend.entity;

public class Achievement {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String category;
    private Integer threshold;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }
}
