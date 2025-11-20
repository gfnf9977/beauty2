package com.beautysalon.booking.service;

import com.beautysalon.booking.entity.Master;
import com.beautysalon.booking.entity.Role;
import com.beautysalon.booking.entity.User;
import com.beautysalon.booking.repository.IMasterRepository;
import com.beautysalon.booking.repository.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final IMasterRepository masterRepository;

    public UserService(IUserRepository userRepository, IMasterRepository masterRepository) {
        this.userRepository = userRepository;
        this.masterRepository = masterRepository;
    }

    public User save(User user) {
        user.setRole(Role.CLIENT);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User login(String email, String password) {
        return findByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .orElse(null);
    }

    public Optional<User> findById(String id) {
        try {
            return userRepository.findById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void deleteById(String id) {
        try {
            userRepository.deleteById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User updateUserRole(UUID userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача з ID " + userId + " не знайдено."));

        Role currentRole = user.getRole();
        if (currentRole == newRole) {
             return user;
        }

        if (currentRole == Role.MASTER && newRole != Role.MASTER) {
            masterRepository.findByUserUserId(userId).ifPresent(masterRepository::delete);
        }

        if (newRole == Role.MASTER && currentRole != Role.MASTER) {
            if (masterRepository.findByUserUserId(userId).isEmpty()) {
                Master newMaster =
                    new Master(user, "Призначити спеціалізацію", 0);
                masterRepository.save(newMaster);
            }
        }

        user.setRole(newRole);

        return userRepository.save(user);
    }
}
