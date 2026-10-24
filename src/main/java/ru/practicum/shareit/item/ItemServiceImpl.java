package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("ItemServiceImpl: method create, userId - {}, itemDto - {}", userId, itemDto);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = ItemMapper.toItem(itemDto, owner);
        log.info("ItemServiceImpl: method create, в реузльтате был получен объект Item:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - {}",
                item.getId(), item.getName(), item.getDescription(), item.getAvailable(), owner.getId());
        ItemDto itemDtoResponse = ItemMapper.toItemDto(itemRepository.save(item));
        log.info("ItemServiceImpl: method create, и преобразован в объект ItemDto:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - " +
                "{}, requestID - {}", itemDtoResponse.getId(), itemDtoResponse.getName(), itemDtoResponse.getDescription(), itemDtoResponse.getAvailable(), itemDtoResponse.getOwnerId(), itemDtoResponse.getRequestId());
        return itemDtoResponse;
    }

    @Transactional
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
        ItemDto itemDtoResponse = ItemMapper.toItemDto(itemRepository.save(item));
        log.info("ItemServiceImpl: method update, и преобразован в объект ItemDto:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - " +
                "{}", itemDtoResponse.getId(), itemDtoResponse.getName(), itemDtoResponse.getDescription(), itemDtoResponse.getAvailable(), itemDtoResponse.getOwnerId());
        return itemDtoResponse;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemWithBookingsDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        ItemWithBookingsDto dto = toDto(item);
        dto.setComments(commentRepository.findByItem_IdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toCommentDto).toList());

        if (item.getOwner().getId().equals(userId)) {
            fillBookings(dto, itemId);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemWithBookingsDto> getAllByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return itemRepository.findByOwner_Id(userId).stream()
                .map(item -> {
                    ItemWithBookingsDto dto = toDto(item);
                    dto.setComments(commentRepository.findByItem_IdOrderByCreatedDesc(item.getId()).stream()
                            .map(CommentMapper::toCommentDto).toList());
                    fillBookings(dto, item.getId());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("ItemServiceImpl: method search, text - {}",text);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private ItemWithBookingsDto toDto(Item item) {
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setOwnerId(item.getOwner().getId());
        dto.setRequestId(item.getRequest() != null ? item.getRequest().getId() : null);
        return dto;
    }

    private void fillBookings(ItemWithBookingsDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        bookingRepository
                .findByItem_IdAndStatusAndStartBeforeOrderByStartDesc(itemId, BookingStatus.APPROVED, now)
                .stream().findFirst()
                .ifPresent(b -> dto.setLastBooking(new BookingShortDto(b.getId(), b.getBooker().getId())));
        bookingRepository
                .findByItem_IdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                .stream().findFirst()
                .ifPresent(b -> dto.setNextBooking(new BookingShortDto(b.getId(), b.getBooker().getId())));
    }

    @Transactional
    @Override
    public CommentDto addComment(Long userId, Long itemId, NewCommentRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        boolean wasBooker = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!wasBooker) {
            throw new ValidationException("Оставить отзыв может только тот, кто брал вещь в аренду после окончания срока");
        }

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}
