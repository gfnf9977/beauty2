package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Schedule;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class MasterService {

    private final IMasterRepository masterRepository;
    private final IScheduleRepository scheduleRepository;

    // Конструктор (прибрали userRepository, бо він не використовується)
    public MasterService(IMasterRepository masterRepository, IScheduleRepository scheduleRepository) {
        this.masterRepository = masterRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // Сценарій: Додати/видалити/змінити майстра (Додавання)
    public Master addMaster(Master newMaster) {
        return masterRepository.save(newMaster);
    }

    // Сценарій: Призначити розклад роботи майстру
    public Schedule setMasterWorkSchedule(UUID masterId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new RuntimeException("Майстер не знайдений."));

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("Некоректний часовий проміжок.");
        }
        
        Schedule schedule = new Schedule(master, date, startTime, endTime);
        return scheduleRepository.save(schedule);
    }

    // === НОВИЙ МЕТОД (Для відображення розкладу в кабінеті) ===
    public Master findMasterByUser(UUID userId) {
        return masterRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("Профіль майстра не знайдено для цього користувача."));
    }
}