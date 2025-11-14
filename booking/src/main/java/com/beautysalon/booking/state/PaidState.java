package com.beautysalon.booking.state;

import com.beautysalon.booking.entity.Booking;
import com.beautysalon.booking.entity.BookingStatus;

public class PaidState implements BookingState {

    private static final PaidState INSTANCE = new PaidState();
    private PaidState() {}
    public static PaidState getInstance() {
        return INSTANCE;
    }

    @Override
    public void confirm(Booking booking) {
        // Вже підтверджено (і оплачено), нічого не робимо
    }

    @Override
    public void pay(Booking booking) {
        throw new IllegalStateException("Бронювання вже оплачено.");
    }

    @Override
    public void cancel(Booking booking) {
        // Дозволяємо скасування (з логікою повернення коштів, якої поки немає)
        booking.setStatus(BookingStatus.CANCELLED);
        // TODO: Тут має бути логіка повернення коштів
    }

    @Override
    public void complete(Booking booking) {
        booking.setStatus(BookingStatus.COMPLETED);
    }
}