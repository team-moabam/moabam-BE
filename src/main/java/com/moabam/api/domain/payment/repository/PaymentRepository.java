package com.moabam.api.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.payment.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
