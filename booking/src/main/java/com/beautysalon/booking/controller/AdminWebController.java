package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Role;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/web/admin")
public class AdminWebController {
    private final BookingService bookingService;
    private final UserService userService;
    private final IServiceRepository serviceRepository;
    private final IMasterRepository masterRepository;

    public AdminWebController(
            BookingService bookingService,
            UserService userService,
            IServiceRepository serviceRepository,
            IMasterRepository masterRepository) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.serviceRepository = serviceRepository;
        this.masterRepository = masterRepository;
    }

    @GetMapping("/bookings")
    public String showAllBookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != Role.ADMIN) {
            return "redirect:/auth/login";
        }
        var allBookings = bookingService.getAllBookings();
        model.addAttribute("bookings", allBookings);
        model.addAttribute("admin", loggedInUser);
        return "admin_bookings_list";
    }

    @GetMapping("/users")
    public String showAllUsers(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != Role.ADMIN) {
            return "redirect:/auth/login";
        }
        var clients = userService.getUsersByRole(Role.CLIENT);
        var masters = userService.getUsersByRole(Role.MASTER);
        model.addAttribute("clients", clients);
        model.addAttribute("masters", masters);
        model.addAttribute("admin", loggedInUser);
        return "admin_users_list";
    }

    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable UUID id, @RequestParam String role) {
        Role newRole = Role.valueOf(role.toUpperCase());
        userService.updateUserRole(id, newRole);
        return "redirect:/web/admin/users";
    }

    @GetMapping("/services")
    @Transactional
    public String showAllServices(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != Role.ADMIN) {
            return "redirect:/auth/login";
        }
        var allServices = serviceRepository.findAll();
        model.addAttribute("services", allServices);
        model.addAttribute("admin", loggedInUser);
        return "admin_services_list";
    }

    @GetMapping("/services/new")
    public String showServiceForm(Model model) {
        model.addAttribute("service", new com.beautysalon.booking.entity.Service());
        model.addAttribute("masters", masterRepository.findAll());
        return "admin_service_form";
    }

    @GetMapping("/services/edit/{id}")
    public String showServiceEditForm(@PathVariable UUID id, Model model) {
        com.beautysalon.booking.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Послугу для редагування не знайдено."));
        model.addAttribute("service", service);
        model.addAttribute("masters", masterRepository.findAll());
        return "admin_service_form";
    }

    @PostMapping("/services/create")
    public String createService(@ModelAttribute com.beautysalon.booking.entity.Service service, @RequestParam UUID masterId) {
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new RuntimeException("Майстер не знайдений для прив'язки послуги."));
        service.setMaster(master);
        bookingService.addService(service);
        return "redirect:/web/admin/services";
    }

    @PostMapping("/services/delete/{id}")
    public String deleteService(@PathVariable UUID id) {
        bookingService.deleteService(id);
        return "redirect:/web/admin/services";
    }
}
