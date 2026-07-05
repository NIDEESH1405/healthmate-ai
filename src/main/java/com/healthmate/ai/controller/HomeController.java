package com.healthmate.ai.controller;

import com.healthmate.ai.service.AppointmentService;
import com.healthmate.ai.service.ReminderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AppointmentService appointmentService;
    private final ReminderService reminderService;

    public HomeController(AppointmentService appointmentService, ReminderService reminderService) {
        this.appointmentService = appointmentService;
        this.reminderService = reminderService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appointmentsBooked", appointmentService.countAll());
        model.addAttribute("activeReminders", reminderService.countActive());
        return "index";
    }
}
