package com.healthmate.ai.model;

import java.io.Serializable;

public class ReportFinding implements Serializable {

    private String label;
    private String value;
    private String status; // Normal | High | Low | Critical

    public ReportFinding() {
    }

    public ReportFinding(String label, String value, String status) {
        this.label = label;
        this.value = value;
        this.status = status;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
