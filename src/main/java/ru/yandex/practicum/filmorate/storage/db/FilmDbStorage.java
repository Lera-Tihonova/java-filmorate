package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private RowMapper<Film> filmMapper;

    @PostConstruct
    private void init() {
        this.filmMapper = (rs, rowNum) -> {
            Film film = Film.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .duration(rs.getInt("duration"))
                    .likes(new HashSet<>())
                    .genres(new LinkedHashSet<>())
                    .build();

            Integer mpaId = rs.getInt("mpa_id");
            if (mpaId > 0) {
                mpaStorage.findById(mpaId).ifPresent(film::setMpa);
            }

            return film;
        };
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            genreStorage.addFilmGenres(film.getId(), genreIds);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        genreStorage.removeFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toList());
            genreStorage.addFilmGenres(film.getId(), genreIds);
        }

        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        film.setLikes(getLikesById(id));
        film.setGenres(new LinkedHashSet<>(genreStorage.getGenresByFilmId(id)));

        return Optional.of(film);
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmMapper);

        if (films.isEmpty()) {
            return films;
        }

        Map<Long, Set<Long>> likesMap = getLikesForFilms(films);
        Map<Long, Set<Genre>> genresMap = getGenresForFilms(films);

        for (Film film : films) {
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }

        return films;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, COUNT(l.user_id) as likes_count FROM films f " +
                "LEFT JOIN likes l ON l.film_id = f.id " +
                "GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);

        if (films.isEmpty()) {
            return films;
        }

        Map<Long, Set<Long>> likesMap = getLikesForFilms(films);
        Map<Long, Set<Genre>> genresMap = getGenresForFilms(films);

        for (Film film : films) {
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }

        return films;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Set<Long> getLikesById(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class, filmId);
        return new HashSet<>(userIds);
    }

    private Map<Long, Set<Long>> getLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return new HashMap<>();

        String ids = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(", "));

        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN (" + ids + ")";

        Map<Long, Set<Long>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });
        return result;
    }

    private Map<Long, Set<Genre>> getGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return new HashMap<>();

        String ids = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(", "));

        String sql = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + ids + ")";

        Map<Long, Set<Genre>> result = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = Genre.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        });
        return result;
    }
}