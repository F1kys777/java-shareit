package ru.practicum.shareit.gateway.dto;

import java.util.Optional;

public enum BookingState {
    ALL, CURRENT, FUTURE, PAST, REJECTED, WAITING;

    public static Optional<BookingState> from(String state) {
        for (BookingState value : values()) {
            if (value.name().equalsIgnoreCase(state)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}