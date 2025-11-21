package com.beautysalon.booking.validation;

public class MasterServiceCompatibilityHandler extends AbstractBookingValidationHandler {

    public MasterServiceCompatibilityHandler() {

    }

    @Override
    public void handle(BookingValidationContext context) {
        if (context.getMaster() == null || context.getService() == null) {
            context.setErrorMessage("Помилка ланцюжка: Об'єкти Майстра або Послуги відсутні.");
            return;
        }

        if (context.getService().getMaster().getMasterId().equals(context.getMaster().getMasterId())) {
            handleNext(context);
        } else {
            context.setErrorMessage(
                "Обраний майстер (" + context.getMaster().getUser().getName() + 
                ") не надає послугу '" + context.getService().getName() + "'."
            );
        }
    }
}