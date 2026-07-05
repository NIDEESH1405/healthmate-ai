package com.healthmate.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthmate.ai.model.ChatMessage;
import com.healthmate.ai.model.SymptomSession;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent 2: Symptom Checker.
 * Purely informational triage flow: collects body areas + severity + free-text description,
 * lets the LLM ask up to 3 clarifying questions in a session-based chat, then produces a
 * final structured assessment (specialist type + urgency banding) via a JSON-mode call.
 * Never produces a diagnosis — only a suggested specialist type and general urgency guidance.
 */
@Service
public class SymptomCheckerService {

    private static final int MAX_CLARIFYING_QUESTIONS = 3;

    private static final String SYSTEM_PROMPT =
            "You are HealthMate AI's informational symptom triage assistant. You are NOT a doctor and must "
            + "never provide a diagnosis or specific treatment. Your job is to ask clarifying questions "
            + "(no more than 3 total across the conversation) to better understand the patient's symptoms, "
            + "then hand off to a structured assessment. Keep each question short, empathetic, and focused on "
            + "one thing at a time (e.g. duration, triggers, associated symptoms, severity changes). "
            + "If the user describes signs of a life-threatening emergency (e.g. chest pain with breathlessness, "
            + "severe bleeding, stroke symptoms), immediately say so plainly and advise seeking emergency care right away.";

    private final GroqService groqService;

    public SymptomCheckerService(GroqService groqService) {
        this.groqService = groqService;
    }

    public SymptomSession startNewSession(List<String> bodyAreas, int severity, String description) {
        SymptomSession session = new SymptomSession();
        session.setBodyAreas(bodyAreas);
        session.setSeverity(severity);
        session.setInitialDescription(description);

        String intro = String.format(
                "Body area(s) affected: %s. Self-reported severity: %d/10. Description: %s",
                String.join(", ", bodyAreas), severity, description
        );
        session.getConversation().add(ChatMessage.user(intro));

        askNextQuestionOrFinish(session);
        return session;
    }

    public void submitAnswer(SymptomSession session, String answer) {
        if (session.isAssessmentComplete()) {
            return;
        }
        session.getConversation().add(ChatMessage.user(answer));
        askNextQuestionOrFinish(session);
    }

    private void askNextQuestionOrFinish(SymptomSession session) {
        if (session.getClarifyingQuestionsAsked() >= MAX_CLARIFYING_QUESTIONS) {
            finalizeAssessment(session);
            return;
        }

        List<ChatMessage> messages = buildMessageList(session);
        String reply = groqService.chat(messages);
        session.getConversation().add(ChatMessage.assistant(reply));
        session.incrementClarifyingQuestions();

        // If the model signals it has enough information already, finalize early.
        if (reply.toLowerCase().contains("[assessment_ready]")) {
            finalizeAssessment(session);
        }
    }

    private List<ChatMessage> buildMessageList(SymptomSession session) {
        List<ChatMessage> messages = new java.util.ArrayList<>();
        messages.add(ChatMessage.system(SYSTEM_PROMPT
                + " Ask exactly one clarifying question in your reply, nothing else. "
                + "If after the patient's latest message you already have enough information, "
                + "reply with the token [ASSESSMENT_READY] instead of a question."));
        messages.addAll(session.getConversation());
        return messages;
    }

    private void finalizeAssessment(SymptomSession session) {
        StringBuilder transcript = new StringBuilder();
        for (ChatMessage m : session.getConversation()) {
            transcript.append(m.getRole()).append(": ").append(m.getContent()).append("\n");
        }

        String userPrompt = "Conversation transcript so far:\n" + transcript
                + "\n\nBased on this, provide a structured informational assessment as JSON with exactly these fields: "
                + "\"specialistType\" (a short specialist name, e.g. 'General Physician', 'Cardiologist', 'Dermatologist', "
                + "'ENT Specialist', 'Orthopedic Specialist', 'Neurologist', 'Gastroenterologist'), "
                + "\"urgencyLevel\" (must be exactly one of: 'Routine', 'See a doctor soon', 'Seek immediate care'), "
                + "\"summary\" (2-3 sentence plain-language summary of the likely area of concern, with an explicit "
                + "reminder that this is not a diagnosis).";

        JsonNode result = groqService.jsonPrompt(
                "You are a careful, conservative medical triage classifier. You never diagnose specific conditions; "
                        + "you only suggest which type of specialist to see and how urgently, erring toward caution.",
                userPrompt
        );

        session.setSpecialistType(result.path("specialistType").asText("General Physician"));
        session.setUrgencyLevel(normalizeUrgency(result.path("urgencyLevel").asText("See a doctor soon")));
        session.setAssessmentSummary(result.path("summary").asText(
                "Based on what you've shared, we recommend consulting a healthcare professional for a proper evaluation. "
                        + "This is not a diagnosis."));
        session.setAssessmentComplete(true);
    }

    private String normalizeUrgency(String raw) {
        if (raw == null) return "See a doctor soon";
        String lower = raw.toLowerCase();
        if (lower.contains("immediate") || lower.contains("emergency")) return "Seek immediate care";
        if (lower.contains("routine")) return "Routine";
        return "See a doctor soon";
    }
}
