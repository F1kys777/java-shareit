package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
public class UserRepository {

    private final Map<Long, User> storage = new HashMap<>();

    public User save(User user) {
        log.info("UserRepository: method save, полученный объект User: {}", user);
        if (user.getId() == null) {
            user.setId(getNextId());
        }
        storage.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        log.info("UserRepository: method update, полученный объект User: {}", user);
        storage.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        log.info("UserRepository: method findById, поступивший id - {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    public List<User> findAll() {
        log.info("UserRepository: method findAll");
        return new ArrayList<>(storage.values());
    }

    public void deleteById(Long id) {
        log.info("UserRepository: method deleteById, поступивший id - {}", id);
        storage.remove(id);
    }

    public boolean existsById(Long id) {
        log.info("UserRepository: method existsById, поступивший id - {}", id);
        return storage.containsKey(id);
    }

    public Optional<User> findByEmail(String email) {
        log.info("UserRepository: method findByEmail, поступивший email - {}", email);
        return storage.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    private long getNextId() {
        long currentMaxId = storage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        currentMaxId++;
        log.info("UserRepository: генерация id , присвоен id - {}", currentMaxId);
        return currentMaxId;
    }
}
