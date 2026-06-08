package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    private Film validFilm;
    private User validUser;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("Test Description");
        validFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        validFilm.setDuration(120);

        validUser = new User();
        validUser.setEmail("test@test.com");
        validUser.setLogin("testuser");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldCreateFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    void shouldFailCreateFilmWithBlankName() throws Exception {
        validFilm.setName("");
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateFilmWithDescriptionTooLong() throws Exception {
        validFilm.setDescription("a".repeat(201));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateFilmWithNegativeDuration() throws Exception {
        validFilm.setDuration(-10);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllFilms() throws Exception {
        filmService.add(validFilm);
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetFilmById() throws Exception {
        Film film = filmService.add(validFilm);
        mockMvc.perform(get("/films/{id}", film.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(film.getId()));
    }

    @Test
    void shouldFailGetFilmByIdNotFound() throws Exception {
        mockMvc.perform(get("/films/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateFilm() throws Exception {
        Film film = filmService.add(validFilm);
        film.setName("Updated Name");
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void shouldAddLike() throws Exception {
        Film film = filmService.add(validFilm);
        User user = userService.add(validUser);
        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRemoveLike() throws Exception {
        Film film = filmService.add(validFilm);
        User user = userService.add(validUser);
        filmService.addLike(film.getId(), user.getId());
        mockMvc.perform(delete("/films/{id}/like/{userId}", film.getId(), user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPopularFilms() throws Exception {
        Film film1 = filmService.add(validFilm);

        Film film2 = new Film();
        film2.setName("Popular Film");
        film2.setDescription("Very popular");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(90);
        film2 = filmService.add(film2);

        User user = userService.add(validUser);
        filmService.addLike(film2.getId(), user.getId());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(film2.getId()));
    }
}