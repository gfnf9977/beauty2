package com.beautysalon.booking.payment;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> paymentStrategies) {
        this.strategies = paymentStrategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::getId, Function.identity()));
    }

    public PaymentStrategy getStrategy(String paymentId) {
        // Використовуємо дефолтну стратегію, якщо ID не передано або не знайдено
        String key = (paymentId != null) ? paymentId.toUpperCase() : "CARD";
        
        PaymentStrategy strategy = strategies.get(key);
        
        if (strategy == null) {
             // Фолбек на CARD, якщо передали щось невідоме
             return strategies.get("CARD");
        }
        return strategy;
    }
}