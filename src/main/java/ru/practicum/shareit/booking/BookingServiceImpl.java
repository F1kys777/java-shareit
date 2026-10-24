package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public BookingDto create(Long userId, NewBookingRequest request) {
        log.info("BookingServiceImpl: create, userId - {}, request - {}", userId, request);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + request.getItemId() + " не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь с id=" + item.getId() + " недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нельзя бронировать свою вещь");
        }

        if (!request.getEnd().isAfter(request.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        if (request.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }

        Booking booking = new Booking();
        booking.setStart(request.getStart());
        booking.setEnd(request.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.info("BookingServiceImpl: create, создано бронирование id - {}", saved.getId());
        return BookingMapper.toBookingDto(saved);
    }

    @Transactional
    @Override
    public BookingDto approve(Long userId, Long bookingId, boolean approved) {
        log.info("BookingServiceImpl: approve, userId - {}, bookingId - {}, approved - {}",
                userId, bookingId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже имеет статус " + booking.getStatus());
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        log.info("BookingServiceImpl: approve, бронирование id - {} переведено в статус {}",
                saved.getId(), saved.getStatus());
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        log.info("BookingServiceImpl: getById, userId - {}, bookingId - {}", userId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование с id=" + bookingId + " не найдено");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, State state) {
        log.info("BookingServiceImpl: getAllByBooker, userId - {}, state - {}", userId, state);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository
                    .findByBooker_IdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository
                    .findByBooker_IdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingRepository
                    .findByBooker_IdOrderByStartDesc(userId);
        };

        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, State state) {
        log.info("BookingServiceImpl: getAllByOwner, userId - {}, state - {}", userId, state);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(userId, now, now);
            case PAST -> bookingRepository
                    .findByItem_Owner_IdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository
                    .findByItem_Owner_IdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingRepository
                    .findByItem_Owner_IdOrderByStartDesc(userId);
        };

        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }
}
