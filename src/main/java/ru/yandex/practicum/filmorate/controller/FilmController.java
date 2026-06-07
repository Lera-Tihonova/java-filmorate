package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getAll() {
        log.debug("Запрос на получение всех фильмов");
        return filmService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film add(@Valid @RequestBody Film film) {
        log.debug("Запрос на добавление фильма: {}", film);
        return filmService.add(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.debug("Запрос на обновление фильма: {}", film);
        if (film.getId() == null) {
            throw new ValidationException("Id фильма обязателен для обновления");
        }
        return filmService.update(film);
    }
}