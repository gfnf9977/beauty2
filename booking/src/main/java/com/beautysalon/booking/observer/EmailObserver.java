package com.beautysalon.booking.observer;

import com.beautysalon.booking.entity.Booking;
import org.springframework.stereotype.Component;

/**
 * Конкретний спостерігач, який "надсилає email".
 * Анотація @Component робить його Spring-біном,
 * щоб ми могли його легко знайти і підписати.
 */
@Component
public class EmailObserver implements IBookingObserver {

    @Override
    public void update(Booking booking) {
        // Тут була б реальна логіка відправки email
        System.out.println(
            "--- [EmailObserver] ---" +
            "\nНадсилаємо email клієнту: " + booking.getClient().getEmail() +
            "\nТема: Статус вашого бронювання змінено" +
            "\nНовий статус: " + booking.getStatus() +
            "\n-----------------------"
        );
    }
}