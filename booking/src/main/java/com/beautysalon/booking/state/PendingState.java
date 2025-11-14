package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;

/**
 * Стан "Очікування".
 * Реалізує ручний Singleton, щоб уникнути
 * створення нових об'єктів при кожному переході.
 */
public class PendingState implements BookingState {

    // 1. Ручна реалізація Singleton
    private static final PendingState INSTANCE = new PendingState();

    private PendingState() {} // 2. Приватний конструктор

    public static PendingState getInstance() { // 3. Публічний метод доступу
        return INSTANCE;
    }

    @Override
    public void confirm(Booking booking) {
        // Логіка переходу: просто змінюємо enum,
        // 'Booking' (Context) сам оновить свій 'state' об'єкт.
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Override
    public void pay(Booking booking) {
        throw new IllegalStateException("Бронювання не може бути оплачено, поки воно не підтверджено.");
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Бронювання не може бути завершено, поки воно не підтверджено та не оплачено.");
    }
}