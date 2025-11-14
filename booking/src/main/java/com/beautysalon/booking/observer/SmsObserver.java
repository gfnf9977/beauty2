package com.beautysalon.booking.observer;

import com.beautysalon.booking.entity.Booking;
import org.springframework.stereotype.Component;

/**
 * Конкретний спостерігач, який "надсилає SMS".
 */
@Component
public class SmsObserver implements IBookingObserver {

    @Override
    public void update(Booking booking) {
        // Тут була б реальна логіка відправки SMS через SMS-шлюз
        System.out.println(
            "--- [SmsObserver] ---" +
            "\nНадсилаємо SMS на номер: " + booking.getClient().getPhone() +
            "\nТекст: Статус вашого бронювання змінено на " + booking.getStatus() +
            "\n---------------------"
        );
    }
}