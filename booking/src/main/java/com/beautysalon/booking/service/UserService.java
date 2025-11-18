package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.Role;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.repository.IUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Зберігає нового користувача
    public User save(User user) {
        user.setRole(Role.CLIENT);
        return userRepository.save(user);
    }

    // Шукає користувача за email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Всіх користувачів
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Логін (тимчасово)
    public User login(String email, String password) {
        return findByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .orElse(null);
    }

    // Додаткові методи
    public Optional<User> findById(String id) {
        try {
            return userRepository.findById(java.util.UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void deleteById(String id) {
        try {
            userRepository.deleteById(java.util.UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    // НОВИЙ МЕТОД: Отримати список користувачів за роллю
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
}
