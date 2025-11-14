package com.beautysalon.booking.config;

import com.beautysalon.booking.observer.EmailObserver;
import com.beautysalon.booking.observer.SmsObserver;
import com.beautysalon.booking.service.BookingEventPublisher;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Конфігураційний клас для "ручної" реєстрації
 * наших спостерігачів у "Суб'єкті".
 */
@Configuration
public class ObserverConfig {

    @Autowired
    private BookingEventPublisher publisher; // Наш "Суб'єкт"

    @Autowired
    private EmailObserver emailObserver; // Наш спостерігач №1

    @Autowired
    private SmsObserver smsObserver; // Наш спостерігач №2

    /**
     * Цей метод автоматично виконається Spring
     * ПІСЛЯ того, як всі біни будуть створені.
     * Ідеальне місце, щоб "підписати" спостерігачів.
     */
    @PostConstruct
    public void registerObservers() {
        System.out.println("ObserverConfig: Реєструємо спостерігачів...");
        publisher.subscribe(emailObserver);
        publisher.subscribe(smsObserver);
    }
}