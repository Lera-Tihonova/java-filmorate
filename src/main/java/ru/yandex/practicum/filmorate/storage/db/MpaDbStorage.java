package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mpa> mpaMapper = (rs, rowNum) -> Mpa.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .build();

    @Override
    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, mpaMapper);
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        List<Mpa> mpas = jdbcTemplate.query(sql, mpaMapper, id);
        return mpas.isEmpty() ? Optional.empty() : Optional.of(mpas.get(0));
    }
}