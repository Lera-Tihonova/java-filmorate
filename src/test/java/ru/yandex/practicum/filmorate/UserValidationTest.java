package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.validation.*;
import java.time.LocalDate;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidateValidUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenLoginHasSpaces() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("bad login");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("justLogin");
        user.setName(null);
        user.setBirthday(LocalDate.now());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        assertEquals("justLogin", user.getName());
    }
}