package com.ycyu.istudyspotbackend.entity;

public class Character {
    private String id;
    private String name;
    private String persona;
    private String speaking_style;

    public Character() {}

    public Character(String id, String name, String persona, String speaking_style) {
        this.id = id;
        this.name = name;
        this.persona = persona;
        this.speaking_style = speaking_style;
    }

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

    public String getSpeaking_style() {
        return speaking_style;
    }

    public void setSpeaking_style(String speaking_style) {
        this.speaking_style = speaking_style;
    }
}
