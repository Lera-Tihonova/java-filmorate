package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class})
class UserDbStorageTest {

    @Autowired
    private UserStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldCreateUser() {
        User created = userStorage.create(testUser);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(created.getLogin()).isEqualTo(testUser.getLogin());
    }

    @Test
    void shouldUpdateUser() {
        User created = userStorage.create(testUser);
        created.setName("Updated Name");

        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldFindUserById() {
        User created = userStorage.create(testUser);

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getEmail()).isEqualTo(created.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> found = userStorage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.create(testUser);
        User secondUser = User.builder()
                .email("test2@example.com")
                .login("testuser2")
                .name("Test User 2")
                .birthday(LocalDate.of(1995, 2, 2))
                .build();
        userStorage.create(secondUser);

        List<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldDeleteUser() {
        User created = userStorage.create(testUser);

        userStorage.delete(created.getId());

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldAddFriend() {
        User user1 = userStorage.create(testUser);
        User user2 = userStorage.create(User.builder()
                .email("friend@example.com")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        userStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = userStorage.create(testUser);
        User user2 = userStorage.create(User.builder()
                .email("friend@example.com")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userStorage.create(testUser);
        User user2 = userStorage.create(User.builder()
                .email("friend@example.com")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
        User commonFriend = userStorage.create(User.builder()
                .email("common@example.com")
                .login("common")
                .name("Common Friend")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(commonFriend.getId());
    }
}