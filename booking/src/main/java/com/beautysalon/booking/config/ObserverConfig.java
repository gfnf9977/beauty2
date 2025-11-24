package com.beautysalon.booking.config;

import com.beautysalon.booking.observer.EmailObserver;
import com.beautysalon.booking.service.BookingEventPublisher;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObserverConfig {

    @Autowired
    private BookingEventPublisher publisher;

    @Autowired
    private EmailObserver emailObserver;



    @PostConstruct
    public void registerObservers() {
        System.out.println("ObserverConfig: Реєструємо спостерігачів...");
        publisher.subscribe(emailObserver);
        // publisher.subscribe(smsObserver); // <-- ВИДАЛЕНО
    }
}