package com.healthmate.ai.config;

import com.healthmate.ai.service.ReportSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ReportSummaryService reportSummaryService;

    public GlobalExceptionHandler(ReportSummaryService reportSummaryService) {
        this.reportSummaryService = reportSummaryService;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(Model model) {
        log.warn("Upload exceeded max size");
        model.addAttribute("uploadError", "The uploaded file is too large. Please upload a PDF under 10MB.");
        model.addAttribute("reportSession", null);
        model.addAttribute("recentReports", reportSummaryService.recentReports());
        return "agent3-report-summarizer";
    }
}

