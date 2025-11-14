package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;
import com.beautysalon.booking.entity.Payment;
import com.beautysalon.booking.repository.IBookingRepository;
import com.beautysalon.booking.repository.IPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * РЕАЛІЗАЦІЯ ПАТЕРНУ "ФАСАД" (ЛР7)
 *
 * Цей клас надає єдиний, спрощений інтерфейс (метод payForBooking)
 * до складної підсистеми оплати.
 *
 * Він приховує (інкапсулює) логіку:
 * 1. Пошуку бронювання (IBookingRepository)
 * 2. Виклику патерну State (booking.pay())
 * 3. Виклику зовнішнього API (поки що симуляція)
 * 4. Створення запису в БД (IPaymentRepository)
 * 5. Повідомлення спостерігачів (через BookingService/EventPublisher)
 */
@Service
public class PaymentFacade {

    private final IBookingRepository bookingRepository;
    private final IPaymentRepository paymentRepository;
    private final BookingService bookingService; // Потрібен для виклику Observer

    public PaymentFacade(
            IBookingRepository bookingRepository, 
            IPaymentRepository paymentRepository,
            BookingService bookingService // Ми візьмемо з нього метод payBooking
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    /**
     * Єдиний метод "Фасаду" для запуску процесу оплати.
     * @param bookingId ID бронювання для оплати
     * @param paymentMethod ("LiqPay", "Stripe", "Cash" - для майбутнього Strategy)
     */
    @Transactional // Гарантує, що все або пройде, або відкотиться
    public Booking payForBooking(UUID bookingId, String paymentMethod) {
        
        System.out.println("--- [PaymentFacade] ---");
        System.out.println("Фасад отримав запит на оплату бронювання: " + bookingId);

        // 1. Отримуємо 'Context' (Booking)
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));

        // 2. Делегуємо логіку патерну State (ЛР4)
        //    'booking.pay()' перевірить, чи можна платити
        //    (кине IllegalStateException, якщо статус не 'CONFIRMED')
        //    і змінить статус на PAID.
        System.out.println("Фасад: Крок 1. Виклик патерну State (booking.pay())...");
        booking.pay(); // <-- Виклик ЛР4 (State)

        // 3. Симуляція виклику зовнішнього платіжного API
        //    (Тут буде логіка патерну Strategy в майбутньому)
        System.out.println("Фасад: Крок 2. Симуляція виклику API (" + paymentMethod + ")...");
        boolean paymentSuccess = simulateExternalPayment(booking.getTotalPrice());

        if (!paymentSuccess) {
            // Якщо оплата не вдалася, ми кидаємо виняток.
            // @Transactional автоматично "відкотить" зміну статусу
            throw new RuntimeException("Зовнішній платіж не вдалося виконати.");
        }

        // 4. Створюємо запис про платіж у БД
        System.out.println("Фасад: Крок 3. Створення запису Payment в БД...");
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // 5. Зберігаємо бронювання зі статусом PAID
        Booking savedBooking = bookingRepository.save(booking);

        // 6. Делегуємо повідомлення спостерігачам (ЛР6)
        //    Ми не можемо викликати bookingService.payBooking(), 
        //    бо це створить цикл. Ми викличемо eventPublisher.
        System.out.println("Фасад: Крок 4. Виклик патерну Observer...");
        // Ми викличемо eventPublisher з bookingService
        bookingService.notifyPaymentObservers(savedBooking); // Потрібно додати цей метод

        System.out.println("--- [PaymentFacade] Завершено ---");
        return savedBooking;
    }

    private boolean simulateExternalPayment(double amount) {
        // У реальному житті: виклик API LiqPay/Stripe
        System.out.println("...З'єднання з API... обробка " + amount + " грн... Успіх.");
        return true;
    }
}