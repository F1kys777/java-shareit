package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto requestDto() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Нужна дрель");
        dto.setCreated(LocalDateTime.now());
        dto.setItems(List.of());
        return dto;
    }

    @Test
    void createShouldReturn201() throws Exception {
        when(itemRequestService.create(eq(20L), any(ItemRequestDto.class))).thenReturn(requestDto());

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Нужна дрель\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllOtherShouldReturn200() throws Exception {
        when(itemRequestService.getAllOther(20L)).thenReturn(List.of(requestDto()));

        mockMvc.perform(get("/requests/all").header(USER_HEADER, 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        when(itemRequestService.getById(1L)).thenReturn(requestDto());

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void getOwnShouldReturn200() throws Exception {
        when(itemRequestService.getAllByMySelf(20L)).thenReturn(List.of(requestDto()));

        mockMvc.perform(get("/requests").header(USER_HEADER, 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
