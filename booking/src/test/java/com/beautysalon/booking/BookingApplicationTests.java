package com.beautysalon.booking;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Імпортуємо все, що потрібно для тестів
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Залишаємо, хоча для цього тесту Spring не потрібен
class BookingApplicationTests {

    /**
     * Цей тест перевіряє "щасливий шлях" — коректні переходи
     * зі стану в стан, як це робив би користувач.
     */
    @Test
    void testSuccessfulStateFlow() {
        System.out.println("--- Початок тесту 'testSuccessfulStateFlow' ---");
        
        // 1. Створюємо новий Booking. 
        // Він не під'єднаний до Spring чи БД. Це чистий Java-об'єкт.
        Booking booking = new Booking();
        
        // 2. Перевіряємо початковий стан
        // Конструктор має автоматично встановити PENDING
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        System.out.println("Стан (1): " + booking.getStatus());

        // 3. Підтверджуємо
        booking.confirm();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        System.out.println("Стан (2) після confirm(): " + booking.getStatus());

        // 4. Оплачуємо
        booking.pay();
        assertEquals(BookingStatus.PAID, booking.getStatus());
        System.out.println("Стан (3) після pay(): " + booking.getStatus());

        // 5. Завершуємо
        booking.complete();
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        System.out.println("Стан (4) після complete(): " + booking.getStatus());
        
        System.out.println("--- Тест 'testSuccessfulStateFlow' успішний ---");
    }

    /**
     * Цей тест перевіряє, чи патерн State
     * ПРАВИЛЬНО БЛОКУЄ недійсні переходи.
     */
    @Test
    void testInvalidStateTransitions() {
        System.out.println("--- Початок тесту 'testInvalidStateTransitions' ---");
        
        // Сценарій 1: Спроба оплатити бронювання, що в очікуванні (PENDING)
        Booking pendingBooking = new Booking();
        assertEquals(BookingStatus.PENDING, pendingBooking.getStatus());

        // Ми очікуємо, що 'PendingState' ки
        // 'IllegalStateException'
        Exception ex1 = assertThrows(IllegalStateException.class, () -> {
            pendingBooking.pay(); // Ця дія має впасти
        });
        
        assertEquals("Бронювання не може бути оплачено, поки воно не підтверджено.", ex1.getMessage());
        System.out.println("Перевірка (1) 'pay() on PENDING' успішна: " + ex1.getMessage());

        // Сценарій 2: Спроба скасувати завершене (COMPLETED) бронювання
        Booking completedBooking = new Booking();
        // Швидко "проганяємо" його до COMPLETED
        completedBooking.confirm();
        completedBooking.pay();
        completedBooking.complete();
        assertEquals(BookingStatus.COMPLETED, completedBooking.getStatus());

        Exception ex2 = assertThrows(IllegalStateException.class, () -> {
            completedBooking.cancel(); // Ця дія має впасти
        });

        assertEquals("Бронювання не може бути скасовано, оскільки воно вже завершено.", ex2.getMessage());
        System.out.println("Перевірка (2) 'cancel() on COMPLETED' успішна: " + ex2.getMessage());
        
        System.out.println("--- Тест 'testInvalidStateTransitions' успішний ---");
    }
}