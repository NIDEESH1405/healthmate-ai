package com.healthmate.ai.repository;

import com.healthmate.ai.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByAppointmentDateAscAppointmentTimeAsc();
    long countByStatus(String status);
}
