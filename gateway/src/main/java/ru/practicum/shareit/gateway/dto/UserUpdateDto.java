package ru.practicum.shareit.gateway.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateDto {
    private String name;
    @Email
    private String email;
}
