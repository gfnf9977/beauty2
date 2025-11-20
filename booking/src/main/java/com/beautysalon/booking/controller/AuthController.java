package com.beautysalon.booking.controller;

import com.beautysalon.booking.entity.Role;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.dto.ScheduleDayDto;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.MasterService;
import com.beautysalon.booking.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; 
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

    // === Сторінка входу ===
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth_login";
    }

    // === Обробка логіну ===
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

    // === Сторінка реєстрації ===
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    // === Обробка реєстрації ===
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

    // === ДАШБОРД КЛІЄНТА (ОНОВЛЕНО) ===
    @GetMapping("/home")
    @Transactional // <-- ЦЯ АНОТАЦІЯ ВИПРАВЛЯЄ ПОМИЛКУ 500
    public String showHomePage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser != null) {
            // Отримуємо свіжого користувача. 
            // Завдяки @Transactional, всі його зв'язки (bookings, master, service) 
            // будуть доступні для Thymeleaf.
            User freshUser = userService.findByEmail(loggedInUser.getEmail()).orElse(loggedInUser);
            
            model.addAttribute("user", freshUser);
            return "dashboard"; 
        } else {
            return "redirect:/auth/login";
        }
    }

    // === ДАШБОРД АДМІНА ===
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

    // === ДАШБОРД МАЙСТРА ===
    @GetMapping("/master/dashboard")
    public String showMasterDashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        if (loggedInUser != null && loggedInUser.getRole() == Role.MASTER) {
            try {
                Master masterProfile = masterService.findMasterByUser(loggedInUser.getUserId());
                UUID masterId = masterProfile.getMasterId();
                
                List<Booking> bookings = bookingService.getBookingsByMaster(masterId); 
                
                // 2. Календарна логіка (статична, поточний місяць)
                YearMonth currentMonth = YearMonth.now();
                List<ScheduleDayDto> monthlySchedule = masterService.getMonthlyScheduleView(masterId, currentMonth);

                model.addAttribute("master", masterProfile);
                model.addAttribute("bookings", bookings);
                model.addAttribute("monthlySchedule", monthlySchedule);
                model.addAttribute("currentMonth", currentMonth);
                
                return "master_dashboard"; 
                
            } catch (RuntimeException e) {
                model.addAttribute("error", "Помилка завантаження профілю: " + e.getMessage());
                return "auth_login";
            }
        } else {
            return "redirect:/auth/login";
        }
    }
}