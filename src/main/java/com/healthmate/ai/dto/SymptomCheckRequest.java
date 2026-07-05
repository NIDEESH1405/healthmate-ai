package com.healthmate.ai.dto;

import java.util.List;

public class SymptomCheckRequest {

    private List<String> bodyAreas;
    private int severity = 5;
    private String description;

    public List<String> getBodyAreas() {
        return bodyAreas;
    }

    public void setBodyAreas(List<String> bodyAreas) {
        this.bodyAreas = bodyAreas;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
