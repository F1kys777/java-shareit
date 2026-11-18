package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User saveUser(String email) {
        User user = new User();
        user.setName("Пользователь " + email);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemRequest saveRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);
        return itemRequestRepository.save(request);
    }

    @Test
    void createShouldSaveRequest() {
        User requestor = saveUser("int-request-user1@mail.ru");
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель");

        ItemRequestDto created = itemRequestService.create(requestor.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void getByIdShouldReturnRequestWithItems() {
        User requestor = saveUser("int-request-user2@mail.ru");
        ItemRequest request = saveRequest(requestor, "Нужна дрель", LocalDateTime.now());
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        item.setOwner(saveUser("int-request-owner2@mail.ru"));
        item.setRequest(request);
        itemRepository.save(item);

        ItemRequestDto dto = itemRequestService.getById(request.getId());

        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void getAllOtherShouldExcludeOwnRequests() {
        User author = saveUser("int-request-user3@mail.ru");
        User other = saveUser("int-request-user4@mail.ru");
        saveRequest(author, "Запрос автора", LocalDateTime.now());
        saveRequest(other, "Запрос другого", LocalDateTime.now().minusHours(1));

        List<ItemRequestDto> requests = itemRequestService.getAllOther(author.getId());

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Запрос другого");
    }

    @Test
    void getAllByMySelfShouldReturnOwnRequestsSortedByCreatedDesc() {
        User requestor = saveUser("int-request-user5@mail.ru");
        saveRequest(requestor, "Старый запрос", LocalDateTime.now().minusDays(2));
        saveRequest(requestor, "Новый запрос", LocalDateTime.now().minusDays(1));

        List<ItemRequestDto> requests = itemRequestService.getAllByMySelf(requestor.getId());

        assertThat(requests).extracting(ItemRequestDto::getDescription)
                .containsExactly("Новый запрос", "Старый запрос");
    }
}