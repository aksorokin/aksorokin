package com.shiftcalc.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Модель смены с поддержкой часовых поясов.
 * Все временные метки хранятся с информацией о часовом поясе (ZonedDateTime).
 */
@Value
@Builder(toBuilder = true)
public class Shift {
    Long id;
    String shiftName;
    String timeZoneId;
    ZonedDateTime startTime;
    ZonedDateTime endTime;
    BigDecimal baseRate;
    Integer employeeCount;

    /**
     * Продолжительность смены в часах
     */
    public long getDurationHours() {
        return Duration.between(startTime, endTime).toHours();
    }

    /**
     * Проверка корректности временных интервалов
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}
