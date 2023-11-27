package com.moabam.api.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.report.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

}
