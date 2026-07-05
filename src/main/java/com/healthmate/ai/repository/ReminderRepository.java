package com.healthmate.ai.repository;

import com.healthmate.ai.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findAllByOrderByReminderTimeAsc();
    long countByTakenTodayFalse();
}
