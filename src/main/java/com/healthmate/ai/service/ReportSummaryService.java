package com.healthmate.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.healthmate.ai.entity.ReportHistory;
import com.healthmate.ai.model.ChatMessage;
import com.healthmate.ai.model.ReportFinding;
import com.healthmate.ai.model.ReportSession;
import com.healthmate.ai.repository.ReportHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 3: Medical Report Summarizer.
 * Extracts text from an uploaded PDF, asks Groq (JSON mode) for a plain-language summary
 * plus a labeled list of values (Normal/High/Low/Critical), and persists ONLY the summary
 * metadata to the database — never the raw extracted report text.
 */
@Service
public class ReportSummaryService {

    private final PdfExtractService pdfExtractService;
    private final GroqService groqService;
    private final ReportHistoryRepository reportHistoryRepository;

    public ReportSummaryService(PdfExtractService pdfExtractService,
                                 GroqService groqService,
                                 ReportHistoryRepository reportHistoryRepository) {
        this.pdfExtractService = pdfExtractService;
        this.groqService = groqService;
        this.reportHistoryRepository = reportHistoryRepository;
    }

    public ReportSession analyze(MultipartFile file) throws IOException {
        String text = pdfExtractService.extractText(file);

        ReportSession session = new ReportSession();
        session.setFileName(file.getOriginalFilename());
        session.setExtractedText(text);

        String prompt = "Here is the extracted text of a patient's medical report:\n\n" + text
                + "\n\nProduce a JSON object with exactly these fields: "
                + "\"summary\" (a clear, plain-language 3-5 sentence summary of what the report shows, "
                + "written for a non-medical reader, explicitly noting this is informational only and not a diagnosis), "
                + "\"findings\" (an array of objects, each with \"label\" (the test/parameter name), "
                + "\"value\" (the reported value including units if present), and \"status\" (must be exactly one of "
                + "'Normal', 'High', 'Low', 'Critical') for every measurable value found in the report).";

        JsonNode result = groqService.jsonPrompt(
                "You are a careful medical report explainer. You summarize lab/diagnostic reports in plain language "
                        + "and flag abnormal values, but you never provide a diagnosis or treatment recommendation. "
                        + "Always recommend the patient discuss results with their doctor.",
                prompt
        );

        session.setSummary(result.path("summary").asText(
                "We were unable to fully process this report. Please consult your doctor to review the original document."));

        List<ReportFinding> findings = new ArrayList<>();
        JsonNode findingsNode = result.path("findings");
        if (findingsNode.isArray()) {
            for (JsonNode f : findingsNode) {
                findings.add(new ReportFinding(
                        f.path("label").asText("Unlabeled value"),
                        f.path("value").asText(""),
                        normalizeStatus(f.path("status").asText("Normal"))
                ));
            }
        }
        session.setFindings(findings);

        ReportHistory history = new ReportHistory();
        history.setFileName(session.getFileName());
        history.setSummary(session.getSummary());
        history.setFlaggedCount((int) session.getFlaggedCount());
        reportHistoryRepository.save(history);

        return session;
    }

    public String askFollowUp(ReportSession session, String question) {
        session.getFollowUpConversation().add(ChatMessage.user(question));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(
                "You are answering follow-up questions about a specific medical report on behalf of the patient. "
                        + "Base your answers ONLY on the report text and summary provided below. Never diagnose or "
                        + "prescribe treatment; if asked to diagnose, gently redirect the patient to their doctor.\n\n"
                        + "REPORT TEXT:\n" + session.getExtractedText() + "\n\nSUMMARY:\n" + session.getSummary()));
        messages.addAll(session.getFollowUpConversation());

        String reply = groqService.chat(messages);
        session.getFollowUpConversation().add(ChatMessage.assistant(reply));
        return reply;
    }

    public List<ReportHistory> recentReports() {
        return reportHistoryRepository.findTop10ByOrderByCreatedAtDesc();
    }

    private String normalizeStatus(String raw) {
        if (raw == null) return "Normal";
        String lower = raw.trim().toLowerCase();
        if (lower.contains("critical")) return "Critical";
        if (lower.contains("high")) return "High";
        if (lower.contains("low")) return "Low";
        return "Normal";
    }
}
