package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaStorage mpaStorage;

    @Test
    void shouldFindAllMpa() {
        List<Mpa> mpas = mpaStorage.findAll();

        assertThat(mpas).isNotEmpty();
        assertThat(mpas).hasSize(5); // 5 рейтингов из data.sql
    }

    @Test
    void shouldFindMpaById() {
        Optional<Mpa> mpa = mpaStorage.findById(1);

        assertThat(mpa).isPresent();
        assertThat(mpa.get().getId()).isEqualTo(1);
        assertThat(mpa.get().getName()).isEqualTo("G");
    }

    @Test
    void shouldReturnEmptyWhenMpaNotFound() {
        Optional<Mpa> mpa = mpaStorage.findById(999);
        assertThat(mpa).isEmpty();
    }
}