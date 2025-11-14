package com.beautysalon.booking.validation;

import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Service;
import com.beautysalon.booking.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object), який передається по ланцюжку валідаторів.
 * Він містить як вхідні дані (ID), так і результат (сутності).
 */
public class BookingValidationContext {

    // Вхідні дані
    private final UUID clientId;
    private final UUID masterId;
    private final UUID serviceId;
    private final LocalDateTime dateTime;

    // Результати валідації (заповнюються хендлерами)
    private User client;
    private Master master;
    private Service service;
    
    // Поле для помилки (зупиняє ланцюжок)
    private String errorMessage;

    public BookingValidationContext(UUID clientId, UUID masterId, UUID serviceId, LocalDateTime dateTime) {
        this.clientId = clientId;
        this.masterId = masterId;
        this.serviceId = serviceId;
        this.dateTime = dateTime;
    }

    // === Геттери ===
    public UUID getClientId() { return clientId; }
    public UUID getMasterId() { return masterId; }
    public UUID getServiceId() { return serviceId; }
    public LocalDateTime getDateTime() { return dateTime; }
    
    public User getClient() { return client; }
    public Master getMaster() { return master; }
    public Service getService() { return service; }

    // === Сеттери для результатів ===
    public void setClient(User client) { this.client = client; }
    public void setMaster(Master master) { this.master = master; }
    public void setService(Service service) { this.service = service; }

    // === Управління помилками ===
    public boolean hasError() {
        return errorMessage != null;
    }
    
    public String getErrorMessage() { return errorMessage; }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}