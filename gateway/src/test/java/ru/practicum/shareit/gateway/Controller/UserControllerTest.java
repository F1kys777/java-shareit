package ru.practicum.shareit.gateway.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.Client.UserClient;
import ru.practicum.shareit.gateway.dto.UserDto;
import ru.practicum.shareit.gateway.dto.UserUpdateDto;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;
import java.util.Map;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    @Test
    void createShouldReturn201() throws Exception {
        when(userClient.create(any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", 1)));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Иван\",\"email\":\"ivan@mail.ru\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateShouldReturn200() throws Exception {
        when(userClient.update(eq(1L), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Пётр\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        when(userClient.getById(1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllShouldReturn200() throws Exception {
        when(userClient.getAll()).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deleteShouldReturn204() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }
}
