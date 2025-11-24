package com.beautysalon.booking.composite;
import java.util.ArrayList;
import java.util.List;

public class ServicePackage implements BookableItem {
    private String name;
    private List<BookableItem> items = new ArrayList<>();

    public ServicePackage(String name) {
        this.name = name;
    }

    public void addItem(BookableItem item) {
        items.add(item);
    }

    public void removeItem(BookableItem item) {
        items.remove(item);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public double getPrice() {
        double totalPrice = 0;
        for (BookableItem item : items) {
            totalPrice += item.getPrice();
        }
        return totalPrice;
    }

    @Override
    public int getDurationMinutes() {
        int totalDuration = 0;
        for (BookableItem item : items) {
            totalDuration += item.getDurationMinutes();
        }
        return totalDuration;
    }
}
