package com.moabam.api.application.bug;

import static com.moabam.api.domain.product.ProductType.*;
import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.payment.PaymentMapper;
import com.moabam.api.application.product.ProductMapper;
import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.bug.repository.BugHistoryRepository;
import com.moabam.api.domain.bug.repository.BugHistorySearchRepository;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.api.domain.coupon.repository.CouponWalletSearchRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.repository.ProductRepository;
import com.moabam.api.dto.bug.BugHistoryResponse;
import com.moabam.api.dto.bug.BugHistoryWithPayment;
import com.moabam.api.dto.bug.BugResponse;
import com.moabam.api.dto.product.ProductsResponse;
import com.moabam.api.dto.product.PurchaseProductRequest;
import com.moabam.api.dto.product.PurchaseProductResponse;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BugService {

	private final MemberService memberService;
	private final BugHistoryRepository bugHistoryRepository;
	private final BugHistorySearchRepository bugHistorySearchRepository;
	private final ProductRepository productRepository;
	private final PaymentRepository paymentRepository;
	private final CouponWalletSearchRepository couponWalletSearchRepository;

	public BugResponse getBug(Long memberId) {
		Bug bug = getByMemberId(memberId);

		return BugMapper.toBugResponse(bug);
	}

	public BugHistoryResponse getBugHistory(Long memberId) {
		List<BugHistoryWithPayment> history = bugHistorySearchRepository.findByMemberIdWithPayment(memberId);

		return BugMapper.toBugHistoryResponse(history);
	}

	public ProductsResponse getBugProducts() {
		List<Product> products = productRepository.findAllByType(BUG);

		return ProductMapper.toProductsResponse(products);
	}

	@Transactional
	public PurchaseProductResponse purchaseBugProduct(Long memberId, Long productId, PurchaseProductRequest request) {
		Product product = getProductById(productId);
		Payment payment = PaymentMapper.toPayment(memberId, product);

		if (!isNull(request.couponWalletId())) {
			CouponWallet couponWallet = getCouponWallet(request.couponWalletId(), memberId);
			payment.applyCoupon(couponWallet);
		}
		paymentRepository.save(payment);

		return ProductMapper.toPurchaseProductResponse(payment);
	}

	@Transactional
	public void use(Member member, BugType bugType, int count) {
		Bug bug = member.getBug();

		bug.use(bugType, count);
		bugHistoryRepository.save(BugMapper.toUseBugHistory(member.getId(), bugType, count));
	}

	@Transactional
	public void reward(Member member, BugType bugType, int count) {
		Bug bug = member.getBug();

		bug.increase(bugType, count);
		bugHistoryRepository.save(BugMapper.toRewardBugHistory(member.getId(), bugType, count));
	}

	@Transactional
	public void charge(Long memberId, Product bugProduct) {
		Bug bug = getByMemberId(memberId);

		bug.charge(bugProduct.getQuantity());
		bugHistoryRepository.save(BugMapper.toChargeBugHistory(memberId, bugProduct.getQuantity()));
	}

	@Transactional
	public void applyCoupon(Long memberId, BugType bugType, int count) {
		Bug bug = getByMemberId(memberId);

		bug.increase(bugType, count);
		bugHistoryRepository.save(BugMapper.toCouponBugHistory(memberId, bugType, count));
	}

	private Bug getByMemberId(Long memberId) {
		return memberService.findMember(memberId)
			.getBug();
	}

	private Product getProductById(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new NotFoundException(PRODUCT_NOT_FOUND));
	}

	private CouponWallet getCouponWallet(Long couponWalletId, Long memberId) {
		return couponWalletSearchRepository.findByIdAndMemberId(couponWalletId, memberId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON_WALLET));
	}
}
