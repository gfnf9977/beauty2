package com.beautysalon.booking.config;

import com.beautysalon.booking.entity.*;
import com.beautysalon.booking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final IMasterRepository masterRepository;
    private final IServiceRepository serviceRepository;
    private final IScheduleRepository scheduleRepository;
    private final PasswordEncoder passwordEncoder; // <--- 1. Додали енкодер

    public DatabaseInitializer(IUserRepository userRepository,
                               IMasterRepository masterRepository,
                               IServiceRepository serviceRepository,
                               IScheduleRepository scheduleRepository,
                               PasswordEncoder passwordEncoder) { // <--- 2. Інжектуємо його
        this.userRepository = userRepository;
        this.masterRepository = masterRepository;
        this.serviceRepository = serviceRepository;
        this.scheduleRepository = scheduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Якщо база порожня - заповнюємо
        if (userRepository.count() == 0) {
            System.out.println("--- [DatabaseInitializer] База порожня. Створення даних... ---");

            // 3. ВАЖЛИВО: Хешуємо паролі перед збереженням!
            String adminPass = passwordEncoder.encode("admin123");
            String masterPass = passwordEncoder.encode("master123");
            String clientPass = passwordEncoder.encode("client123");

            // 1. ADMIN
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail("admin@beauty.com");
            admin.setPassword(adminPass); // <--- Зберігаємо ХЕШ
            admin.setPhone("+380000000000");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // 2. MASTER
            User masterUser = new User();
            masterUser.setName("Іванна Шевченко");
            masterUser.setEmail("master@beauty.com");
            masterUser.setPassword(masterPass); // <--- Зберігаємо ХЕШ
            masterUser.setPhone("+380991234567");
            masterUser.setRole(Role.MASTER);
            userRepository.save(masterUser);

            Master masterProfile = new Master();
            masterProfile.setUser(masterUser);
            masterProfile.setSpecialization("Топ-стиліст");
            masterProfile.setExperience(5);
            masterRepository.save(masterProfile);

            // 3. CLIENT
            User client = new User();
            client.setName("Тестовий Клієнт");
            client.setEmail("client@test.com");
            client.setPassword(clientPass); // <--- Зберігаємо ХЕШ
            client.setPhone("+380509876543");
            client.setRole(Role.CLIENT);
            userRepository.save(client);

            // 4. SERVICE
            Service service = new Service();
            service.setName("Чоловіча стрижка");
            service.setDescription("Класична стрижка");
            service.setPrice(500.0);
            service.setDurationMinutes(60);
            service.setMaster(masterProfile);
            serviceRepository.save(service);

            // 5. SCHEDULE
            Schedule schedule = new Schedule();
            schedule.setMaster(masterProfile);
            schedule.setWorkDate(LocalDate.now().plusDays(1));
            schedule.setStartTime(LocalTime.of(9, 0));
            schedule.setEndTime(LocalTime.of(18, 0));
            scheduleRepository.save(schedule);

            System.out.println("--- [DatabaseInitializer] Готово! Паролі захешовано. ---");
        }
    }
}