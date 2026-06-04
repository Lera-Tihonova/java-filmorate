package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> getAll() {
        log.debug("Запрос на получение всех пользователей");
        return userService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User add(@Valid @RequestBody User user) {
        log.debug("Запрос на добавление пользователя: {}", user);
        return userService.add(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.debug("Запрос на обновление пользователя: {}", user);
        if (user.getId() == null) {
            throw new ValidationException("Id пользователя обязателен для обновления");
        }
        return userService.update(user);
    }
}