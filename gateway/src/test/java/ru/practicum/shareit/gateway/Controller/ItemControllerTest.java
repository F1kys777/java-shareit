package ru.practicum.shareit.gateway.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.Client.ItemClient;
import ru.practicum.shareit.gateway.dto.ItemDto;
import ru.practicum.shareit.gateway.dto.ItemUpdateDto;
import ru.practicum.shareit.gateway.dto.NewCommentRequest;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;
import java.util.Map;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createShouldReturn201() throws Exception {
        when(itemClient.create(eq(10L), any(ItemDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("id", 1, "requestId", 5)));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Аккумуляторная\"," +
                                "\"available\":true,\"requestId\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(5));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        when(itemClient.update(eq(10L), eq(1L), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Перфоратор\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        when(itemClient.getById(10L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/items/1").header(USER_HEADER, 10L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwnerShouldReturn200() throws Exception {
        when(itemClient.getAllByOwner(10L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/items").header(USER_HEADER, 10L))
                .andExpect(status().isOk());
    }

    @Test
    void searchShouldReturn200() throws Exception {
        when(itemClient.search("дрель")).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentShouldReturn200() throws Exception {
        when(itemClient.addComment(eq(20L), eq(1L), any(NewCommentRequest.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 3)));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отлично\"}"))
                .andExpect(status().isOk());
    }
}
