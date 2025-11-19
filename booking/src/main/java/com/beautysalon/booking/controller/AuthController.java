package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Role;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.dto.ScheduleDayDto;
import com.beautysalon.booking.service.UserService;
import com.beautysalon.booking.service.MasterService;
import com.beautysalon.booking.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final MasterService masterService;
    private final BookingService bookingService;

    public AuthController(UserService userService, MasterService masterService, BookingService bookingService) {
        this.userService = userService;
        this.masterService = masterService;
        this.bookingService = bookingService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth_login";
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        User user = userService.login(email, password);
        if (user != null) {
            session.setAttribute("loggedInUser", user);
            if (user.getRole() == Role.ADMIN) {
                return "redirect:/auth/admin/dashboard";
            } else if (user.getRole() == Role.MASTER) {
                return "redirect:/auth/master/dashboard";
            } else {
                return "redirect:/auth/home";
            }
        } else {
            model.addAttribute("error", "Невірний email або пароль");
            return "auth_login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Користувач з таким email вже існує!");
            model.addAttribute("user", user);
            return "register";
        }
        userService.save(user);
        redirectAttributes.addFlashAttribute("success", "Реєстрація успішна! Увійдіть.");
        return "redirect:/auth/login";
    }

    @GetMapping("/home")
    public String showHomePage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            User freshUser = userService.findByEmail(loggedInUser.getEmail()).orElse(loggedInUser);
            model.addAttribute("user", freshUser);
            return "dashboard";
        } else {
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null && loggedInUser.getRole() == Role.ADMIN) {
            model.addAttribute("admin", loggedInUser);
            return "admin_dashboard";
        } else {
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/master/dashboard")
    public String showMasterDashboard(
            HttpSession session,
            Model model,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != Role.MASTER) {
            return "redirect:/auth/login";
        }

        try {
            Master masterProfile = masterService.findMasterByUser(loggedInUser.getUserId());
            UUID masterId = masterProfile.getMasterId();

            List<Booking> bookings = bookingService.getBookingsByMaster(masterId);

            YearMonth currentMonth = month == null ? YearMonth.now() : month;
            List<ScheduleDayDto> monthlySchedule = masterService.getMonthlyScheduleView(masterProfile.getMasterId(), currentMonth);

            model.addAttribute("master", masterProfile);
            model.addAttribute("bookings", bookings);
            model.addAttribute("monthlySchedule", monthlySchedule);
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("nextMonth", currentMonth.plusMonths(1));
            model.addAttribute("prevMonth", currentMonth.minusMonths(1));

            return "master_dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Помилка завантаження розкладу: " + e.getMessage());
            return "auth/login";
        }
    }
}
