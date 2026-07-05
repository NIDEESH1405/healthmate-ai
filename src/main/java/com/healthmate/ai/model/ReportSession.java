package com.healthmate.ai.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the in-progress state of a single Medical Report Summarizer session (Agent 3).
 * The extracted PDF text and full analysis are kept ONLY in the HttpSession (in memory)
 * so that follow-up questions can be grounded in the report — this is never written to
 * the database. Only a ReportHistory row (file name, summary, flagged count) is persisted.
 */
public class ReportSession implements Serializable {

    private String fileName;
    private String extractedText;
    private String summary;
    private List<ReportFinding> findings = new ArrayList<>();
    private final List<ChatMessage> followUpConversation = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<ReportFinding> getFindings() {
        return findings;
    }

    public void setFindings(List<ReportFinding> findings) {
        this.findings = findings;
    }

    public List<ChatMessage> getFollowUpConversation() {
        return followUpConversation;
    }

    public long getFlaggedCount() {
        return findings.stream()
                .filter(f -> f.getStatus() != null && !f.getStatus().equalsIgnoreCase("Normal"))
                .count();
    }
}
