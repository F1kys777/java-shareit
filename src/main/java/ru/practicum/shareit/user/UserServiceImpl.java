package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        log.info("UserServiceImpl: method create, userDto - {}", userDto);
        userRepository.findByEmail(userDto.getEmail()).ifPresent(u -> {
            throw new ConflictException("Email уже используется: " + userDto.getEmail());
        });

        User user = UserMapper.toUser(userDto);
        user.setId(null);

        userRepository.save(user);
        UserDto userDto1 = UserMapper.toUserDto(user);
        log.info("UserServiceImpl: method create, результат - {}", userDto1);
        return userDto1;
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("UserServiceImpl: method update, userId - {}, userDto - {}", userId, userDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        if (userDto.getEmail() != null) {
            userRepository.findByEmail(userDto.getEmail())
                    .filter(u -> !u.getId().equals(userId))
                    .ifPresent(u -> {
                        throw new ConflictException("Email уже используется: " + userDto.getEmail());
                    });
            log.info("UserServiceImpl: изменение email: было - {}, стало - {}", user.getEmail(), userDto.getEmail());
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            log.info("UserServiceImpl: изменение name: было - {}, стало - {}",
                    user.getName(), userDto.getName());
            user.setName(userDto.getName());
        }

        User updated = userRepository.update(user);
        UserDto userDto1 = UserMapper.toUserDto(updated);
        log.info("UserServiceImpl: method update, результат - {}", userDto1);
        return userDto1;
    }

    @Override
    public UserDto getById(Long userId) {
        log.info("UserServiceImpl: method getById, userId - {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        log.info("UserServiceImpl: method getAll");
        List<UserDto> usersDto1 = userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
        log.info("UserServiceImpl: method getAll, найдено пользователей - {}", usersDto1.size());
        return usersDto1;
    }

    @Override
    public void delete(Long userId) {
        log.info("UserServiceImpl: method delete, userId - {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        userRepository.deleteById(userId);
        log.info("UserServiceImpl: пользователь с id - {} удалён", userId);
    }
}
