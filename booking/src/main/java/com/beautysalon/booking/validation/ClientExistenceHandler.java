package com.beautysalon.booking.validation;

import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.repository.IUserRepository;
import java.util.Optional;

/**
 * Конкретний обробник: перевіряє, чи існує клієнт.
 */
public class ClientExistenceHandler extends AbstractBookingValidationHandler {

    private final IUserRepository userRepository;

    public ClientExistenceHandler(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(BookingValidationContext context) {
        Optional<User> client = userRepository.findById(context.getClientId());

        if (client.isPresent()) {
            context.setClient(client.get()); // Збагачуємо контекст
            handleNext(context); // Передаємо далі
        } else {
            // Зупиняємо ланцюжок
            context.setErrorMessage("Клієнт не знайдений.");
        }
    }
}