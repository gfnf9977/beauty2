package com.beautysalon.booking.entity;

/**
 * Enum, що представляє статус бронювання,
 * який буде зберігатися в базі даних.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    PAID,
    COMPLETED,
    CANCELLED
}