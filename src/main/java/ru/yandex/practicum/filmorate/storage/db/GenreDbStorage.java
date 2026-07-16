package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genre> genreMapper = (rs, rowNum) -> Genre.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .build();

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreMapper);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreMapper, id);
        return genres.isEmpty() ? Optional.empty() : Optional.of(genres.get(0));
    }

    @Override
    public List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? ORDER BY g.id";
        return jdbcTemplate.query(sql, genreMapper, filmId);
    }

    @Override
    public void addFilmGenres(Long filmId, List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        String inClause = genreIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        String checkSql = "SELECT COUNT(*) FROM genres WHERE id IN (" + inClause + ")";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);

        if (count == null || count != genreIds.size()) {
            throw new IllegalArgumentException("Один или несколько жанров не найдены");
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, genreIds, genreIds.size(), (ps, genreId) -> {
            ps.setLong(1, filmId);
            ps.setInt(2, genreId);
        });
    }

    @Override
    public void removeFilmGenres(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}