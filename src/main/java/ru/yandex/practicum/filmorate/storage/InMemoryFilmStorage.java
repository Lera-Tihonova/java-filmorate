package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.*;

@Component
public class InMemoryFilmStorage {
    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    public Collection<Film> getAll() {
        return films.values();
    }

    public Film add(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.debug("Добавлен фильм: {}", film);
        return film;
    }

    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id {} не найден для обновления", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.debug("Обновлён фильм: {}", film);
        return film;
    }

    public Film getById(Integer id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }
}