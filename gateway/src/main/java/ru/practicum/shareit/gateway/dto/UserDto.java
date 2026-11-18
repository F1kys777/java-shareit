package ru.practicum.shareit.gateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
}
