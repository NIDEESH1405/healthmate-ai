package com.healthmate.ai.model;

public class MedicineExplanation {

    private String medicineName;
    private String explanation;
    private boolean foundInLocalDb;
    private String localDbNote;

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isFoundInLocalDb() {
        return foundInLocalDb;
    }

    public void setFoundInLocalDb(boolean foundInLocalDb) {
        this.foundInLocalDb = foundInLocalDb;
    }

    public String getLocalDbNote() {
        return localDbNote;
    }

    public void setLocalDbNote(String localDbNote) {
        this.localDbNote = localDbNote;
    }
}
