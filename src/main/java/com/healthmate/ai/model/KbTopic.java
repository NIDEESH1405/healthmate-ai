package com.healthmate.ai.model;

import java.util.List;

/**
 * Represents a single knowledge-base topic entry loaded from hospital_kb.json.
 * The "departments" field is only populated for the "departments" topic; it is
 * null for all other topics.
 */
public class KbTopic {

    private String id;
    private String title;
    private String icon;
    private List<String> keywords;
    private String content;
    private List<KbDepartment> departments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<KbDepartment> getDepartments() {
        return departments;
    }

    public void setDepartments(List<KbDepartment> departments) {
        this.departments = departments;
    }
}
