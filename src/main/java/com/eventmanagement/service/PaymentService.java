package com.eventmanagement.service;

import com.eventmanagement.dao.PaymentDAO;
import com.eventmanagement.model.Payment;
import com.eventmanagement.util.TransactionIdGenerator;
import java.util.List;

public class PaymentService {
    private final PaymentDAO paymentDAO = new PaymentDAO();

    /** Simulates a payment gateway call — succeeds unless the amount is invalid. */
    public Payment simulatePayment(int bookingId, double amount) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setTransactionId(TransactionIdGenerator.generate());
        payment.setAmount(amount);
        payment.setStatus(amount > 0 ? "SUCCESS" : "FAILED");
        paymentDAO.createPayment(payment);
        return payment;
    }

    /** Records a payment with an explicitly chosen status (used by the Simulate/Edit dialog). */
    public Payment recordPayment(int bookingId, double amount, String status) {
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setTransactionId(TransactionIdGenerator.generate());
        payment.setAmount(amount);
        payment.setStatus(status);
        paymentDAO.createPayment(payment);
        return payment;
    }

    public boolean updatePayment(int paymentId, String transactionId, double amount, String status) {
        return paymentDAO.updatePayment(paymentId, transactionId, amount, status);
    }

    public boolean deletePayment(int paymentId) {
        return paymentDAO.deletePayment(paymentId);
    }

    public List<Payment> getAllPayments() { return paymentDAO.getAllPayments(); }
}