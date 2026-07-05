package com.healthmate.ai.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the in-progress state of a single Symptom Checker conversation (Agent 2).
 * Stored as a HttpSession attribute; never persisted to the database.
 */
public class SymptomSession implements Serializable {

    private List<String> bodyAreas = new ArrayList<>();
    private int severity;
    private String initialDescription;
    private final List<ChatMessage> conversation = new ArrayList<>();
    private int clarifyingQuestionsAsked = 0;
    private boolean assessmentComplete = false;
    private String specialistType;
    private String urgencyLevel;
    private String assessmentSummary;

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

    public String getInitialDescription() {
        return initialDescription;
    }

    public void setInitialDescription(String initialDescription) {
        this.initialDescription = initialDescription;
    }

    public List<ChatMessage> getConversation() {
        return conversation;
    }

    public int getClarifyingQuestionsAsked() {
        return clarifyingQuestionsAsked;
    }

    public void incrementClarifyingQuestions() {
        this.clarifyingQuestionsAsked++;
    }

    public boolean isAssessmentComplete() {
        return assessmentComplete;
    }

    public void setAssessmentComplete(boolean assessmentComplete) {
        this.assessmentComplete = assessmentComplete;
    }

    public String getSpecialistType() {
        return specialistType;
    }

    public void setSpecialistType(String specialistType) {
        this.specialistType = specialistType;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getAssessmentSummary() {
        return assessmentSummary;
    }

    public void setAssessmentSummary(String assessmentSummary) {
        this.assessmentSummary = assessmentSummary;
    }
}
