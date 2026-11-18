package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

@JsonTest
public class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldWriteDatesAsIsoStrings() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2030, 5, 1, 9, 15));
        dto.setEnd(LocalDateTime.of(2030, 5, 2, 18, 45));
        dto.setStatus(BookingStatus.WAITING);

        assertThat(json.write(dto).getJson())
                .contains("\"start\":\"2030-05-01T09:15:00\"")
                .contains("\"status\":\"WAITING\"");
    }

    @Test
    void shouldReadDatesAndStatus() throws Exception {
        BookingDto dto = json.parseObject("{\"id\":1,\"start\":\"2030-05-01T09:15:00\"," +
                "\"end\":\"2030-05-02T18:45:00\",\"status\":\"APPROVED\"}");

        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 5, 1, 9, 15));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }
}
