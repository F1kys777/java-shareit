package ru.practicum.shareit.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestDto {
    @NotBlank
    private String description;
}
