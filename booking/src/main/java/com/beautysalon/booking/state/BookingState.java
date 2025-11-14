package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;

/**
 * Інтерфейс State (Стан).
 * Визначає контракт для всіх конкретних станів бронювання.
 * Кожен метод приймає 'Context' (Booking) для зміни його стану.
 */
public interface BookingState {

    void confirm(Booking booking);
    
    void pay(Booking booking);
    
    void cancel(Booking booking);
    
    void complete(Booking booking);
}