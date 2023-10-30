package com.moabam.api.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.BugService;
import com.moabam.api.dto.bug.BugResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

	private final BugService bugService;

	@GetMapping
	public ResponseEntity<BugResponse> getBug() {
		return ResponseEntity.ok(bugService.getBug(1L));
	}
}
