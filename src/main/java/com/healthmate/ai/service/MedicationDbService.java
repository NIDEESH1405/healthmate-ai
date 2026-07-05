package com.healthmate.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthmate.ai.model.Medication;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Loads the static local medication reference (data/medication_db.json) at startup.
 * Used by Agent 4 (Prescription Reminder) to cross-check LLM medicine explanations
 * against a curated local source, flagging when a medicine isn't in the local DB.
 */
@Service
public class MedicationDbService {

    private static final Logger log = LoggerFactory.getLogger(MedicationDbService.class);

    private List<Medication> medications = Collections.emptyList();

    @PostConstruct
    public void loadMedicationDb() {
        try (InputStream is = new ClassPathResource("data/medication_db.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            var root = mapper.readTree(is);
            Medication[] loaded = mapper.treeToValue(root.path("medications"), Medication[].class);
            this.medications = List.of(loaded);
            log.info("Loaded {} medications into local reference DB", medications.size());
        } catch (Exception e) {
            log.error("Failed to load medication_db.json", e);
            this.medications = Collections.emptyList();
        }
    }

    public List<Medication> getAllMedications() {
        return medications;
    }

    public Optional<Medication> findByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return medications.stream()
                .filter(m -> m.getName().toLowerCase(Locale.ROOT).equals(normalized)
                        || (m.getAliases() != null && m.getAliases().stream()
                                .anyMatch(a -> a.toLowerCase(Locale.ROOT).equals(normalized))))
                .findFirst();
    }
}
