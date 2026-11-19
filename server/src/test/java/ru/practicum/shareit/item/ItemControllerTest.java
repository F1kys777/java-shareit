package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.NewCommentRequest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto() {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Дрель");
        dto.setDescription("Аккумуляторная");
        dto.setAvailable(true);
        dto.setRequestId(5L);
        return dto;
    }

    @Test
    void createShouldReturn201() throws Exception {
        when(itemService.create(eq(10L), any(ItemDto.class))).thenReturn(itemDto());

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
        when(itemService.update(eq(10L), eq(1L), any(ItemDto.class))).thenReturn(itemDto());

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Перфоратор\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(1L);
        dto.setComments(List.of());
        when(itemService.getById(10L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/items/1").header(USER_HEADER, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByOwnerShouldReturn200() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(1L);
        when(itemService.getAllByOwner(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/items").header(USER_HEADER, 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void searchShouldReturn200() throws Exception {
        when(itemService.search("дрель")).thenReturn(List.of(itemDto()));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addCommentShouldReturn200() throws Exception {
        CommentDto comment = new CommentDto();
        comment.setId(3L);
        comment.setText("Отлично");
        comment.setCreated(LocalDateTime.now());
        when(itemService.addComment(eq(20L), eq(1L), any(NewCommentRequest.class))).thenReturn(comment);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отлично\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отлично"));
    }

    @Test
    void getByIdUnknownShouldReturn404() throws Exception {
        when(itemService.getById(10L, 99L)).thenThrow(new NotFoundException("Вещь с id=99 не найдена"));

        mockMvc.perform(get("/items/99").header(USER_HEADER, 10L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateByNotOwnerShouldReturn403() throws Exception {
        when(itemService.update(eq(10L), eq(1L), any(ItemDto.class)))
                .thenThrow(new ForbiddenException("Редактировать вещь может только владелец"));

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Перфоратор\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addCommentWithoutBookingShouldReturn400() throws Exception {
        when(itemService.addComment(eq(20L), eq(1L), any(NewCommentRequest.class)))
                .thenThrow(new ValidationException("Оставить отзыв нельзя"));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отлично\"}"))
                .andExpect(status().isBadRequest());
    }
}
