package com.healthmate.ai.repository;

import com.healthmate.ai.entity.ReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {
    List<ReportHistory> findTop10ByOrderByCreatedAtDesc();
}
