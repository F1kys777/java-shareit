package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        log.info("ItemRequestServiceImpl: method create, itemRequestDto - {}", itemRequestDto);
        User requestor = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);
        itemRequestRepository.save(request);
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        dto.setItems(List.of());
        log.info("ItemRequestServiceImpl: method create, результат - {}", dto);
        return dto;
    }

    @Override
    public ItemRequestDto getById(Long requestId) {
        log.info("ItemRequestServiceImpl: method getById, requestId - {}", requestId);
        ItemRequest request = itemRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("request с id=" + requestId + " не найден"));
        List<Item> items = itemRepository.findByRequest_IdIn(List.of(requestId));

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        dto.setItems(items.stream()
                .map(ItemMapper::toItemShortDto)
                .toList());
        return dto;
    }

    @Override
    public List<ItemRequestDto> getAllOther(Long userId) {
        log.info("ItemRequestServiceImpl: method getAllOther");
        List<ItemRequest> requestList = itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId);
        if (requestList.isEmpty()) {
            return List.of();
        }

        List<Long> ids = requestList.stream().map(ItemRequest::getId).toList();
        Map<Long, List<ItemShortDto>> byRequestId = groupItems(ids);
        log.info("ItemRequestServiceImpl: method getAllOther, найдено request - {}", requestList.size());
        return requestList.stream()
                .map(r -> {
                    ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(r);
                    dto.setItems(byRequestId.getOrDefault(r.getId(), List.of()));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllByMySelf(Long userId) {
        log.info("ItemRequestServiceImpl: method getAllByMySelf");
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> ids = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<ItemShortDto>> byRequestId = groupItems(ids);

        return requests.stream()
                .map(r -> {
                    ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(r);
                    dto.setItems(byRequestId.getOrDefault(r.getId(), List.of()));
                    return dto;
                })
                .toList();
    }

    private Map<Long, List<ItemShortDto>> groupItems(List<Long> ids) {
        return itemRepository.findByRequest_IdIn(ids).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemMapper::toItemShortDto, Collectors.toList())));
    }
}
