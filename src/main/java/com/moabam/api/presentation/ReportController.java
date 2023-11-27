package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.report.ReportService;
import com.moabam.api.dto.report.ReportRequest;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public void reports(@Auth AuthMember authMember, @Valid @RequestBody ReportRequest reportRequest) {
		reportService.report(authMember, reportRequest);
	}
}
