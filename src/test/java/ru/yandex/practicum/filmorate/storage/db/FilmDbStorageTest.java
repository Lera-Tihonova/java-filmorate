package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        Mpa mpa = Mpa.builder().id(1).name("G").build();

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(Genre.builder().id(1).name("Комедия").build());

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(genres)
                .build();
    }

    @Test
    void shouldCreateFilm() {
        Film created = filmStorage.create(testFilm);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(testFilm.getName());
        assertThat(created.getMpa()).isNotNull();
        assertThat(created.getMpa().getId()).isEqualTo(1);
        assertThat(created.getGenres()).isNotEmpty();
    }

    @Test
    void shouldUpdateFilm() {
        Film created = filmStorage.create(testFilm);
        created.setName("Updated Film");

        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated Film");
    }

    @Test
    void shouldFindFilmById() {
        Film created = filmStorage.create(testFilm);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getName()).isEqualTo(created.getName());
    }

    @Test
    void shouldReturnEmptyWhenFilmNotFound() {
        Optional<Film> found = filmStorage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(testFilm);
        Film secondFilm = Film.builder()
                .name("Second Film")
                .description("Second Description")
                .releaseDate(LocalDate.of(2010, 2, 2))
                .duration(90)
                .mpa(Mpa.builder().id(2).name("PG").build())
                .build();
        filmStorage.create(secondFilm);

        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldDeleteFilm() {
        Film created = filmStorage.create(testFilm);

        filmStorage.delete(created.getId());

        Optional<Film> found = filmStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldAddLike() {
        Film film = filmStorage.create(testFilm);
        User user = userStorage.create(User.builder()
                .email("user@example.com")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        filmStorage.addLike(film.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(film.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).hasSize(1);
    }

    @Test
    void shouldRemoveLike() {
        Film film = filmStorage.create(testFilm);
        User user = userStorage.create(User.builder()
                .email("user@example.com")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(film.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).isEmpty();
    }

    @Test
    void shouldGetPopularFilms() {
        Film film1 = filmStorage.create(testFilm);
        Film film2 = filmStorage.create(Film.builder()
                .name("Second Film")
                .description("Second Description")
                .releaseDate(LocalDate.of(2010, 2, 2))
                .duration(90)
                .mpa(Mpa.builder().id(2).name("PG").build())
                .build());

        User user = userStorage.create(User.builder()
                .email("user@example.com")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        filmStorage.addLike(film1.getId(), user.getId());

        List<Film> popular = filmStorage.getPopularFilms(5);

        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
    }
}