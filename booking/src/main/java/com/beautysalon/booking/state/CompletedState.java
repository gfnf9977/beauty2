package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;

public class CompletedState implements BookingState {

    private static final CompletedState INSTANCE = new CompletedState();
    private CompletedState() {}
    public static CompletedState getInstance() {
        return INSTANCE;
    }

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException("Бронювання вже завершено.");
    }

    @Override
    public void pay(Booking booking) {
        throw new IllegalStateException("Бронювання вже завершено.");
    }

    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException("Бронювання не може бути скасовано, оскільки воно вже завершено.");
    }

    @Override
    public void complete(Booking booking) {
        // Вже завершено, нічого не робимо
    }
}