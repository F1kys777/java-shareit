package ru.practicum.shareit.gateway.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.Client.ItemRequestClient;
import ru.practicum.shareit.gateway.dto.ItemRequestDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) long userId,
                                         @Valid @RequestBody ItemRequestDto dto) {
        return itemRequestClient.create(userId, dto);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOther(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemRequestClient.getAllOther(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@PathVariable long requestId) {
        return itemRequestClient.getById(requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemRequestClient.getOwn(userId);
    }
}
