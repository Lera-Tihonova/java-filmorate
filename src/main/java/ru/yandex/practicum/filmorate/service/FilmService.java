package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film add(Film film) {
        validateFilmForCreate(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validateFilmForUpdate(film);
        return filmStorage.update(film);
    }

    private void validateFilmForCreate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Дата релиза {} не может быть раньше 28 декабря 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        // name, description, duration проверяются аннотациями @Valid
    }

    private void validateFilmForUpdate(Film film) {
        // id обязателен
        if (film.getId() == null) {
            throw new ValidationException("Id фильма обязателен для обновления");
        }
        // Остальные поля валидируем только если они не null
        if (film.getName() != null && film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}