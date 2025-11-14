package com.beautysalon.booking.composite;

/**
 * ІНТЕРФЕЙС "КОМПОНЕНТ" (Component)
 *
 * Визначає єдиний інтерфейс для "Листків" (Service)
 * та "Компонувальників" (ServicePackage).
 *
 * Це дозволяє клієнтському коду (напр. BookingService)
 * працювати з ними однаково.
 */
public interface BookableItem {
    
    /**
     * @return Загальна ціна (або однієї послуги,
     * або сума всіх послуг у пакеті).
     */
    double getPrice();

    /**
     * @return Загальна тривалість у хвилинах.
     */
    int getDurationMinutes();

    /**
     * @return Опис (назва послуги або назва пакету).
     */
    String getName();
}