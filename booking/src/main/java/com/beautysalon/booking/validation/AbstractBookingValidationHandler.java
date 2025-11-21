package com.beautysalon.booking.validation;

public abstract class AbstractBookingValidationHandler implements IBookingValidationHandler {

    protected IBookingValidationHandler nextHandler;

    @Override
    public void setNext(IBookingValidationHandler next) {
        this.nextHandler = next;
    }

    protected void handleNext(BookingValidationContext context) {
        if (!context.hasError() && this.nextHandler != null) {
            this.nextHandler.handle(context);
        }
    }
 
    @Override
    public abstract void handle(BookingValidationContext context);
}