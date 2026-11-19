package ru.practicum.shareit.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewCommentRequest {
    @NotBlank
    private String text;
}
