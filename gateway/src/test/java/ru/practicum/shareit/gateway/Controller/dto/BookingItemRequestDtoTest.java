package ru.practicum.shareit.gateway.Controller.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.gateway.dto.BookingItemRequestDto;
import static org.assertj.core.api.Assertions.*;



import java.time.LocalDateTime;
import java.util.Set;

@JsonTest
public class BookingItemRequestDtoTest {

    @Autowired
    private JacksonTester<BookingItemRequestDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldReadAndWriteIsoDates() throws Exception {
        BookingItemRequestDto dto = json.parseObject(
                "{\"itemId\":2,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T11:30:00\"}");

        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 10, 0));
        assertThat(json.write(dto).getJson()).contains("\"end\":\"2030-01-02T11:30:00\"");
    }

    @Test
    void shouldRejectNullFields() {
        Set<ConstraintViolation<BookingItemRequestDto>> violations =
                validator.validate(new BookingItemRequestDto());

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("itemId", "start", "end");
    }
}
