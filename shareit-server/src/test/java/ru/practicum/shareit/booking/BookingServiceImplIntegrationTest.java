package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User saveUser(String email) {
        User user = new User();
        user.setName("Пользователь " + email);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item saveItem(User owner) {
        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Аккумуляторная");
        item.setAvailable(true);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking saveBooking(Item item, User booker) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    @Test
    void createShouldSaveWaitingBooking() {
        User owner = saveUser("int-booking-owner1@mail.ru");
        User booker = saveUser("int-booking-booker1@mail.ru");
        Item item = saveItem(owner);
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.create(booker.getId(), request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void approveShouldChangeStatus() {
        User owner = saveUser("int-booking-owner2@mail.ru");
        User booker = saveUser("int-booking-booker2@mail.ru");
        Booking booking = saveBooking(saveItem(owner), booker);

        assertThat(bookingService.approve(owner.getId(), booking.getId(), true).getStatus())
                .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getByIdShouldReturnBooking() {
        User owner = saveUser("int-booking-owner3@mail.ru");
        User booker = saveUser("int-booking-booker3@mail.ru");
        Booking booking = saveBooking(saveItem(owner), booker);

        assertThat(bookingService.getById(booker.getId(), booking.getId()).getId())
                .isEqualTo(booking.getId());
    }

    @Test
    void getAllByBookerShouldReturnBookings() {
        User owner = saveUser("int-booking-owner4@mail.ru");
        User booker = saveUser("int-booking-booker4@mail.ru");
        saveBooking(saveItem(owner), booker);

        assertThat(bookingService.getAllByBooker(booker.getId(), State.ALL)).hasSize(1);
    }

    @Test
    void getAllByOwnerShouldReturnBookings() {
        User owner = saveUser("int-booking-owner5@mail.ru");
        User booker = saveUser("int-booking-booker5@mail.ru");
        saveBooking(saveItem(owner), booker);

        assertThat(bookingService.getAllByOwner(owner.getId(), State.ALL)).hasSize(1);
    }
}
