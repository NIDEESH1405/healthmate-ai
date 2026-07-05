package com.healthmate.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public class ReminderRequest {

    @NotBlank(message = "Medicine name is required")
    private String medicineName;

    @NotNull(message = "Please choose a reminder time")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime reminderTime;

    @NotBlank(message = "Please choose a frequency")
    private String frequency;

    private String dosageNote;

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDosageNote() {
        return dosageNote;
    }

    public void setDosageNote(String dosageNote) {
        this.dosageNote = dosageNote;
    }
}
