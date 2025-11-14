package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;

public class CancelledState implements BookingState {

    private static final CancelledState INSTANCE = new CancelledState();
    private CancelledState() {}
    public static CancelledState getInstance() {
        return INSTANCE;
    }

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException("Бронювання скасовано і не може бути підтверджено.");
    }

    @Override
    public void pay(Booking booking) {
        throw new IllegalStateException("Бронювання скасовано і не може бути оплачено.");
    }

    @Override
    public void cancel(Booking booking) {
        // Вже скасовано, нічого не робимо
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Бронювання скасовано і не може бути завершено.");
    }
}