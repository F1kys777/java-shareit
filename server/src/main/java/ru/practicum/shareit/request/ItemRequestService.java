package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getAllOther(Long userId);

    ItemRequestDto getById(Long requestId);

    List<ItemRequestDto> getAllByMySelf(Long userId);
}
