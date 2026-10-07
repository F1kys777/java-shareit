package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class ItemRepository {

    private final Map<Long, Item> storage = new HashMap<>();

    public Item save(Item item) {
        log.info("ItemRepository: method save, полученный объект Item:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - " +
                "{}, request - {}", item.getId(), item.getName(), item.getDescription(), item.getAvailable(), item.getOwner(), item.getRequest());
        if (item.getId() == null) {
            item.setId(getNextId());
        }
        storage.put(item.getId(), item);
        return item;
    }

    public Item update(Item item) {
        log.info("ItemRepository: method update, полученный объект Item:" +
                " id - {}, name - {}, description - {}, available status - {}, ownerID - " +
                "{}, request - {}", item.getId(), item.getName(), item.getDescription(), item.getAvailable(), item.getOwner(), item.getRequest());
        storage.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        log.info("ItemRepository: method findById, поступивший id - {}",id);
        return Optional.ofNullable(storage.get(id));
    }

    public List<Item> findAllByOwnerId(Long ownerId) {
        log.info("ItemRepository: method findAllByOwnerId, ownerId - {}",ownerId);
        return storage.values().stream()
                .filter(i -> i.getOwner() != null && i.getOwner().getId().equals(ownerId))
                .toList();
    }

    public List<Item> searchAvailableByText(String text) {
        log.info("ItemRepository: method searchAvailableByText, text - {}",text);
        String lower = text.toLowerCase();
        return storage.values().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i -> i.getName().toLowerCase().contains(lower)
                        || i.getDescription().toLowerCase().contains(lower))
                .toList();
    }

    private long getNextId() {
        long currentMaxId = storage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        currentMaxId++;
        log.info("ItemRepository: генерация id , присвоен id - {}", currentMaxId);
        return currentMaxId;
    }
}
