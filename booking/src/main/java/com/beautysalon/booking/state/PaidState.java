package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;

public class PaidState implements BookingState {

    private static final PaidState INSTANCE = new PaidState();
    private PaidState() {}
    public static PaidState getInstance() {
        return INSTANCE;
    }

    @Override
    public void confirm(Booking booking) {
    }

    @Override
    public void pay(Booking booking) {
        throw new IllegalStateException("Бронювання вже оплачено.");
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
    }

    @Override
    public void complete(Booking booking) {
        booking.setStatus(BookingStatus.COMPLETED);
    }
}