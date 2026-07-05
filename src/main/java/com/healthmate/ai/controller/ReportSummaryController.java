package com.healthmate.ai.controller;

import com.healthmate.ai.model.ReportSession;
import com.healthmate.ai.service.ReportSummaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/agents/report-summarizer")
public class ReportSummaryController {

    private static final String SESSION_KEY = "reportSession";

    private final ReportSummaryService reportSummaryService;

    public ReportSummaryController(ReportSummaryService reportSummaryService) {
        this.reportSummaryService = reportSummaryService;
    }

    @GetMapping
    public String index(HttpSession session, Model model) {
        model.addAttribute("reportSession", session.getAttribute(SESSION_KEY));
        model.addAttribute("recentReports", reportSummaryService.recentReports());
        return "agent3-report-summarizer";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, HttpSession session, Model model) {
        try {
            ReportSession reportSession = reportSummaryService.analyze(file);
            session.setAttribute(SESSION_KEY, reportSession);
        } catch (Exception e) {
            model.addAttribute("uploadError", e.getMessage());
            model.addAttribute("reportSession", session.getAttribute(SESSION_KEY));
            model.addAttribute("recentReports", reportSummaryService.recentReports());
            return "agent3-report-summarizer";
        }
        return "redirect:/agents/report-summarizer";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question, HttpSession session) {
        ReportSession reportSession = (ReportSession) session.getAttribute(SESSION_KEY);
        if (reportSession != null && question != null && !question.isBlank()) {
            reportSummaryService.askFollowUp(reportSession, question);
        }
        return "redirect:/agents/report-summarizer";
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(HttpSession session) {
        ReportSession reportSession = (ReportSession) session.getAttribute(SESSION_KEY);
        if (reportSession == null) {
            return ResponseEntity.notFound().build();
        }
        StringBuilder content = new StringBuilder();
        content.append("HealthMate AI - Medical Report Summary\n");
        content.append("File: ").append(reportSession.getFileName()).append("\n");
        content.append("========================================\n\n");
        content.append("SUMMARY:\n").append(reportSession.getSummary()).append("\n\n");
        content.append("FINDINGS:\n");
        reportSession.getFindings().forEach(f ->
                content.append(String.format("- %s: %s [%s]%n", f.getLabel(), f.getValue(), f.getStatus())));
        content.append("\nThis summary is informational only and does not constitute a medical diagnosis. ")
                .append("Please consult a licensed healthcare professional to review your original report.\n");

        byte[] bytes = content.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-summary.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

    @PostMapping("/clear")
    public String clear(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        return "redirect:/agents/report-summarizer";
    }
}
