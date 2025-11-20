package com.beautysalon.booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "masters")
public class Master {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID masterId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String specialization;
    private int experience;

    @JsonIgnore
    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<Service> services;

    @JsonIgnore
    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    @JsonIgnore
    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    public Master() {}

    public Master(User user, String specialization, int experience) {
        this.user = user;
        this.specialization = specialization;
        this.experience = experience;
    }

    public UUID getMasterId() {
        return masterId;
    }

    public void setMasterId(UUID masterId) {
        this.masterId = masterId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Review> getMasterReviews() {
        if (bookings == null) return new ArrayList<>();

        return bookings.stream()
                .map(Booking::getReview)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public String getFormattedRating() {
        List<Review> reviews = getMasterReviews();

        if (reviews.isEmpty()) {
            return "-";
        }

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return String.format("%.1f", average).replace(',', '.');
    }
}
