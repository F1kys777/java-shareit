package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("UserController: method create, userDto - {}", userDto);
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @RequestBody UserDto userDto) {
        log.info("UserController: method update, userId - {}, userDto - {}", userId, userDto);
        return userService.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        log.info("UserController: method getById, userId - {}", userId);
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("UserController: method getAll");
        return userService.getAll();
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("UserController: method delete, userId - {}", userId);
        userService.delete(userId);
    }
}
