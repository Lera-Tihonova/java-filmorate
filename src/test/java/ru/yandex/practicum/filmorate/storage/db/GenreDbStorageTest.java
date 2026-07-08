package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;  // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    @Autowired
    private GenreStorage genreStorage;  // <-- ТЕПЕРЬ ЭТО РАБОТАЕТ

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSize(6);
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> genre = genreStorage.findById(1);

        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(1);
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void shouldReturnEmptyWhenGenreNotFound() {
        Optional<Genre> genre = genreStorage.findById(999);
        assertThat(genre).isEmpty();
    }
}