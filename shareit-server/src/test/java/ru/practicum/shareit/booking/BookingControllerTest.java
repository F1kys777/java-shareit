package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto() {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setStatus(BookingStatus.WAITING);
        return dto;
    }

    @Test
    void createShouldReturn200() throws Exception {
        when(bookingService.create(eq(20L), any())).thenReturn(bookingDto());

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":2,\"start\":\"2030-01-01T10:00:00\"," +
                                "\"end\":\"2030-01-02T10:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveShouldReturn200() throws Exception {
        when(bookingService.approve(10L, 1L, true)).thenReturn(bookingDto());

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 10L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        when(bookingService.getById(20L, 1L)).thenReturn(bookingDto());

        mockMvc.perform(get("/bookings/1").header(USER_HEADER, 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByBookerShouldReturn200() throws Exception {
        when(bookingService.getAllByBooker(20L, State.ALL)).thenReturn(List.of(bookingDto()));

        mockMvc.perform(get("/bookings").header(USER_HEADER, 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllByOwnerShouldReturn200() throws Exception {
        when(bookingService.getAllByOwner(10L, State.ALL)).thenReturn(List.of(bookingDto()));

        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}