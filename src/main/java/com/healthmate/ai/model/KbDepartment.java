package com.healthmate.ai.model;

import java.util.List;

public class KbDepartment {

    private String name;
    private List<KbDoctor> doctors;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<KbDoctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<KbDoctor> doctors) {
        this.doctors = doctors;
    }
}
