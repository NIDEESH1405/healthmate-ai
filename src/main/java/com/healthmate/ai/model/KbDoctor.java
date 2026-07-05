package com.healthmate.ai.model;

import java.util.List;

public class KbDoctor {

    private String name;
    private List<String> availability;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAvailability() {
        return availability;
    }

    public void setAvailability(List<String> availability) {
        this.availability = availability;
    }
}
