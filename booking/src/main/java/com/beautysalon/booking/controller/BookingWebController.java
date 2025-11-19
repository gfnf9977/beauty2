package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.PaymentFacade;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        // Отримуємо унікальні назви для фільтрації на фронтенді
        Set<String> uniqueServiceNames = serviceRepository.findAll().stream()
            .map(com.beautysalon.booking.entity.Service::getName)
            .collect(Collectors.toSet());

        model.addAttribute("uniqueServiceNames", uniqueServiceNames);
        model.addAttribute("masters", masterRepository.findAll()); // Поки передаємо всіх
        return "booking_create";
    }

    @PostMapping("/create")
    public String createBooking(
            @RequestParam String serviceName,
            @RequestParam UUID masterId,
            @RequestParam String dateTime,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/auth/login";

        // КРОК 1: Знаходимо потрібний serviceId на основі Name та MasterId
        // Це гарантує, що ми отримуємо serviceId, який *дійсно* прив'язаний до обраного masterId
        List<com.beautysalon.booking.entity.Service> services = serviceRepository.findByName(serviceName);
        UUID finalServiceId = services.stream()
            .filter(s -> s.getMaster() != null && s.getMaster().getMasterId().equals(masterId))
            .map(s -> s.getServiceId())
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Не вдалося знайти конкретну послугу, прив'язану до обраного майстра."));

        try {
            // Тут спрацює Ланцюжок, Компонувальник і Спостерігач
            bookingService.createBooking(user.getUserId(), finalServiceId, masterId, LocalDateTime.parse(dateTime));
            return "redirect:/auth/home";
        } catch (Exception e) {
            model.addAttribute("error", "Помилка створення: " + e.getMessage());

            // Повторне завантаження даних для форми
            Set<String> uniqueServiceNames = serviceRepository.findAll().stream()
                .map(com.beautysalon.booking.entity.Service::getName)
                .collect(Collectors.toSet());
            model.addAttribute("uniqueServiceNames", uniqueServiceNames);
            model.addAttribute("masters", masterRepository.findAll());
            return "booking_create";
        }
    }

    @PostMapping("/{id}/confirm")
    public String confirmBooking(@PathVariable UUID id) {
        bookingService.confirmBooking(id);
        return "redirect:/auth/home";
    }

    @PostMapping("/{id}/pay")
    public String payBooking(@PathVariable UUID id) {
        // Викликаємо Фасад! (ЛР7)
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

    // === ФІКС ПОМИЛКИ КОМПІЛЯЦІЇ / AJAX API ===

    @GetMapping("/masters/by-service/{serviceId}")
    @ResponseBody
    public ResponseEntity<List<Master>> getMastersByService(@PathVariable UUID serviceId) {
        // Цей метод використовувався раніше, але зараз він частково дублює логіку
        // і буде видавати хибний результат, якщо є 1+ Master.
        // Ми залишаємо його для зворотної сумісності, але використовуємо findById.
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
        
        // Збираємо унікальний список майстрів з усіх знайдених послуг
        List<Master> availableMasters = services.stream()
            .map(com.beautysalon.booking.entity.Service::getMaster)
            .filter(master -> master != null)
            .distinct()
            .collect(Collectors.toList());
            
        return new ResponseEntity<>(availableMasters, HttpStatus.OK);
    }
}