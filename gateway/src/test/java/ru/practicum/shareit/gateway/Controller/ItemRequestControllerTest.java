package ru.practicum.shareit.gateway.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.Client.ItemRequestClient;
import ru.practicum.shareit.gateway.dto.ItemRequestDto;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;
import java.util.Map;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createShouldReturn201() throws Exception {
        when(itemRequestClient.create(eq(20L), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1)));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Нужна дрель\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllOtherShouldReturn200() throws Exception {
        when(itemRequestClient.getAllOther(20L))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/requests/all").header(USER_HEADER, 20L))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        when(itemRequestClient.getById(1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnShouldReturn200() throws Exception {
        when(itemRequestClient.getOwn(20L))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/requests").header(USER_HEADER, 20L))
                .andExpect(status().isOk());
    }
}
