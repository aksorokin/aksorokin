package com.shiftcalc.dto;

import lombok.Builder;
import lombok.Value;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Value
@Builder
public class ShiftDto {
    Long id;

    @NotBlank(message = "Название смены не может быть пустым")
    String shiftName;

    @NotBlank(message = "Часовой пояс не может быть пустым")
    String timeZoneId;

    @NotNull(message = "Время начала не может быть null")
    ZonedDateTime startTime;

    @NotNull(message = "Время окончания не может быть null")
    ZonedDateTime endTime;

    @NotNull(message = "Базовая ставка не может быть null")
    @Positive(message = "Базовая ставка должна быть положительной")
    BigDecimal baseRate;

    @NotNull(message = "Количество сотрудников не может быть null")
    @Min(value = 1, message = "Количество сотрудников должно быть не менее 1")
    Integer employeeCount;
}
