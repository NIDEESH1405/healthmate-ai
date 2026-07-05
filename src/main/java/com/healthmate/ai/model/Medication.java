package com.healthmate.ai.model;

import java.util.List;

public class Medication {

    private String name;
    private List<String> aliases;
    private String category;
    private String commonUse;
    private String typicalFrequency;
    private String notes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCommonUse() {
        return commonUse;
    }

    public void setCommonUse(String commonUse) {
        this.commonUse = commonUse;
    }

    public String getTypicalFrequency() {
        return typicalFrequency;
    }

    public void setTypicalFrequency(String typicalFrequency) {
        this.typicalFrequency = typicalFrequency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
