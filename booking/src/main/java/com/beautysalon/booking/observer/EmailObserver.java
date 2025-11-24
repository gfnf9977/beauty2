package com.beautysalon.booking.observer;

import com.beautysalon.booking.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailObserver implements IBookingObserver {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailObserver(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async 
    public void update(Booking booking) {
        try {
            String toEmail = booking.getClient().getEmail();
            String clientName = booking.getClient().getName();
            String serviceName = booking.getService().getName();
            String masterName = booking.getMaster().getUser().getName();
            String bookingTime = booking.getBookingDate() + " о " + booking.getBookingTime();
            String status = booking.getStatus().name(); // PENDING, CONFIRMED...
            
            // 1. Перекладаємо статус на українську мову
            String statusUa = switch (status) {
                case "PENDING" -> "Очікує підтвердження";
                case "CONFIRMED" -> "Підтверджено адміністратором";
                case "PAID" -> "Оплачено";
                case "COMPLETED" -> "Виконано (Завершено)";
                case "CANCELLED" -> "Скасовано";
                default -> status;
            };

            String subject = "Оновлення статусу: " + statusUa;
            
            StringBuilder text = new StringBuilder();
            text.append("Вітаємо, ").append(clientName).append("!\n\n");
            text.append("Статус вашого запису на послугу \"").append(serviceName).append("\" змінено.\n");
            text.append("--------------------------------------------------\n");
            text.append("Майстер: ").append(masterName).append("\n");
            text.append("Час візиту: ").append(bookingTime).append("\n");
            text.append("Новий статус: ").append(statusUa.toUpperCase()).append("\n");
            text.append("--------------------------------------------------\n\n");
            
            // Додаємо підказки залежно від статусу
            if ("CONFIRMED".equals(status)) {
                text.append("✅ Ваше бронювання підтверджено! Будь ласка, перейдіть до особистого кабінету для оплати:\n");
                text.append("http://localhost:8080/auth/login\n\n"); // Посилання на вхід
            } else if ("PAID".equals(status)) {
                text.append("💰 Оплата пройшла успішно. Чекаємо на вас у салоні!\n\n");
            } else if ("COMPLETED".equals(status)) {
                text.append("Дякуємо, що завітали до нас! Будемо вдячні за ваш відгук про майстра.\n");
                text.append("Ви можете залишити його у своєму кабінеті:\n");
                text.append("http://localhost:8080/auth/home\n\n");
            }

            text.append("З повагою,\nКоманда Beauty Salon");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply.beautysalon@gmail.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text.toString());

            mailSender.send(message);
            
            System.out.println("📧 [EmailObserver] Лист успішно відправлено на " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ [EmailObserver] Помилка відправки пошти: " + e.getMessage());
        }
    }
}