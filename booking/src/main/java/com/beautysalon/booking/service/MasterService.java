package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Schedule;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IScheduleRepository;
import org.springframework.stereotype.Service;
import com.beautysalon.booking.dto.ScheduleDayDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
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

    public Schedule setMasterWorkSchedule(UUID masterId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new RuntimeException("Майстер не знайдений."));
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("Некоректний часовий проміжок.");
        }
        Schedule schedule = new Schedule(master, date, startTime, endTime);
        return scheduleRepository.save(schedule);
    }

    public Master findMasterByUser(UUID userId) {
        return masterRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("Профіль майстра не знайдено для цього користувача."));
    }

    public List<Schedule> getMasterSchedule(UUID masterId) {
        return scheduleRepository.findAllByMasterMasterId(masterId);
    }

    public Schedule addMasterSchedule(UUID masterId, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        Master master = masterRepository.findById(masterId)
            .orElseThrow(() -> new RuntimeException("Master not found with ID: " + masterId));
        Schedule newSchedule = new Schedule();
        newSchedule.setMaster(master);
        newSchedule.setWorkDate(workDate);
        newSchedule.setStartTime(startTime);
        newSchedule.setEndTime(endTime);
        return scheduleRepository.save(newSchedule);
    }

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
}
