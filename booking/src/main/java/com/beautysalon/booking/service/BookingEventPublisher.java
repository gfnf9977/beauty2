package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.observer.IBookingObserver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * "Суб'єкт" (Publisher) в патерні Observer.
 * Це "ручна" реалізація без використання Spring Events.
 * Він керує списком підписників (спостерігачів) і повідомляє їх.
 */
@Service
public class BookingEventPublisher {

    private final List<IBookingObserver> observers = new ArrayList<>();

    public void subscribe(IBookingObserver observer) {
        System.out.println("BookingEventPublisher: Новий підписник -> " + observer.getClass().getSimpleName());
        observers.add(observer);
    }

    public void unsubscribe(IBookingObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Booking booking) {
        System.out.println("BookingEventPublisher: Повідомляємо " + observers.size() + " спостерігачів про зміну статусу...");
        for (IBookingObserver observer : observers) {
            try {
                observer.update(booking);
            } catch (Exception e) {
                System.err.println("Помилка при повідомленні спостерігача " + 
                                   observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}