package com.beautysalon.booking.repository;

import com.beautysalon.booking.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IReviewRepository extends JpaRepository<Review, UUID> {
}