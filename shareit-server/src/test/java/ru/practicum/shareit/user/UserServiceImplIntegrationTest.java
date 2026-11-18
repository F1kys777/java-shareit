package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto newUser(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    @Test
    void createShouldSaveUser() {
        UserDto created = userService.create(newUser("Иван", "int-user1@mail.ru"));

        assertThat(created.getId()).isNotNull();
        assertThat(userRepository.findById(created.getId()).orElseThrow().getEmail())
                .isEqualTo("int-user1@mail.ru");
    }

    @Test
    void updateShouldChangeName() {
        UserDto created = userService.create(newUser("Иван", "int-user2@mail.ru"));
        UserDto patch = new UserDto();
        patch.setName("Пётр");

        assertThat(userService.update(created.getId(), patch).getName()).isEqualTo("Пётр");
    }

    @Test
    void getByIdShouldReturnUser() {
        UserDto created = userService.create(newUser("Иван", "int-user3@mail.ru"));

        assertThat(userService.getById(created.getId()).getEmail()).isEqualTo("int-user3@mail.ru");
    }

    @Test
    void getAllShouldReturnCreatedUsers() {
        userService.create(newUser("Иван", "int-user4@mail.ru"));
        userService.create(newUser("Пётр", "int-user5@mail.ru"));

        assertThat(userService.getAll())
                .extracting(UserDto::getEmail)
                .contains("int-user4@mail.ru", "int-user5@mail.ru");
    }

    @Test
    void deleteShouldRemoveUser() {
        UserDto created = userService.create(newUser("Иван", "int-user6@mail.ru"));

        userService.delete(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void createWithDuplicateEmailShouldThrowConflict() {
        userService.create(newUser("Иван", "int-user7@mail.ru"));

        assertThrows(ConflictException.class,
                () -> userService.create(newUser("Пётр", "int-user7@mail.ru")));
    }

    @Test
    void updateWithEmailOfAnotherUserShouldThrowConflict() {
        userService.create(newUser("Иван", "int-user8@mail.ru"));
        UserDto second = userService.create(newUser("Пётр", "int-user9@mail.ru"));
        UserDto patch = new UserDto();
        patch.setEmail("int-user8@mail.ru");

        assertThrows(ConflictException.class, () -> userService.update(second.getId(), patch));
    }

    @Test
    void updateUnknownUserShouldThrowNotFound() {
        UserDto patch = new UserDto();
        patch.setName("Пётр");

        assertThrows(NotFoundException.class, () -> userService.update(999L, patch));
    }

    @Test
    void getByIdUnknownUserShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void deleteUnknownUserShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> userService.delete(999L));
    }
}
