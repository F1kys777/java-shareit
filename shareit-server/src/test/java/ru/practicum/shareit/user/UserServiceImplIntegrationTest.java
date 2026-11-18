package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

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
}
