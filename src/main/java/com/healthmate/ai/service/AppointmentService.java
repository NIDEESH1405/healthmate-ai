package com.healthmate.ai.service;

import com.healthmate.ai.dto.AppointmentRequest;
import com.healthmate.ai.entity.Appointment;
import com.healthmate.ai.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Agent 1: Appointment Scheduling.
 * Validates a booking request against the hospital knowledge base availability data,
 * asks Groq to generate a friendly confirmation message, and persists the appointment.
 */
@Service
public class AppointmentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private final AppointmentRepository appointmentRepository;
    private final RetrievalService retrievalService;
    private final GroqService groqService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               RetrievalService retrievalService,
                               GroqService groqService) {
        this.appointmentRepository = appointmentRepository;
        this.retrievalService = retrievalService;
        this.groqService = groqService;
    }

    public static class BookingResult {
        public boolean success;
        public String warning;
        public String confirmationMessage;
        public Appointment appointment;
    }

    public BookingResult book(AppointmentRequest request) {
        BookingResult result = new BookingResult();

        StringBuilder warning = new StringBuilder();
        if (retrievalService.isWeekendClosed(request.getAppointmentDate())) {
            warning.append("Note: Sundays are closed for routine OPD; only emergency services operate. ");
        }
        if (!retrievalService.isDoctorAvailable(request.getDepartment(), request.getDoctorName(), request.getAppointmentDate())) {
            warning.append("Note: ").append(request.getDoctorName())
                    .append(" is not typically scheduled in ").append(request.getDepartment())
                    .append(" on that day of the week according to our availability records; the front desk will confirm.");
        }
        result.warning = warning.length() > 0 ? warning.toString().trim() : null;

        Appointment appointment = new Appointment();
        appointment.setDepartment(request.getDepartment());
        appointment.setDoctorName(request.getDoctorName());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setPatientName(request.getPatientName());
        appointment.setContactNumber(request.getContactNumber());
        appointment.setStatus("CONFIRMED");

        Appointment saved = appointmentRepository.save(appointment);
        result.appointment = saved;
        result.success = true;

        String prompt = String.format(
                "Write a short, warm, professional appointment confirmation message (3-4 sentences max) for a patient "
                        + "named %s. Department: %s. Doctor: %s. Date: %s. Time: %s. "
                        + "Remind them to arrive 15 minutes early and bring any previous medical records. "
                        + "Do not give any medical advice or diagnosis — this message is purely administrative confirmation.",
                request.getPatientName(),
                request.getDepartment(),
                request.getDoctorName(),
                saved.getAppointmentDate().format(DATE_FMT),
                saved.getAppointmentTime().format(TIME_FMT)
        );
        result.confirmationMessage = groqService.singlePrompt(
                "You are a helpful hospital front-desk assistant. You only confirm appointment logistics "
                        + "and never provide medical advice, diagnosis, or treatment suggestions.",
                prompt
        );

        return result;
    }

    public List<Appointment> listAll() {
        return appointmentRepository.findAllByOrderByAppointmentDateAscAppointmentTimeAsc();
    }

    public void cancel(Long id) {
        appointmentRepository.findById(id).ifPresent(a -> {
            a.setStatus("CANCELLED");
            appointmentRepository.save(a);
        });
    }

    public long countActive() {
        return appointmentRepository.countByStatus("CONFIRMED");
    }

    public long countAll() {
        return appointmentRepository.count();
    }
}
