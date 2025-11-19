package com.beautysalon.booking.repository;

import com.beautysalon.booking.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findByMasterMasterIdAndWorkDate(UUID masterId, LocalDate date);

    @Query("SELECT DISTINCT s.workDate FROM Schedule s WHERE s.master.masterId = :masterId")
    List<LocalDate> findDistinctWorkDatesByMasterId(UUID masterId);
}
