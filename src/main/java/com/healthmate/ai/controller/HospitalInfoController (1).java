package com.healthmate.ai.controller;

import com.healthmate.ai.model.ChatMessage;
import com.healthmate.ai.service.HospitalInfoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/agents/hospital-info")
public class HospitalInfoController {

    private static final String HISTORY_SESSION_KEY = "hospitalInfoHistory";

    private final HospitalInfoService hospitalInfoService;

    public HospitalInfoController(HospitalInfoService hospitalInfoService) {
        this.hospitalInfoService = hospitalInfoService;
    }

    @GetMapping
    public String index(HttpSession session, Model model) {
        model.addAttribute("topics", hospitalInfoService.allTopics());
        model.addAttribute("hospitalName", hospitalInfoService.hospitalName());
        model.addAttribute("conversation", getHistory(session));
        return "agent5-hospital-info";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question, HttpSession session, RedirectAttributes redirectAttributes) {
        List<ChatMessage> history = getHistory(session);
        HospitalInfoService.QaResult result = hospitalInfoService.ask(question, history);
        session.setAttribute(HISTORY_SESSION_KEY, history);

        redirectAttributes.addFlashAttribute("qaResult", result);
        redirectAttributes.addFlashAttribute("askedQuestion", question);
        return "redirect:/agents/hospital-info";
    }

    @PostMapping("/reset")
    public String reset(HttpSession session) {
        session.removeAttribute(HISTORY_SESSION_KEY);
        return "redirect:/agents/hospital-info";
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessage> getHistory(HttpSession session) {
        List<ChatMessage> history = (List<ChatMessage>) session.getAttribute(HISTORY_SESSION_KEY);
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute(HISTORY_SESSION_KEY, history);
        }
        return history;
    }
}
