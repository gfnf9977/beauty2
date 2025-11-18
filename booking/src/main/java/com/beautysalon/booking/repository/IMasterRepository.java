package com.beautysalon.booking.repository;

import com.beautysalon.booking.entity.Master;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface IMasterRepository extends JpaRepository<Master, UUID> {
    Optional<Master> findByUserUserId(UUID userId);
}