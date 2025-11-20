package com.beautysalon.booking.dto;

import java.util.UUID;

public class MasterOptionDto {
    private UUID masterId;
    private String fullName;
    private String specialization;

    public MasterOptionDto(UUID masterId, String fullName, String specialization) {
        this.masterId = masterId;
        this.fullName = fullName;
        this.specialization = specialization;
    }

    public UUID getMasterId() { return masterId; }
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
}