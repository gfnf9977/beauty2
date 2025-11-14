package com.beautysalon.booking.validation;

import com.beautysalon.booking.entity.Service;
import com.beautysalon.booking.repository.IServiceRepository;
import java.util.Optional;

/**
 * Конкретний обробник: перевіряє, чи існує послуга.
 */
public class ServiceExistenceHandler extends AbstractBookingValidationHandler {

    private final IServiceRepository serviceRepository;

    public ServiceExistenceHandler(IServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void handle(BookingValidationContext context) {
        // У тебе в BookingService було 'com.beautysalon.booking.entity.Service'
        // тому що ім'я конфліктувало з @Service.
        // Тут конфлікту немає, але для ясності можна
        // використовувати повне ім'я.
        Optional<com.beautysalon.booking.entity.Service> service = serviceRepository.findById(context.getServiceId());

        if (service.isPresent()) {
            context.setService(service.get()); // Збагачуємо контекст
            handleNext(context); // Передаємо далі (поки це кінець)
        } else {
            // Зупиняємо ланцюжок
            context.setErrorMessage("Послугу не знайдено.");
        }
    }
}