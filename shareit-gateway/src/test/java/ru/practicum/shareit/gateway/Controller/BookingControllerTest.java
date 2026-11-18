package ru.practicum.shareit.gateway.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.Client.BookingClient;
import ru.practicum.shareit.gateway.dto.BookingItemRequestDto;
import ru.practicum.shareit.gateway.dto.BookingState;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createShouldReturn200() throws Exception {
        when(bookingClient.create(eq(20L), any(BookingItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":2,\"start\":\"2030-01-01T10:00:00\"," +
                                "\"end\":\"2030-01-02T10:00:00\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void approveShouldReturn200() throws Exception {
        when(bookingClient.approve(10L, 1L, true)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 10L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        when(bookingClient.getById(20L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/bookings/1").header(USER_HEADER, 20L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByBookerShouldReturn200() throws Exception {
        when(bookingClient.getAllByBooker(20L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/bookings").header(USER_HEADER, 20L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwnerShouldReturn200() throws Exception {
        when(bookingClient.getAllByOwner(10L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 10L))
                .andExpect(status().isOk());
    }
}
