package com.beautysalon.booking.service;

import com.beautysalon.booking.dto.ScheduleDayDto;
import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Schedule;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MasterService {

    private final IMasterRepository masterRepository;
    private final IScheduleRepository scheduleRepository;

    public MasterService(IMasterRepository masterRepository, IScheduleRepository scheduleRepository) {
        this.masterRepository = masterRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public Master addMaster(Master newMaster) {
        return masterRepository.save(newMaster);
    }

    public Master findMasterByUser(UUID userId) {
        return masterRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("Профіль майстра не знайдено."));
    }
    
    public Master findMasterById(UUID masterId) {
         return masterRepository.findById(masterId)
                .orElseThrow(() -> new RuntimeException("Майстра не знайдено"));
    }

    // === НОВИЙ МЕТОД ДЛЯ ОНОВЛЕННЯ ПРОФІЛЮ (Спеціалізація) ===
    @Transactional
    public void updateMasterProfile(UUID masterId, String specialization, int experience) {
        Master master = findMasterById(masterId);
        master.setSpecialization(specialization);
        master.setExperience(experience);
        masterRepository.save(master);
    }

    public List<Schedule> getMasterSchedule(UUID masterId) {
        return scheduleRepository.findAllByMasterMasterId(masterId);
    }
    
    // === МЕТОДИ ДЛЯ КАЛЕНДАРЯ ===

    public List<ScheduleDayDto> getMonthlyScheduleView(UUID masterId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Schedule> schedules = scheduleRepository.findByMasterMasterIdAndWorkDateBetween(masterId, startDate, endDate);

        Map<LocalDate, Schedule> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(Schedule::getWorkDate, s -> s));

        List<ScheduleDayDto> monthlyView = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (scheduleMap.containsKey(date)) {
                monthlyView.add(new ScheduleDayDto(scheduleMap.get(date)));
            } else {
                monthlyView.add(new ScheduleDayDto(date));
            }
            date = date.plusDays(1);
        }
        return monthlyView;
    }

    public Schedule getScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    public void deleteSchedule(UUID scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    public void upsertSchedule(UUID scheduleId, UUID masterId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        Schedule schedule;
        if (scheduleId != null) {
            schedule = getScheduleById(scheduleId);
        } else {
            List<Schedule> existing = scheduleRepository.findByMasterMasterIdAndWorkDate(masterId, workDate);
            if (!existing.isEmpty()) {
                 schedule = existing.get(0);
            } else {
                 schedule = new Schedule();
                 schedule.setMaster(masterRepository.findById(masterId).orElseThrow());
            }
        }
        schedule.setWorkDate(workDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        scheduleRepository.save(schedule);
    }
    
    // Для сумісності зі старим кодом AdminController (якщо використовується)
    public Schedule setMasterWorkSchedule(UUID masterId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        upsertSchedule(null, masterId, workDate, startTime, endTime);
        return scheduleRepository.findByMasterMasterIdAndWorkDate(masterId, workDate).stream().findFirst().orElse(null);
    }
}