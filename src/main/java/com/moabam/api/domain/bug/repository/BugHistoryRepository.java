package com.moabam.api.domain.bug.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.bug.BugHistory;

public interface BugHistoryRepository extends JpaRepository<BugHistory, Long> {

}
