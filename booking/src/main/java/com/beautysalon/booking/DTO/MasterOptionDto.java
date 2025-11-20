package com.beautysalon.booking.dto;

import java.util.UUID;

public class MasterOptionDto {
    private UUID masterId;
    private String fullName;
    private String specialization;
    private String rating; 

    public MasterOptionDto(UUID masterId, String fullName, String specialization, String rating) {
        this.masterId = masterId;
        this.fullName = fullName;
        this.specialization = specialization;
        this.rating = rating;
    }

    public UUID getMasterId() { return masterId; }
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getRating() { return rating; } 
}