package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.bug.BugService;
import com.moabam.api.dto.bug.BugResponse;
import com.moabam.api.dto.product.ProductsResponse;
import com.moabam.api.dto.product.PurchaseProductRequest;
import com.moabam.api.dto.product.PurchaseProductResponse;
import com.moabam.global.auth.annotation.CurrentMember;
import com.moabam.global.auth.model.AuthorizationMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

	private final BugService bugService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public BugResponse getBug(@CurrentMember AuthorizationMember member) {
		return bugService.getBug(member.id());
	}

	@GetMapping("/products")
	@ResponseStatus(HttpStatus.OK)
	public ProductsResponse getBugProducts() {
		return bugService.getBugProducts();
	}

	@PostMapping("/products/{productId}/purchase")
	@ResponseStatus(HttpStatus.OK)
	public PurchaseProductResponse purchaseBugProduct(@CurrentMember AuthorizationMember member,
		@PathVariable Long productId, @Valid @RequestBody PurchaseProductRequest request) {
		return bugService.purchaseBugProduct(member.id(), productId, request);
	}
}
