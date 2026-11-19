package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;

    private User saveUser(String email) {
        User user = new User();
        user.setName("Пользователь " + email);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item saveItem(User owner, String name, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Описание " + name);
        item.setAvailable(available);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Test
    void createShouldLinkItemToRequest() {
        User owner = saveUser("int-item-owner1@mail.ru");
        User requestor = saveUser("int-item-requestor1@mail.ru");
        ItemRequest request = new ItemRequest();
        request.setDescription("Нужна дрель");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        itemRequestRepository.save(request);

        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);
        dto.setRequestId(request.getId());

        ItemDto created = itemService.create(owner.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void updateShouldChangeNameForOwner() {
        User owner = saveUser("int-item-owner2@mail.ru");
        Item item = saveItem(owner, "Дрель", true);
        ItemDto patch = new ItemDto();
        patch.setName("Перфоратор");

        assertThat(itemService.update(owner.getId(), item.getId(), patch).getName())
                .isEqualTo("Перфоратор");
    }

    @Test
    void searchShouldReturnOnlyAvailableMatchingItems() {
        User owner = saveUser("int-item-owner3@mail.ru");
        Item matching = saveItem(owner, "Дрель аккумуляторная", true);
        saveItem(owner, "Дрель старая", false);

        assertThat(itemService.search("дрель"))
                .extracting(ItemDto::getId)
                .containsExactly(matching.getId());
    }

    @Test
    void getByIdShouldReturnComments() {
        User owner = saveUser("int-item-owner4@mail.ru");
        User author = saveUser("int-item-author4@mail.ru");
        Item item = saveItem(owner, "Дрель", true);
        Comment comment = new Comment();
        comment.setText("Отлично");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        assertThat(itemService.getById(owner.getId(), item.getId()).getComments()).hasSize(1);
    }

    @Test
    void getAllByOwnerShouldReturnOwnerItems() {
        User owner = saveUser("int-item-owner5@mail.ru");
        saveItem(owner, "Дрель", true);
        saveItem(owner, "Молоток", true);

        assertThat(itemService.getAllByOwner(owner.getId())).hasSize(2);
    }

    @Test
    void addCommentShouldSaveCommentAfterFinishedBooking() {
        User owner = saveUser("int-item-owner6@mail.ru");
        User booker = saveUser("int-item-booker6@mail.ru");
        Item item = saveItem(owner, "Дрель", true);
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Всё хорошо");

        assertThat(itemService.addComment(booker.getId(), item.getId(), request).getText())
                .isEqualTo("Всё хорошо");
    }

    @Test
    void createWithoutRequestShouldSaveItem() {
        User owner = saveUser("int-item-owner7@mail.ru");
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);

        assertThat(itemService.create(owner.getId(), dto).getRequestId()).isNull();
    }

    @Test
    void createByUnknownUserShouldThrowNotFound() {
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);

        assertThrows(NotFoundException.class, () -> itemService.create(999L, dto));
    }

    @Test
    void createWithUnknownRequestShouldThrowNotFound() {
        User owner = saveUser("int-item-owner8@mail.ru");
        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);
        dto.setRequestId(999L);

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), dto));
    }

    @Test
    void updateByNotOwnerShouldThrowForbidden() {
        Item item = saveItem(saveUser("int-item-owner9@mail.ru"), "Дрель", true);
        User stranger = saveUser("int-item-stranger9@mail.ru");
        ItemDto patch = new ItemDto();
        patch.setName("Перфоратор");

        assertThrows(ForbiddenException.class,
                () -> itemService.update(stranger.getId(), item.getId(), patch));
    }

    @Test
    void updateUnknownItemShouldThrowNotFound() {
        User owner = saveUser("int-item-owner10@mail.ru");
        ItemDto patch = new ItemDto();
        patch.setName("Перфоратор");

        assertThrows(NotFoundException.class,
                () -> itemService.update(owner.getId(), 999L, patch));
    }

    @Test
    void getAllByOwnerUnknownUserShouldThrowNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.getAllByOwner(999L));
    }

    @Test
    void addCommentWithoutFinishedBookingShouldThrowValidation() {
        Item item = saveItem(saveUser("int-item-owner16@mail.ru"), "Дрель", true);
        User author = saveUser("int-item-author16@mail.ru");
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Всё хорошо");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(author.getId(), item.getId(), request));
    }

    @Test
    void addCommentByUnknownUserShouldThrowNotFound() {
        Item item = saveItem(saveUser("int-item-owner17@mail.ru"), "Дрель", true);
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Всё хорошо");

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(999L, item.getId(), request));
    }

    @Test
    void addCommentForUnknownItemShouldThrowNotFound() {
        User author = saveUser("int-item-author18@mail.ru");
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Всё хорошо");

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(author.getId(), 999L, request));
    }
}
