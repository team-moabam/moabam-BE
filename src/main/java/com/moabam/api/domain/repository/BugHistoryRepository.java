package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.BugHistory;

public interface BugHistoryRepository extends JpaRepository<BugHistory, Long> {

}
