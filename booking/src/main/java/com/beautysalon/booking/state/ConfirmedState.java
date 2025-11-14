package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;

public class ConfirmedState implements BookingState {

    private static final ConfirmedState INSTANCE = new ConfirmedState();
    private ConfirmedState() {}
    public static ConfirmedState getInstance() {
        return INSTANCE;
    }

    @Override
    public void confirm(Booking booking) {
        // Вже підтверджено, нічого не робимо
    }

    @Override
    public void pay(Booking booking) {
        // Це дозволений перехід
        booking.setStatus(BookingStatus.PAID);
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
    }

    @Override
    public void complete(Booking booking) {
        throw new IllegalStateException("Бронювання має бути оплачено перед завершенням.");
    }
}