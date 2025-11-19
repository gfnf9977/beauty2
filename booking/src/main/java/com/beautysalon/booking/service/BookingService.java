package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.*;
import com.beautysalon.booking.repository.IBookingRepository;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IScheduleRepository;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.repository.IUserRepository;
import com.beautysalon.booking.validation.*;
import com.beautysalon.booking.composite.BookableItem;
import com.beautysalon.booking.composite.ServicePackage;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BookingService {
    private final IBookingRepository bookingRepository;
    private final IBookingValidationHandler validationChain;
    private final BookingEventPublisher eventPublisher;
    private final PaymentFacade paymentFacade;
    private final IServiceRepository serviceRepository;
    private final IScheduleRepository scheduleRepository;

    public BookingService(
            IBookingRepository bookingRepository,
            IUserRepository userRepository,
            IServiceRepository serviceRepository,
            IMasterRepository masterRepository,
            IScheduleRepository scheduleRepository,
            BookingEventPublisher eventPublisher,
            @Lazy PaymentFacade paymentFacade) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.scheduleRepository = scheduleRepository;
        this.eventPublisher = eventPublisher;
        this.paymentFacade = paymentFacade;

        IBookingValidationHandler clientHandler = new ClientExistenceHandler(userRepository);
        IBookingValidationHandler masterHandler = new MasterExistenceHandler(masterRepository);
        IBookingValidationHandler serviceHandler = new ServiceExistenceHandler(serviceRepository);
        IBookingValidationHandler compatibilityHandler = new MasterServiceCompatibilityHandler();

        clientHandler.setNext(masterHandler);
        masterHandler.setNext(serviceHandler);
        serviceHandler.setNext(compatibilityHandler);
        this.validationChain = clientHandler;
    }

    private Set<LocalTime> getAllPossibleSlots() {
        Set<LocalTime> allSlots = new HashSet<>();
        for (int hour = 8; hour < 20; hour++) {
            allSlots.add(LocalTime.of(hour, 0));
        }
        return allSlots;
    }

    public Set<LocalTime> getOccupiedSlots(UUID masterId, LocalDate date) {
        List<Booking> bookings = bookingRepository.findByMasterMasterIdAndBookingDate(masterId, date);
        Set<LocalTime> occupiedSlots = new HashSet<>();

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            int durationMinutes = booking.getService().getDurationMinutes();
            LocalTime startTime = booking.getBookingTime().truncatedTo(ChronoUnit.HOURS);
            int numberOfSlots = durationMinutes / 60;
            for (int i = 0; i < numberOfSlots; i++) {
                occupiedSlots.add(startTime.plusHours(i));
            }
        }

        List<Schedule> masterSchedules = scheduleRepository.findByMasterMasterIdAndWorkDate(masterId, date);

        if (masterSchedules.isEmpty()) {
            return getAllPossibleSlots();
        }

        Schedule schedule = masterSchedules.get(0);
        LocalTime workStart = schedule.getStartTime();
        LocalTime workEnd = schedule.getEndTime();

        Set<LocalTime> allPossibleSlots = getAllPossibleSlots();

        for (LocalTime slot : allPossibleSlots) {
            int minServiceDurationHours = 1;
            LocalTime slotEnd = slot.plusHours(minServiceDurationHours);

            if (slot.isBefore(workStart) || slotEnd.isAfter(workEnd)) {
                occupiedSlots.add(slot);
            }
        }

        return occupiedSlots;
    }

    public List<LocalDate> getMasterWorkingDates(UUID masterId) {
        return scheduleRepository.findDistinctWorkDatesByMasterId(masterId);
    }

    public Booking createBooking(UUID clientId, UUID serviceId, UUID masterId, LocalDateTime desiredDateTime) {
        BookingValidationContext context = new BookingValidationContext(clientId, masterId, serviceId, desiredDateTime);
        validationChain.handle(context);
        if (context.hasError()) {
            throw new RuntimeException(context.getErrorMessage());
        }

        BookableItem service1 = context.getService();
        ServicePackage spaPackage = new ServicePackage("SPA-пакет 'Релакс'");
        spaPackage.addItem(service1);
        com.beautysalon.booking.entity.Service service2 =
                new com.beautysalon.booking.entity.Service("Миття голови", "", 150, 15);
        spaPackage.addItem(service2);

        Booking newBooking = new Booking();
        newBooking.setClient(context.getClient());
        newBooking.setMaster(context.getMaster());
        newBooking.setService(context.getService());
        newBooking.setBookingDate(context.getDateTime().toLocalDate());
        newBooking.setBookingTime(context.getDateTime().toLocalTime());
        newBooking.setTotalPrice(spaPackage.getPrice());
        newBooking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(newBooking);
        eventPublisher.notifyObservers(savedBooking);
        return savedBooking;
    }

    public Booking confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.confirm();
        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.notifyObservers(savedBooking);
        return savedBooking;
    }

    public Booking completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.complete();
        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.notifyObservers(savedBooking);
        return savedBooking;
    }

    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));
        booking.cancel();
        Booking savedBooking = bookingRepository.save(booking);
        eventPublisher.notifyObservers(savedBooking);
        return savedBooking;
    }

    public List<Booking> getBookingsByClient(UUID clientId) {
        return bookingRepository.findByClientUserIdOrderByBookingDateDesc(clientId);
    }

    public List<Booking> getBookingsByMaster(UUID masterId) {
        return bookingRepository.findByMasterMasterIdOrderByBookingDateDesc(masterId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "bookingDate", "bookingTime"));
    }

    public com.beautysalon.booking.entity.Service addService(com.beautysalon.booking.entity.Service service) {
        return serviceRepository.save(service);
    }

    public void deleteService(UUID serviceId) {
        serviceRepository.deleteById(serviceId);
    }

    public Optional<com.beautysalon.booking.entity.Service> findServiceById(UUID serviceId) {
        return serviceRepository.findById(serviceId);
    }

    public void notifyPaymentObservers(Booking booking) {
        eventPublisher.notifyObservers(booking);
    }

    public Optional<Booking> getBookingByMasterAndDateTime(UUID masterId, LocalDate date, LocalTime time) {
        return bookingRepository.findByMasterMasterIdAndBookingDateAndBookingTime(masterId, date, time);
    }
}
