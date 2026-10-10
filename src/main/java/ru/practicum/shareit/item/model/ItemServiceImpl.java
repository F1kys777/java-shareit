package ru.practicum.shareit.item.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("ItemServiceImpl: method create, userId - {}, itemDto - {}", userId, itemDto);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = ItemMapper.toItem(itemDto, owner);
        log.info("ItemServiceImpl: method create, в реузльтате был получен объект Item:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - " +
                "{}, request - {}", item.getId(), item.getName(), item.getDescription(), item.getAvailable(), owner.getId(), item.getRequest());
        ItemDto itemDtoResponse = ItemMapper.toItemDto(itemRepository.save(item));
        log.info("ItemServiceImpl: method create, и преобразован в объект ItemDto:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - " +
                "{}, requestID - {}", itemDtoResponse.getId(), itemDtoResponse.getName(), itemDtoResponse.getDescription(), itemDtoResponse.getAvailable(), itemDtoResponse.getOwnerId(), itemDtoResponse.getRequestId());
        return itemDtoResponse;
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("ItemServiceImpl: method update, userId - {}, itemId - {}, itemDto - {}", userId, itemId, itemDto);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
            log.info("ItemServiceImpl: изменение itemId - {}, изменено name:\n было - {}, стало - {}",itemId, itemDto.getName(), item.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
            log.info("ItemServiceImpl: изменение itemId - {}, изменено description:\n было - {}, стало - {}",itemId, itemDto.getDescription(), item.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
            log.info("ItemServiceImpl: изменение itemId - {}, изменено статус Available:\n было - {}, стало - {}",itemId, itemDto.getAvailable(), item.getAvailable());
        }
        ItemDto itemDtoResponse = ItemMapper.toItemDto(itemRepository.update(item));
        log.info("ItemServiceImpl: method update, и преобразован в объект ItemDto:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - " +
                "{}, requestID - {}", itemDtoResponse.getId(), itemDtoResponse.getName(), itemDtoResponse.getDescription(), itemDtoResponse.getAvailable(), itemDtoResponse.getOwnerId(), itemDtoResponse.getRequestId());
        return itemDtoResponse;
    }

    @Override
    public ItemDto getById(Long itemId) {
        log.info("ItemServiceImpl: method getById, itemId - {}",itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        log.info("ItemServiceImpl: method getAllByOwner, userId - {}",userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("ItemServiceImpl: method search, text - {}",text);
        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
