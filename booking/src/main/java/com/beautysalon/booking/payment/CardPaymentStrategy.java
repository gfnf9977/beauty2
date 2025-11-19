package com.beautysalon.booking.payment;

import org.springframework.stereotype.Service;

@Service
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public String getId() {
        return "CARD";
    }

    @Override
    public boolean processPayment(double amount, String cardNumber) {
        if (amount > 0) {
            // Беремо останні 4 цифри для логу, перевіряючи довжину
            String maskedCard = cardNumber.length() >= 4 
                ? cardNumber.substring(cardNumber.length() - 4) 
                : cardNumber;
                
            System.out.println("💳 [Strategy: CARD] Успішна оплата " + amount + " грн. Картка **** " + maskedCard);
            return true;
        }
        return false;
    }

    @Override
    public boolean processRefund(double amount) {
        if (amount > 0) {
            System.out.println("💳 [Strategy: CARD] Успішне повернення " + amount + " грн. на рахунок клієнта.");
            return true;
        }
        return false;
    }
}