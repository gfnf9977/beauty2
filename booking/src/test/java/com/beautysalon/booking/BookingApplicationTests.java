package com.beautysalon.booking;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;
import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Service;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IServiceRepository;
import com.beautysalon.booking.repository.IUserRepository;
import com.beautysalon.booking.service.BookingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional 
class BookingApplicationTests {
    
    // === Тести для ЛР4 (State) ===
    @Test
    void testSuccessfulStateFlow() {
        System.out.println("--- Тест ЛР4: testSuccessfulStateFlow ---");
        Booking booking = new Booking();
        booking.confirm();
        booking.pay();
        booking.complete();
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        System.out.println("--- Тест ЛР4: testSuccessfulStateFlow [УСПІХ] ---");
    }

    @Test
    void testInvalidStateTransitions() {
        System.out.println("--- Тест ЛР4: testInvalidStateTransitions ---");
        Booking pendingBooking = new Booking();
        assertThrows(IllegalStateException.class, pendingBooking::pay);
        System.out.println("--- Тест ЛР4: testInvalidStateTransitions [УСПІХ] ---");
    }

    // === Тести для ЛР5 (Chain of Responsibility) ===

    @Autowired
    private BookingService bookingService;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IMasterRepository masterRepository;
    @Autowired
    private IServiceRepository serviceRepository;
    
    private UUID validClientId;
    private UUID validMasterId;
    private UUID validServiceId;
    private LocalDateTime time = LocalDateTime.now();

    /**
     * Створюємо РЕАЛЬНІ дані в БД перед кожним тестом.
     */
    @BeforeEach
    void setUpRealDatabaseData() {
        // 1. Створюємо і НЕГАЙНО зберігаємо реального клієнта
        User client = new User("Test Client", "client@test.com", "pass", "123");
        userRepository.saveAndFlush(client); // <--- ВИРІШЕННЯ
        this.validClientId = client.getUserId();

        // 2. Створюємо і НЕГАЙНО зберігаємо реального майстра
        Master master = new Master(client, "Test Spec", 5);
        masterRepository.saveAndFlush(master); // <--- ВИРІШЕННЯ
        this.validMasterId = master.getMasterId();

        // 3. Створюємо і НЕГАЙНО зберігаємо реальну послугу
        com.beautysalon.booking.entity.Service service = 
            new com.beautysalon.booking.entity.Service("Test Service", "Desc", 100, 30);
        serviceRepository.saveAndFlush(service); // <--- ВИРІШЕННЯ
        this.validServiceId = service.getServiceId();
        
        // Тут НЕМАЄ дубліката
    }

    /**
     * Тест на ПРОВАЛ ланцюжка (на реальній БД).
     */
    @Test
    void testChainOfResponsibility_Failure_RealDB() {
        System.out.println("--- Тест ЛР5: testChainOfResponsibility_Failure (Real DB) ---");
        
        UUID invalidClientId = UUID.randomUUID(); 
        
        Exception ex = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(invalidClientId, validMasterId, validServiceId, time);
        });

        assertEquals("Клієнт не знайдений.", ex.getMessage());
        System.out.println("--- Тест ЛР5: testChainOfResponsibility_Failure [УСПІХ] ---");
    }
    
    /**
     * Тест на УСПІХ ланцюжка (на реальній БД).
     */
    @Test
    void testChainOfResponsibility_Success_RealDB() {
        System.out.println("--- Тест ЛР5: testChainOfResponsibility_Success (Real DB) ---");
        
        // Тепер, завдяки saveAndFlush(), findById() знайде всі сутності
        assertDoesNotThrow(() -> {
            bookingService.createBooking(validClientId, validMasterId, validServiceId, time);
        });
        
        System.out.println("--- Тест ЛР5: testChainOfResponsibility_Success [УСПІХ] ---");
    }
}