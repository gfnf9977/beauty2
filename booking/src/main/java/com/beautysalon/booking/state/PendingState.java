package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;

public class PendingState implements BookingState {

    private static final PendingState INSTANCE = new PendingState();

    private PendingState() {} 

    public static PendingState getInstance() { 
        return INSTANCE;
    }

    @Override
    public void confirm(Booking booking) {
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