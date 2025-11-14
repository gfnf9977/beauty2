package com.beautysalon.booking.composite;

import java.util.ArrayList;
import java.util.List;

/**
 * "КОМПОНУВАЛЬНИК" (Composite)
 *
 * Представляє собою пакет послуг. Він реалізує той самий
 * інтерфейс, що й 'Service', але всередині містить список
 * інших 'BookableItem'.
 */
public class ServicePackage implements BookableItem {

    private String name;
    private List<BookableItem> items = new ArrayList<>();

    public ServicePackage(String name) {
        this.name = name;
    }

    // === Методи для управління пакетом ===
    public void addItem(BookableItem item) {
        items.add(item);
    }
    
    public void removeItem(BookableItem item) {
        items.remove(item);
    }

    // === Реалізація інтерфейсу ===

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Патерн Composite в дії:
     * Ціна пакету - це сума цін всіх його компонентів.
     */
    @Override
    public double getPrice() {
        double totalPrice = 0;
        for (BookableItem item : items) {
            totalPrice += item.getPrice();
        }
        // Можна додати логіку знижки, напр: return totalPrice * 0.9;
        return totalPrice;
    }

    /**
     * Патерн Composite в дії:
     * Тривалість пакету - це сума тривалостей всіх його компонентів.
     */
    @Override
    public int getDurationMinutes() {
        int totalDuration = 0;
        for (BookableItem item : items) {
            totalDuration += item.getDurationMinutes();
        }
        return totalDuration;
    }
}