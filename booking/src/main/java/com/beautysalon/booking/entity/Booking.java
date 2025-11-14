package com.beautysalon.booking.entity;

import com.beautysalon.booking.state.*; // <-- Імпортуємо наші чисті стани
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// БІЛЬШЕ НЕМАЄ жодних імпортів Spring (ApplicationContext, @Autowired)

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID bookingId;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    private LocalDate bookingDate;
    private LocalTime bookingTime;

    // === Зміни для State Pattern ===

    @Enumerated(EnumType.STRING) // <-- Зберігаємо enum у БД як рядок
    @Column(name = "status")
    private BookingStatus status;

    @Transient // <-- НЕ зберігаємо в БД, це поле для логіки [cite: 137]
    private BookingState state;

    // === Кінець змін ===

    private double totalPrice;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    /**
     * Конструктор за замовчуванням.
     * Встановлює початковий стан при створенні нового бронювання.
     */
    public Booking() {
        this.status = BookingStatus.PENDING;
        initState(); // Ініціалізуємо об'єкт стану
    }

    /**
     * Метод, що викликається JPA ПІСЛЯ завантаження сутності з БД.
     * Використовується для ініціалізації поля 'state' на основі 'status'.
     */
    @PostLoad
    private void onPostLoad() {
        initState();
    }

    /**
     * Ініціалізує поле 'state' на основі поточного 'status'.
     * Це "ручна" реалізація, яка не використовує Spring.
     * Викликає Singleton-екземпляри станів.
     */
    private void initState() {
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        
        // Використовуємо 'getInstance()' замість 'new ...' або 'context.getBean(...)'
        this.state = switch (status) {
            case PENDING   -> PendingState.getInstance();
            case CONFIRMED -> ConfirmedState.getInstance();
            case PAID      -> PaidState.getInstance();
            case COMPLETED -> CompletedState.getInstance();
            case CANCELLED -> CancelledState.getInstance();
        };
    }

    // === Делегування дій поточному стану ===
    // 'Booking' (Context) не знає логіки, він просто делегує[cite: 156].
    
    public void confirm() {
        state.confirm(this);
    }
    
    public void pay() {
        state.pay(this);
    }
    
    public void cancel() {
        state.cancel(this);
    }
    
    public void complete() {
        state.complete(this);
    }

    // === Геттери та Сеттери ===

    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    public Master getMaster() { return master; }
    public void setMaster(Master master) { this.master = master; }
    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public LocalTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalTime bookingTime) { this.bookingTime = bookingTime; }
    
    public BookingStatus getStatus() { return status; }
    
    /**
     * Ключовий метод!
     * Коли стан (State) змінює 'status' бронювання,
     * цей сеттер автоматично викликає 'initState()' для
     * оновлення об'єкта 'state'.
     */
    public void setStatus(BookingStatus status) {
        this.status = status;
        initState(); // <-- Синхронізація об'єкта 'state' зі 'status'
    }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}