package itsm.itsm_backend.ticket.jpa;

import itsm.itsm_backend.reportWithOllama.MonthlyTrendReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyTrendReportRepository extends JpaRepository<MonthlyTrendReport, Long> {
    Optional<MonthlyTrendReport> findByPeriodLabel(String periodLabel);
}

