package com.healthmate.ai.service;

import com.healthmate.ai.model.KbDepartment;
import com.healthmate.ai.model.KbDoctor;
import com.healthmate.ai.model.KbTopic;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lightweight keyword-overlap retrieval over the hospital knowledge base.
 * No embeddings/vector store is used — topics are scored purely by the count of
 * shared tokens between the query and each topic's keyword list + title + content,
 * which is sufficient for a small, well-curated KB of this size.
 *
 * Shared by:
 *   - Agent 1 (Appointment Scheduling): validating department/doctor/day-of-week availability
 *   - Agent 5 (Hospital Info): grounded Q&A retrieval, restricting the LLM to top-3 matched topics
 */
@Service
public class RetrievalService {

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "is", "are", "a", "an", "of", "to", "and", "for", "in", "on", "at", "what", "how",
            "when", "where", "does", "do", "can", "i", "my", "me", "it", "this", "that", "with", "about"
    );

    private final KnowledgeBaseService knowledgeBaseService;

    public RetrievalService(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /** Returns the top-N KB topics most relevant to the given free-text query. */
    public List<KbTopic> retrieveTopTopics(String query, int topN) {
        Set<String> queryTokens = tokenize(query);
        return knowledgeBaseService.getAllTopics().stream()
                .map(topic -> new ScoredTopic(topic, score(topic, queryTokens)))
                .sorted(Comparator.comparingInt((ScoredTopic st) -> st.score).reversed())
                .limit(topN)
                .filter(st -> st.score > 0)
                .map(st -> st.topic)
                .collect(Collectors.toList());
    }

    private int score(KbTopic topic, Set<String> queryTokens) {
        Set<String> topicTokens = tokenize(
                (topic.getTitle() == null ? "" : topic.getTitle()) + " "
                + (topic.getContent() == null ? "" : topic.getContent()) + " "
                + (topic.getKeywords() == null ? "" : String.join(" ", topic.getKeywords()))
        );
        int overlap = 0;
        for (String token : queryTokens) {
            if (topicTokens.contains(token)) {
                overlap++;
            }
        }
        // Give extra weight to direct keyword matches (curated, higher-signal terms).
        if (topic.getKeywords() != null) {
            for (String kw : topic.getKeywords()) {
                for (String qt : queryTokens) {
                    if (kw.toLowerCase(Locale.ROOT).contains(qt)) {
                        overlap += 2;
                    }
                }
            }
        }
        return overlap;
    }

    private Set<String> tokenize(String text) {
        if (text == null) return Set.of();
        var matcher = WORD_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        return matcher.results()
                .map(m -> m.group())
                .filter(w -> w.length() > 1 && !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());
    }

    /**
     * Checks whether the given doctor in the given department is available on the
     * weekday corresponding to the given date. Returns true if the doctor/department
     * combination isn't found (fail-open for data-entry flexibility) — callers should
     * primarily rely on this for user-facing warnings, not hard blocking.
     */
    public boolean isDoctorAvailable(String departmentName, String doctorName, LocalDate date) {
        List<KbDepartment> departments = knowledgeBaseService.getDepartments();
        String dayAbbrev = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        for (KbDepartment dept : departments) {
            if (dept.getName().equalsIgnoreCase(departmentName)) {
                for (KbDoctor doc : dept.getDoctors()) {
                    if (doc.getName().equalsIgnoreCase(doctorName)) {
                        return doc.getAvailability() != null && doc.getAvailability().contains(dayAbbrev);
                    }
                }
            }
        }
        return true;
    }

    private static final class ScoredTopic {
        final KbTopic topic;
        final int score;

        ScoredTopic(KbTopic topic, int score) {
            this.topic = topic;
            this.score = score;
        }
    }

    public boolean isWeekendClosed(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
