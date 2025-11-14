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
    private final BookingEventPublisher eventPublisher; // Ін'єкція "Суб'єкта"

    public BookingService(
            IBookingRepository bookingRepository,
            IUserRepository userRepository,
            IServiceRepository serviceRepository,
            IMasterRepository masterRepository,
            BookingEventPublisher eventPublisher) {

        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;

        // === Ланцюжок валідаторів (Chain of Responsibility) ===
        IBookingValidationHandler clientHandler = new ClientExistenceHandler(userRepository);
        IBookingValidationHandler masterHandler = new MasterExistenceHandler(masterRepository);
        IBookingValidationHandler serviceHandler = new ServiceExistenceHandler(serviceRepository);

        clientHandler.setNext(masterHandler);
        masterHandler.setNext(serviceHandler);
        this.validationChain = clientHandler;
    }

    /**
     * Створення бронювання з валідацією та повідомленням спостерігачів.
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

        Booking savedBooking = bookingRepository.save(newBooking);
        eventPublisher.notifyObservers(savedBooking); // Повідомляємо спостерігачів про нове бронювання
        return savedBooking;
    }

    /**
     * Підтвердження бронювання з повідомленням спостерігачів.
     */
    public Booking confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.confirm();
        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.notifyObservers(savedBooking); // Повідомляємо про зміну статусу
        return savedBooking;
    }

    /**
     * Оплата бронювання з повідомленням спостерігачів.
     */
    public Booking payBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.pay();
        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.notifyObservers(savedBooking); // Повідомляємо про зміну статусу
        return savedBooking;
    }

    /**
     * Завершення бронювання з повідомленням спостерігачів.
     */
    public Booking completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.complete();
        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.notifyObservers(savedBooking); // Повідомляємо про зміну статусу
        return savedBooking;
    }

    /**
     * Скасування бронювання з повідомленням спостерігачів.
     */
    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.cancel();
        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.notifyObservers(savedBooking); // Повідомляємо про зміну статусу
        return savedBooking;
    }

    /**
     * Отримання бронювань клієнта.
     */
    public List<Booking> getBookingsByClient(UUID clientId) {
        return bookingRepository.findByClientUserIdOrderByBookingDateDesc(clientId);
    }
}
