package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.PaymentFacade;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/web/bookings")
public class BookingWebController {

    private final BookingService bookingService;
    private final PaymentFacade paymentFacade;
    private final IServiceRepository serviceRepository;
    private final IMasterRepository masterRepository;

    public BookingWebController(BookingService bookingService, PaymentFacade paymentFacade, IServiceRepository serviceRepository, IMasterRepository masterRepository) {
        this.bookingService = bookingService;
        this.paymentFacade = paymentFacade;
        this.serviceRepository = serviceRepository;
        this.masterRepository = masterRepository;
    }

    // === 1. Сторінка створення бронювання (форма) ===
    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        // Передаємо списки в HTML для випадаючих списків (<select>)
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("masters", masterRepository.findAll());
        return "booking_create"; 
    }

    // === 2. Обробка створення (ЛР5 - Chain + ЛР6 - Observer + ЛР8 - Composite) ===
    @PostMapping("/create")
    public String createBooking(
            @RequestParam UUID serviceId,
            @RequestParam UUID masterId,
            @RequestParam String dateTime,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        try {
            // Тут спрацює Ланцюжок, Компонувальник і Спостерігач
            bookingService.createBooking(user.getUserId(), serviceId, masterId, LocalDateTime.parse(dateTime));
            return "redirect:/auth/home"; // Повертаємось на дашборд
        } catch (Exception e) {
            model.addAttribute("error", "Помилка створення: " + e.getMessage());
            // Повертаємо дані назад, щоб форма не була порожньою
            model.addAttribute("services", serviceRepository.findAll());
            model.addAttribute("masters", masterRepository.findAll());
            return "booking_create";
        }
    }

    // === 3. Дії зі станами (ЛР4 - State) ===

    @PostMapping("/{id}/confirm")
    public String confirmBooking(@PathVariable UUID id) {
        bookingService.confirmBooking(id);
        return "redirect:/auth/home";
    }

    // === 4. Оплата (ЛР7 - Facade) ===
    @PostMapping("/{id}/pay")
    public String payBooking(@PathVariable UUID id) {
        // Викликаємо Фасад!
        paymentFacade.payForBooking(id, "LiqPay");
        return "redirect:/auth/home";
    }

    @PostMapping("/{id}/complete")
    public String completeBooking(@PathVariable UUID id) {
        bookingService.completeBooking(id);
        return "redirect:/auth/home";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable UUID id) {
        bookingService.cancelBooking(id);
        return "redirect:/auth/home";
    }
}