package com.beautysalon.booking.dto;

import com.beautysalon.booking.entity.Schedule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class ScheduleDayDto {
    private final LocalDate date;
    private final boolean isWorking;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final UUID scheduleId; // Для редагування

    // Конструктор для робочого дня
    public ScheduleDayDto(Schedule schedule) {
        this.date = schedule.getWorkDate();
        this.isWorking = true;
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.scheduleId = schedule.getScheduleId();
    }

    // Конструктор для вихідного дня
    public ScheduleDayDto(LocalDate date) {
        this.date = date;
        this.isWorking = false;
        this.startTime = null;
        this.endTime = null;
        this.scheduleId = null;
    }

    // --- Гетери ---
    public LocalDate getDate() { return date; }
    public boolean isWorking() { return isWorking; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public UUID getScheduleId() { return scheduleId; }
}