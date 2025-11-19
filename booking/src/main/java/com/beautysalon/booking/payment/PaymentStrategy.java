package com.beautysalon.booking.payment;

public interface PaymentStrategy {
    String getId();
    boolean processPayment(double amount, String cardNumber);
    boolean processRefund(double amount);
}