package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.*;
import com.beautysalon.booking.repository.IBookingRepository;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.repository.IUserRepository;
import com.beautysalon.booking.validation.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final IBookingRepository bookingRepository;
    private final IBookingValidationHandler validationChain;

    public BookingService(
            IBookingRepository bookingRepository,
            IUserRepository userRepository,
            IServiceRepository serviceRepository,
            IMasterRepository masterRepository) {

        this.bookingRepository = bookingRepository;

        // === Ручна збірка ланцюжка валідаторів ===
        IBookingValidationHandler clientHandler = new ClientExistenceHandler(userRepository);
        IBookingValidationHandler masterHandler = new MasterExistenceHandler(masterRepository);
        IBookingValidationHandler serviceHandler = new ServiceExistenceHandler(serviceRepository);

        // TODO: Додати ScheduleValidationHandler, коли буде реалізовано
        // IBookingValidationHandler scheduleHandler = new ScheduleValidationHandler(scheduleRepository);

        // З'єднуємо ланки в ланцюжок
        clientHandler.setNext(masterHandler);
        masterHandler.setNext(serviceHandler);
        // TODO: serviceHandler.setNext(scheduleHandler);

        this.validationChain = clientHandler;
    }

    /**
     * Створення бронювання з валідацією через ланцюжок.
     */
    public Booking createBooking(UUID clientId, UUID serviceId, UUID masterId, LocalDateTime desiredDateTime) {
        BookingValidationContext context = new BookingValidationContext(
                clientId, masterId, serviceId, desiredDateTime);

        validationChain.handle(context);

        if (context.hasError()) {
            throw new RuntimeException(context.getErrorMessage());
        }

        Booking newBooking = new Booking();
        newBooking.setClient(context.getClient());
        newBooking.setMaster(context.getMaster());
        newBooking.setService(context.getService());
        newBooking.setBookingDate(context.getDateTime().toLocalDate());
        newBooking.setBookingTime(context.getDateTime().toLocalTime());
        newBooking.setTotalPrice(context.getService().getPrice());
        newBooking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(newBooking);
    }

    /**
     * Підтвердження бронювання.
     */
    public Booking confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.confirm();
        return bookingRepository.save(booking);
    }

    /**
     * Оплата бронювання.
     */
    public Booking payBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.pay();
        return bookingRepository.save(booking);
    }

    /**
     * Завершення бронювання.
     */
    public Booking completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.complete();
        return bookingRepository.save(booking);
    }

    /**
     * Скасування бронювання.
     */
    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.cancel();
        return bookingRepository.save(booking);
    }

    /**
     * Отримання бронювань клієнта.
     */
    public List<Booking> getBookingsByClient(UUID clientId) {
        return bookingRepository.findByClientUserIdOrderByBookingDateDesc(clientId);
    }
}
