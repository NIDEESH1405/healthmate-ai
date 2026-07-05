package com.healthmate.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthmate.ai.dto.AppointmentRequest;
import com.healthmate.ai.service.AppointmentService;
import com.healthmate.ai.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agents/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ObjectMapper objectMapper;

    public AppointmentController(AppointmentService appointmentService, KnowledgeBaseService knowledgeBaseService,
                                  ObjectMapper objectMapper) {
        this.appointmentService = appointmentService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("appointmentRequest", new AppointmentRequest());
        model.addAttribute("departments", knowledgeBaseService.getDepartments());
        model.addAttribute("appointments", appointmentService.listAll());
        model.addAttribute("departmentsJson", toJson(knowledgeBaseService.getDepartments()));
        return "agent1-appointments";
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "[]";
        }
    }

    @PostMapping("/book")
    public String book(@Valid @ModelAttribute("appointmentRequest") AppointmentRequest request,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", knowledgeBaseService.getDepartments());
            model.addAttribute("appointments", appointmentService.listAll());
            model.addAttribute("departmentsJson", toJson(knowledgeBaseService.getDepartments()));
            return "agent1-appointments";
        }

        AppointmentService.BookingResult result = appointmentService.book(request);
        redirectAttributes.addFlashAttribute("bookingResult", result);
        return "redirect:/agents/appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        appointmentService.cancel(id);
        return "redirect:/agents/appointments";
    }
}
