package com.beautysalon.booking.repository;

import com.beautysalon.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface IBookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByClientUserIdOrderByBookingDateDesc(UUID userId);
    List<Booking> findByMasterMasterIdOrderByBookingDateDesc(UUID masterId);
}