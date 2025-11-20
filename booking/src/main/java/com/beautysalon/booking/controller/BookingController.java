package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.PaymentFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final PaymentFacade paymentFacade;

    public BookingController(
            BookingService bookingService,
            PaymentFacade paymentFacade) {
        this.bookingService = bookingService;
        this.paymentFacade = paymentFacade;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestParam UUID clientId,
            @RequestParam UUID serviceId,
            @RequestParam UUID masterId,
            @RequestParam String dateTime,
            @RequestParam(required = false, defaultValue = "false") boolean allInclusive) {
        try {
            LocalDateTime desiredDateTime = LocalDateTime.parse(dateTime);
            Booking newBooking = bookingService.createBooking(
                    clientId, serviceId, masterId, desiredDateTime, allInclusive);
            return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Booking>> getClientBookings(@PathVariable UUID clientId) {
        List<Booking> bookings = bookingService.getBookingsByClient(clientId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable UUID bookingId) {
        try {
            Booking booking = bookingService.confirmBooking(bookingId);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<?> payBooking(
            @PathVariable UUID bookingId,
            @RequestParam(defaultValue = "CARD") String paymentMethod,
            @RequestParam(defaultValue = "0000") String cardNumber) {
        try {
            Booking booking = paymentFacade.payForBooking(bookingId, paymentMethod, cardNumber);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable UUID bookingId) {
        try {
            Booking booking = bookingService.completeBooking(bookingId);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable UUID bookingId) {
        try {
            Booking cancelledBooking = bookingService.cancelBooking(bookingId);
            return new ResponseEntity<>(cancelledBooking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{bookingId}/cancel-with-refund")
    public ResponseEntity<String> cancelBookingWithRefund(@PathVariable UUID bookingId) {
        try {
            Booking cancelledBooking = bookingService.cancelBooking(bookingId);
            if (cancelledBooking.getStatus() == BookingStatus.CANCELLED) {
                String refundMessage = paymentFacade.refundBooking(bookingId);
                return new ResponseEntity<>(refundMessage, HttpStatus.OK);
            }
            return new ResponseEntity<>("Бронювання скасовано, оплати не було.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
