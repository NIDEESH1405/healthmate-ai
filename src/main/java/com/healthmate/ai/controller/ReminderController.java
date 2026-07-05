package com.healthmate.ai.controller;

import com.healthmate.ai.dto.ReminderRequest;
import com.healthmate.ai.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agents/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("reminderRequest", new ReminderRequest());
        model.addAttribute("medications", reminderService.allMedications());
        model.addAttribute("reminders", reminderService.listAll());
        return "agent4-reminders";
    }

    @PostMapping("/explain")
    public String explain(@RequestParam String medicineName, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("explanation", reminderService.explainMedicine(medicineName));
        redirectAttributes.addFlashAttribute("explainedMedicine", medicineName);
        return "redirect:/agents/reminders";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("reminderRequest") ReminderRequest request,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("medications", reminderService.allMedications());
            model.addAttribute("reminders", reminderService.listAll());
            return "agent4-reminders";
        }
        reminderService.createReminder(request);
        return "redirect:/agents/reminders";
    }

    @PostMapping("/{id}/taken")
    public String markTaken(@PathVariable Long id) {
        reminderService.markTaken(id);
        return "redirect:/agents/reminders";
    }

    @PostMapping("/{id}/reset")
    public String resetTaken(@PathVariable Long id) {
        reminderService.resetTaken(id);
        return "redirect:/agents/reminders";
    }

    @PostMapping("/{id}/remove")
    public String remove(@PathVariable Long id) {
        reminderService.remove(id);
        return "redirect:/agents/reminders";
    }
}
