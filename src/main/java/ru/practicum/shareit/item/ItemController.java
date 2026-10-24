package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("ItemController: method create, userId - {}, itemDto - {}", userId, itemDto);
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("ItemController: method update, userId - {}, itemId - {}, itemDto - {}", userId, itemId, itemDto);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        log.info("ItemController: method getById, itemId - {}", itemId);
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("ItemController: method getAllByOwner, userId - {}", userId);
        return itemService.getAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("ItemController: method search, text - {}", text);
        return itemService.search(text);
    }
}
