package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.PaymentFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/bookings")
public class BookingWebController {
    private final BookingService bookingService;
    private final PaymentFacade paymentFacade;
    private final IServiceRepository serviceRepository;
    private final IMasterRepository masterRepository;

    public BookingWebController(
            BookingService bookingService,
            PaymentFacade paymentFacade,
            IServiceRepository serviceRepository,
            IMasterRepository masterRepository) {
        this.bookingService = bookingService;
        this.paymentFacade = paymentFacade;
        this.serviceRepository = serviceRepository;
        this.masterRepository = masterRepository;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        Set<String> uniqueServiceNames = serviceRepository.findAll().stream()
            .map(com.beautysalon.booking.entity.Service::getName)
            .collect(Collectors.toSet());

        model.addAttribute("uniqueServiceNames", uniqueServiceNames);
        model.addAttribute("masters", masterRepository.findAll());
        return "booking_create";
    }

    @PostMapping("/create")
    public String createBooking(
            @RequestParam String serviceName,
            @RequestParam UUID masterId,
            @RequestParam String bookingDate,
            @RequestParam String bookingTime,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        LocalDateTime finalDateTime = LocalDateTime.parse(bookingDate + "T" + bookingTime);
        List<com.beautysalon.booking.entity.Service> services = serviceRepository.findByName(serviceName);
        UUID finalServiceId = services.stream()
            .filter(s -> s.getMaster() != null && s.getMaster().getMasterId().equals(masterId))
            .map(s -> s.getServiceId())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Не вдалося знайти конкретну послугу, прив'язану до обраного майстра."));

        try {
            bookingService.createBooking(user.getUserId(), finalServiceId, masterId, finalDateTime);
            return "redirect:/auth/home";
        } catch (Exception e) {
            model.addAttribute("error", "Помилка створення: " + e.getMessage());
            Set<String> uniqueServiceNames = serviceRepository.findAll().stream()
                .map(com.beautysalon.booking.entity.Service::getName)
                .collect(Collectors.toSet());
            model.addAttribute("uniqueServiceNames", uniqueServiceNames);
            model.addAttribute("masters", masterRepository.findAll());
            return "booking_create";
        }
    }

    @PostMapping("/{id}/confirm")
    public String confirmBooking(@PathVariable UUID id, HttpServletRequest request) {
        bookingService.confirmBooking(id);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/auth/home");
    }

    @PostMapping("/{id}/pay")
    public String payBooking(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "CARD") String paymentMethod,
            @RequestParam String cardNumber,
            HttpServletRequest request) {

        paymentFacade.payForBooking(id, paymentMethod, cardNumber);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/auth/home");
    }

    @PostMapping("/{id}/complete")
    public String completeBooking(@PathVariable UUID id, HttpServletRequest request) {
        bookingService.completeBooking(id);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/auth/home");
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable UUID id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            Booking cancelledBooking = bookingService.cancelBooking(id);

            if (cancelledBooking.getPayment() != null && "PAID".equals(cancelledBooking.getPayment().getPaymentStatus())) {
                String refundMessage = paymentFacade.refundBooking(id);
                redirectAttributes.addFlashAttribute("success", "Бронювання скасовано. " + refundMessage);
            } else {
                redirectAttributes.addFlashAttribute("success", "Бронювання успішно скасовано.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка скасування: " + e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/auth/home");
    }

    @GetMapping("/{id}/receipt")
    public String getBookingReceipt(@PathVariable UUID id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        Booking booking = bookingService.findBookingById(id)
                .orElseThrow(() -> new RuntimeException("Бронювання не знайдено"));

        model.addAttribute("booking", booking);
        model.addAttribute("payment", booking.getPayment());

        return "receipt";
    }

    @GetMapping("/masters/by-service/{serviceId}")
    @ResponseBody
    public ResponseEntity<List<Master>> getMastersByService(@PathVariable UUID serviceId) {
        Optional<com.beautysalon.booking.entity.Service> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isPresent()) {
            Master master = serviceOpt.get().getMaster();
            if (master != null) {
                return new ResponseEntity<>(List.of(master), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(List.of(), HttpStatus.OK);
    }

    @GetMapping("/masters/by-service-name/{serviceName}")
    @ResponseBody
    public ResponseEntity<List<Master>> getMastersByServiceName(@PathVariable String serviceName) {
        List<com.beautysalon.booking.entity.Service> services = serviceRepository.findByName(serviceName);
        if (services.isEmpty()) {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        List<Master> availableMasters = services.stream()
            .map(com.beautysalon.booking.entity.Service::getMaster)
            .filter(master -> master != null)
            .distinct()
            .collect(Collectors.toList());
        return new ResponseEntity<>(availableMasters, HttpStatus.OK);
    }

    @GetMapping("/slots/available")
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableTimeSlots(
            @RequestParam UUID masterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LocalTime> allWorkingSlots = new ArrayList<>();
        for (int hour = 8; hour < 20; hour++) {
            allWorkingSlots.add(LocalTime.of(hour, 0));
        }
        Set<LocalTime> occupiedSlots = bookingService.getOccupiedSlots(masterId, date);
        List<String> availableSlots = allWorkingSlots.stream()
            .filter(slot -> !occupiedSlots.contains(slot))
            .map(LocalTime::toString)
            .collect(Collectors.toList());
        return new ResponseEntity<>(availableSlots, HttpStatus.OK);
    }

    @GetMapping("/dates/working/{masterId}")
    @ResponseBody
    public ResponseEntity<List<LocalDate>> getMasterWorkingDates(@PathVariable UUID masterId) {
        List<LocalDate> workingDates = bookingService.getMasterWorkingDates(masterId);
        return new ResponseEntity<>(workingDates, HttpStatus.OK);
    }
}
