package com.healthmate.ai.controller;

import com.healthmate.ai.dto.SymptomCheckRequest;
import com.healthmate.ai.model.SymptomSession;
import com.healthmate.ai.service.SymptomCheckerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/agents/symptom-checker")
public class SymptomCheckerController {

    private static final String SESSION_KEY = "symptomSession";

    private final SymptomCheckerService symptomCheckerService;

    public SymptomCheckerController(SymptomCheckerService symptomCheckerService) {
        this.symptomCheckerService = symptomCheckerService;
    }

    @GetMapping
    public String index(HttpSession session, Model model) {
        SymptomSession symptomSession = (SymptomSession) session.getAttribute(SESSION_KEY);
        model.addAttribute("symptomSession", symptomSession);
        return "agent2-symptom-checker";
    }

    @PostMapping("/start")
    public String start(@ModelAttribute SymptomCheckRequest request, HttpSession session) {
        SymptomSession symptomSession = symptomCheckerService.startNewSession(
                request.getBodyAreas() == null ? java.util.List.of() : request.getBodyAreas(),
                request.getSeverity(),
                request.getDescription() == null || request.getDescription().isBlank()
                        ? "No additional description provided." : request.getDescription()
        );
        session.setAttribute(SESSION_KEY, symptomSession);
        return "redirect:/agents/symptom-checker";
    }

    @PostMapping("/answer")
    public String answer(@RequestParam String answer, HttpSession session) {
        SymptomSession symptomSession = (SymptomSession) session.getAttribute(SESSION_KEY);
        if (symptomSession != null) {
            symptomCheckerService.submitAnswer(symptomSession, answer);
            session.setAttribute(SESSION_KEY, symptomSession);
        }
        return "redirect:/agents/symptom-checker";
    }

    @PostMapping("/reset")
    public String reset(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        return "redirect:/agents/symptom-checker";
    }
}
