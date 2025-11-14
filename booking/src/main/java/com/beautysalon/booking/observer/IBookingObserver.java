package com.beautysalon.booking.observer;

import com.beautysalon.booking.entity.Booking;

/**
 * Інтерфейс Observer (Спостерігач).
 * Визначає єдиний метод 'update', який буде викликаний
 * "Суб'єктом", коли відбудеться подія.
 */
public interface IBookingObserver {
    
    /**
     * @param booking Об'єкт бронювання, в якому відбулися зміни.
     */
    void update(Booking booking);
}