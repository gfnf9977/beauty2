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

    // "Ручний" список спостерігачів
    private final List<IBookingObserver> observers = new ArrayList<>();

    /**
     * Метод для додавання нового спостерігача.
     */
    public void subscribe(IBookingObserver observer) {
        System.out.println("BookingEventPublisher: Новий підписник -> " + observer.getClass().getSimpleName());
        observers.add(observer);
    }

    /**
     * Метод для видалення спостерігача.
     */
    public void unsubscribe(IBookingObserver observer) {
        observers.remove(observer);
    }

    /**
     * Головний метод. Повідомляє ВСІХ підписників про подію.
     */
    public void notifyObservers(Booking booking) {
        System.out.println("BookingEventPublisher: Повідомляємо " + observers.size() + " спостерігачів про зміну статусу...");
        for (IBookingObserver observer : observers) {
            try {
                observer.update(booking);
            } catch (Exception e) {
                // Логуємо помилку, але не зупиняємо цикл,
                // щоб інші спостерігачі отримали повідомлення
                System.err.println("Помилка при повідомленні спостерігача " + 
                                   observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}