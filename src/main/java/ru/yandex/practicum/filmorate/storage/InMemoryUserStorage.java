package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;

@Component
public class InMemoryUserStorage {
    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    public Collection<User> getAll() {
        return users.values();
    }

    public User add(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.debug("Добавлен пользователь: {}", user);
        return user;
    }

    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден для обновления", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.debug("Обновлён пользователь: {}", user);
        return user;
    }

    public User getById(Integer id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }
}