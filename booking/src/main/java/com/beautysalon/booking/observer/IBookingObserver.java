package com.beautysalon.booking.observer;
import com.beautysalon.booking.entity.Booking;

public interface IBookingObserver {
    void update(Booking booking);
}
