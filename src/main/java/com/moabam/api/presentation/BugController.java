package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.BugService;
import com.moabam.api.dto.BugResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

	private final BugService bugService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public BugResponse getBug() {
		return bugService.getBug(1L);
	}
}
