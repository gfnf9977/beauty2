package com.beautysalon.booking.controller;

import com.beautysalon.booking.dto.ScheduleDayDto;
import com.beautysalon.booking.entity.*;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IScheduleRepository;
import com.beautysalon.booking.service.BookingService;
import com.beautysalon.booking.service.UserService;
import com.beautysalon.booking.service.MasterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/admin")
public class AdminWebController {

    private final BookingService bookingService;
    private final UserService userService;
    private final IServiceRepository serviceRepository;
    private final IMasterRepository masterRepository;
    private final MasterService masterService;
    private final IScheduleRepository scheduleRepository;

    public AdminWebController(
            BookingService bookingService,
            UserService userService,
            IServiceRepository serviceRepository,
            IMasterRepository masterRepository,
            MasterService masterService,
            IScheduleRepository scheduleRepository) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.serviceRepository = serviceRepository;
        this.masterRepository = masterRepository;
        this.masterService = masterService;
        this.scheduleRepository = scheduleRepository;
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
        var banned = userService.getUsersByRole(Role.BANNED);
        model.addAttribute("clients", clients);
        model.addAttribute("masters", masters);
        model.addAttribute("banned", banned);
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
        List<Service> allServices = serviceRepository.findAll();
        Map<String, List<Service>> groupedServices = allServices.stream()
                .collect(Collectors.groupingBy(Service::getName));
        model.addAttribute("groupedServices", groupedServices);
        model.addAttribute("admin", loggedInUser);
        return "admin_services_list";
    }

    @GetMapping("/services/new")
    public String showServiceForm(Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("masters", masterRepository.findAll());
        return "admin_service_form";
    }

    @GetMapping("/services/edit/{id}")
    public String showServiceEditForm(@PathVariable UUID id, Model model) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Послугу для редагування не знайдено."));
        model.addAttribute("service", service);
        model.addAttribute("masters", masterRepository.findAll());
        return "admin_service_form";
    }

    @PostMapping("/services/create")
    public String createService(@ModelAttribute Service service, @RequestParam UUID masterId) {
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

    @GetMapping("/masters/{userId}/schedule")
    public String showMasterSchedule(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != Role.ADMIN) {
            return "redirect:/auth/login";
        }
        Master master = masterService.findMasterByUser(userId);

        YearMonth currentMonth = month == null ? YearMonth.now() : month;

        List<ScheduleDayDto> monthlySchedule = masterService.getMonthlyScheduleView(master.getMasterId(), currentMonth);

        model.addAttribute("master", master);
        model.addAttribute("monthlySchedule", monthlySchedule);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("nextMonth", currentMonth.plusMonths(1));
        model.addAttribute("prevMonth", currentMonth.minusMonths(1));

        return "admin_master_schedule";
    }

    @GetMapping("/masters/{userId}/schedule/{scheduleIdOrNew}/edit")
    public String showEditScheduleForm(
            @PathVariable UUID userId,
            @PathVariable String scheduleIdOrNew,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpSession session,
            Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != Role.ADMIN) {
            return "redirect:/auth/login";
        }
        Master master = masterService.findMasterByUser(userId);
        Schedule schedule = new Schedule();
        schedule.setMaster(master);
        boolean isEdit = false;

        if (!"new".equals(scheduleIdOrNew)) {
            UUID scheduleId = UUID.fromString(scheduleIdOrNew);
            schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
            isEdit = true;
        } else if (date != null) {
            schedule.setWorkDate(date);
            schedule.setStartTime(LocalTime.of(9, 0));
            schedule.setEndTime(LocalTime.of(18, 0));
        } else {
            return "redirect:/web/admin/masters/" + userId + "/schedule";
        }
        model.addAttribute("master", master);
        model.addAttribute("schedule", schedule);
        model.addAttribute("isEdit", isEdit);

        return "admin_schedule_edit";
    }

    @PostMapping("/masters/{userId}/schedule/save")
    @Transactional
    public String saveSchedule(
            @PathVariable UUID userId,
            @RequestParam(required = false) UUID scheduleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false, defaultValue = "false") boolean delete,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || loggedInUser.getRole() != Role.ADMIN) {
            return "redirect:/auth/login";
        }
        Master master = masterService.findMasterByUser(userId);
        UUID masterId = master.getMasterId();

        if (delete && scheduleId != null) {
            scheduleRepository.deleteById(scheduleId);
            return "redirect:/web/admin/masters/" + userId + "/schedule?month=" + YearMonth.from(workDate);
        }

        Schedule schedule;
        if (scheduleId != null) {
            schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        } else {
            List<Schedule> existingSchedules = scheduleRepository.findByMasterMasterIdAndWorkDate(masterId, workDate);

            if (!existingSchedules.isEmpty()) {
                schedule = existingSchedules.get(0);
            } else {
                schedule = new Schedule();
                schedule.setMaster(master);
            }
        }

        schedule.setWorkDate(workDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        scheduleRepository.save(schedule);

        return "redirect:/web/admin/masters/" + userId + "/schedule?month=" + YearMonth.from(workDate);
    }
}
