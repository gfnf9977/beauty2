package com.beautysalon.booking.validation;

/**
 * Абстрактний базовий клас для реалізації логіки ланцюжка "вручну".
 */
public abstract class AbstractBookingValidationHandler implements IBookingValidationHandler {

    protected IBookingValidationHandler nextHandler;

    @Override
    public void setNext(IBookingValidationHandler next) {
        this.nextHandler = next;
    }

    /**
     * Допоміжний метод, який передає контекст далі,
     * ЯКЩО немає помилки і є наступний обробник.
     */
    protected void handleNext(BookingValidationContext context) {
        if (!context.hasError() && this.nextHandler != null) {
            this.nextHandler.handle(context);
        }
    }
    
    // 'handle' залишається абстрактним для реалізації в 
    // конкретних класах
    @Override
    public abstract void handle(BookingValidationContext context);
}