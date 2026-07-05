package com.healthmate.ai.controller;

import com.healthmate.ai.service.HospitalInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agents/hospital-info")
public class HospitalInfoController {

    private final HospitalInfoService hospitalInfoService;

    public HospitalInfoController(HospitalInfoService hospitalInfoService) {
        this.hospitalInfoService = hospitalInfoService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("topics", hospitalInfoService.allTopics());
        model.addAttribute("hospitalName", hospitalInfoService.hospitalName());
        return "agent5-hospital-info";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("qaResult", hospitalInfoService.ask(question));
        redirectAttributes.addFlashAttribute("askedQuestion", question);
        return "redirect:/agents/hospital-info";
    }
}
