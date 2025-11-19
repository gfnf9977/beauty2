package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;
import com.beautysalon.booking.entity.Payment;
import com.beautysalon.booking.repository.IBookingRepository;
import com.beautysalon.booking.repository.IPaymentRepository;
import com.beautysalon.booking.payment.PaymentStrategy;
import com.beautysalon.booking.payment.PaymentStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentFacade {
    private final IBookingRepository bookingRepository;
    private final IPaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final PaymentStrategyFactory strategyFactory;

    public PaymentFacade(
            IBookingRepository bookingRepository,
            IPaymentRepository paymentRepository,
            BookingService bookingService,
            PaymentStrategyFactory strategyFactory) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.strategyFactory = strategyFactory;
    }

    @Transactional
    public Booking payForBooking(UUID bookingId, String paymentMethod, String cardNumber) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));

        booking.pay();

        PaymentStrategy strategy = strategyFactory.getStrategy(paymentMethod);

        if (!strategy.processPayment(booking.getTotalPrice(), cardNumber)) {
            throw new RuntimeException("Зовнішній платіж " + strategy.getId() + " не вдалося виконати.");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(strategy.getId());
        payment.setPaymentStatus("PAID");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCardNumber(cardNumber);
        paymentRepository.save(payment);

        Booking savedBooking = bookingRepository.save(booking);
        bookingService.notifyPaymentObservers(savedBooking);
        return savedBooking;
    }

    @Transactional
    public String refundBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено."));

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new IllegalStateException("Повернення коштів можливе лише для скасованих бронювань.");
        }

        Payment payment = paymentRepository.findByBookingBookingId(bookingId);
        if (payment == null || payment.getPaymentStatus().equals("REFUNDED")) {
            throw new IllegalStateException("Платіж не знайдено або вже повернено.");
        }

        PaymentStrategy strategy = strategyFactory.getStrategy(payment.getPaymentMethod());

        if (strategy.processRefund(payment.getAmount())) {
            payment.setPaymentStatus("REFUNDED");
            paymentRepository.save(payment);

            String maskedCard = "**** **** **** " + payment.getCardNumber().substring(payment.getCardNumber().length() - 4);
            return "Кошти у розмірі " + payment.getAmount() + " грн повернено на рахунок: " + maskedCard;
        }

        throw new RuntimeException("Помилка повернення коштів через платіжний шлюз.");
    }
}
