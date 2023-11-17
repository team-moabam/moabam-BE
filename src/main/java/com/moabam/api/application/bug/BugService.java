package com.moabam.api.application.bug;

import static com.moabam.api.domain.bug.BugActionType.*;
import static com.moabam.api.domain.bug.BugType.*;
import static com.moabam.api.domain.product.ProductType.*;
import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.payment.PaymentMapper;
import com.moabam.api.application.product.ProductMapper;
import com.moabam.api.domain.bug.BugHistory;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.bug.repository.BugHistorySearchRepository;
import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.repository.ProductRepository;
import com.moabam.api.dto.bug.BugResponse;
import com.moabam.api.dto.bug.TodayBugResponse;
import com.moabam.api.dto.product.ProductsResponse;
import com.moabam.api.dto.product.PurchaseProductRequest;
import com.moabam.api.dto.product.PurchaseProductResponse;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BugService {

	private final MemberService memberService;
	private final BugHistorySearchRepository bugHistorySearchRepository;
	private final ProductRepository productRepository;
	private final PaymentRepository paymentRepository;
	private final ClockHolder clockHolder;

	public BugResponse getBug(Long memberId) {
		Member member = memberService.getById(memberId);

		return BugMapper.toBugResponse(member.getBug());
	}

	public TodayBugResponse getTodayBug(Long memberId) {
		List<BugHistory> todayRewardBug = bugHistorySearchRepository.find(memberId, REWARD, clockHolder.times());
		int morningBug = calculateBugQuantity(todayRewardBug, MORNING);
		int nightBug = calculateBugQuantity(todayRewardBug, NIGHT);

		return BugMapper.toTodayBugResponse(morningBug, nightBug);
	}

	public ProductsResponse getBugProducts() {
		List<Product> products = productRepository.findAllByType(BUG);

		return ProductMapper.toProductsResponse(products);
	}

	public PurchaseProductResponse purchaseBugProduct(Long memberId, Long productId, PurchaseProductRequest request) {
		Product product = getById(productId);
		Payment payment = PaymentMapper.toEntity(memberId, product);

		if (!isNull(request.couponId())) {
			Coupon coupon = Coupon.builder().build(); // TODO: CouponWallet 에 존재하는 할인 쿠폰인지 확인 @홍
			payment.applyCoupon(coupon);
		}
		paymentRepository.save(payment);

		return ProductMapper.toPurchaseProductResponse(payment);
	}

	private int calculateBugQuantity(List<BugHistory> bugHistory, BugType bugType) {
		return bugHistory.stream()
			.filter(history -> bugType.equals(history.getBugType()))
			.mapToInt(BugHistory::getQuantity)
			.sum();
	}

	private Product getById(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
	}
}
