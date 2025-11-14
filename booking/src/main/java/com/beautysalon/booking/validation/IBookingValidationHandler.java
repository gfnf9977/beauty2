package com.beautysalon.booking.validation;

/**
 * Інтерфейс Handler (Обробник) для патерну Chain of Responsibility.
 */
public interface IBookingValidationHandler {
    
    /**
     * Встановлює наступний обробник у ланцюжку.
     */
    void setNext(IBookingValidationHandler next);

    /**
     * Обробляє запит (контекст).
     * Або передає його далі, або зупиняє ланцюжок.
     */
    void handle(BookingValidationContext context);
}