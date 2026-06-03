package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final InMemoryUserStorage userStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User add(User user) {
        validateUser(user);
        return userStorage.add(user);
    }

    public User update(User user) {
        validateUser(user);
        return userStorage.update(user);
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, используем логин: {}", user.getLogin());
        }
    }
}