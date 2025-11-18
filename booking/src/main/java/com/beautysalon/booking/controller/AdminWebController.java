package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.Role;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/web/admin")
public class AdminWebController {

    private final BookingService bookingService;
    private final UserService userService;

    public AdminWebController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/bookings")
    public String showAllBookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null || loggedInUser.getRole() != Role.ADMIN) {
            return "redirect:/auth/login";
        }

        List<Booking> allBookings = bookingService.getAllBookings();

        model.addAttribute("bookings", allBookings);
        model.addAttribute("admin", loggedInUser);

        return "admin_bookings_list";
    }

    // === НОВИЙ МЕТОД: Сторінка управління користувачами ===
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
}
