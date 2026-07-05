package com.healthmate.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthmate.ai.model.KbDepartment;
import com.healthmate.ai.model.KbTopic;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Loads the static hospital knowledge base (data/hospital_kb.json) once at startup
 * and exposes it in-memory. Used directly by Agent 5 (Hospital Info) for KB browsing,
 * and indirectly by Agent 1 (Appointment Scheduling) for department/doctor/availability
 * validation via RetrievalService.
 */
@Service
public class KnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private List<KbTopic> topics = Collections.emptyList();
    private String hospitalName = "HealthMate General Hospital";

    @PostConstruct
    public void loadKnowledgeBase() {
        try (InputStream is = new ClassPathResource("data/hospital_kb.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            var root = mapper.readTree(is);
            this.hospitalName = root.path("hospitalName").asText(hospitalName);
            KbTopic[] loaded = mapper.treeToValue(root.path("topics"), KbTopic[].class);
            this.topics = List.of(loaded);
            log.info("Loaded {} hospital knowledge base topics", topics.size());
        } catch (Exception e) {
            log.error("Failed to load hospital_kb.json", e);
            this.topics = Collections.emptyList();
        }
    }

    public List<KbTopic> getAllTopics() {
        return topics;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public List<KbDepartment> getDepartments() {
        return topics.stream()
                .filter(t -> "departments".equals(t.getId()))
                .findFirst()
                .map(KbTopic::getDepartments)
                .orElse(Collections.emptyList());
    }
}
