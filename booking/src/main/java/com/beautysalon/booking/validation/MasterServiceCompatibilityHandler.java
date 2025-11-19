package com.beautysalon.booking.validation;

/**
 * КОНКРЕТНИЙ ОБРОБНИК: MasterServiceCompatibilityHandler
 *
 * Перевіряє, чи обраний Майстер дійсно надає обрану Послугу.
 * Це запобігає ситуації, коли манікюр бронюють у колориста.
 */
public class MasterServiceCompatibilityHandler extends AbstractBookingValidationHandler {

    // Цьому хендлеру не потрібен репозиторій, 
    // оскільки він працює з об'єктами, які вже знайшли попередні ланки.

    public MasterServiceCompatibilityHandler() {
        // Конструктор
    }

    @Override
    public void handle(BookingValidationContext context) {
        // Перевірка, чи попередні ланки вже знайшли об'єкти
        if (context.getMaster() == null || context.getService() == null) {
            // Це не повинно статися, якщо Chain правильно зібраний
            context.setErrorMessage("Помилка ланцюжка: Об'єкти Майстра або Послуги відсутні.");
            return;
        }

        // КЛЮЧОВА ЛОГІКА: Перевірка сумісності ID
        // Master.masterId (з контексту) має збігатися з Service.master.masterId
        // Ми використовуємо service.getMaster().getMasterId(), щоб отримати ID майстра,
        // який ПРИВ'ЯЗАНИЙ до цієї послуги в базі.
        
        if (context.getService().getMaster().getMasterId().equals(context.getMaster().getMasterId())) {
            // Якщо сумісність є, передаємо далі
            handleNext(context);
        } else {
            // Якщо сумісності немає, зупиняємо ланцюжок
            context.setErrorMessage(
                "Обраний майстер (" + context.getMaster().getUser().getName() + 
                ") не надає послугу '" + context.getService().getName() + "'."
            );
        }
    }
}