package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final InMemoryFilmStorage filmStorage;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film add(Film film) {
        validateFilm(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validateFilm(film);
        return filmStorage.update(film);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Дата релиза {} не может быть раньше 28 декабря 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}