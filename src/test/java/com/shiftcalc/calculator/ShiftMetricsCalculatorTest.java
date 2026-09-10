package com.shiftcalc.calculator;

import com.shiftcalc.model.Shift;
import com.shiftcalc.model.ShiftCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для калькулятора показателей смен.
 */
class ShiftMetricsCalculatorTest {

    private ShiftMetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShiftMetricsCalculator();
    }

    @Test
    void testCalculateBasicMetrics() {
        // Смена: 8 часов, 10 сотрудников, ставка 100 руб/час
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 9, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 15, 17, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        
        Shift shift = new Shift(1L, "Test Shift", "Europe/Moscow", start, end, 
                new BigDecimal("100.00"), 10);

        ShiftCalculationResult result = calculator.calculate(shift);

        assertEquals(8L, result.getDurationHours());
        assertEquals(new BigDecimal("80.00"), result.getTotalEmployeeHours());
        assertEquals(new BigDecimal("8000.00"), result.getTotalLaborCost());
        assertEquals(new BigDecimal("100.00"), result.getAverageHourlyRate());
        assertEquals(10, result.getEmployeeCount());
    }

    @Test
    void testCalculateWithNightHours() {
        // Ночная смена: 22:00 - 06:00 (8 часов, все ночные)
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 22, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 16, 6, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        
        Shift shift = new Shift(2L, "Night Shift", "Europe/Moscow", start, end,
                new BigDecimal("100.00"), 5);

        BigDecimal nightMultiplier = new BigDecimal("1.5");
        ShiftCalculationResult result = calculator.calculateWithNightHours(shift, nightMultiplier);

        // 8 ночных часов * 1.5 * 100 * 5 сотрудников = 6000
        assertEquals(new BigDecimal("6000.00"), result.getTotalLaborCost());
    }

    @Test
    void testCalculateMixedDayNightShift() {
        // Смешанная смена: 14:00 - 23:00 (9 часов, 1 ночной час - 22:00)
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 14, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 15, 23, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        
        Shift shift = new Shift(3L, "Evening Shift", "Europe/Moscow", start, end,
                new BigDecimal("100.00"), 1);

        BigDecimal nightMultiplier = new BigDecimal("1.5");
        ShiftCalculationResult result = calculator.calculateWithNightHours(shift, nightMultiplier);

        // 8 дневных часов + 1 ночной * 1.5 = 8 + 1.5 = 9.5 * 100 = 950
        assertEquals(new BigDecimal("950.00"), result.getTotalLaborCost());
    }

    @Test
    void testConvertToTimeZone() {
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 15, 20, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        
        Shift shift = new Shift(4L, "Moscow Shift", "Europe/Moscow", start, end,
                new BigDecimal("100.00"), 1);

        Shift utcShift = calculator.convertToTimeZone(shift, "UTC");

        assertEquals("UTC", utcShift.getTimeZoneId());
        // Москва UTC+3, значит 12:00 Moscow = 09:00 UTC
        assertEquals(9, utcShift.getStartTime().getHour());
        assertEquals(17, utcShift.getEndTime().getHour());
    }

    @Test
    void testDifferentTimeZones() {
        // Одинаковое абсолютное время в разных часовых поясах
        ZonedDateTime moscowStart = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime nyStart = moscowStart.withZoneSameInstant(ZoneId.of("America/New_York"));
        
        Shift moscowShift = new Shift(5L, "Moscow", "Europe/Moscow", moscowStart,
                moscowStart.plusHours(8), new BigDecimal("100.00"), 1);
        
        Shift nyShift = new Shift(6L, "NewYork", "America/New_York", nyStart,
                nyStart.plusHours(8), new BigDecimal("100.00"), 1);

        ShiftCalculationResult moscowResult = calculator.calculate(moscowShift);
        ShiftCalculationResult nyResult = calculator.calculate(nyShift);

        // Продолжительность должна быть одинаковой
        assertEquals(moscowResult.getDurationHours(), nyResult.getDurationHours());
        // Стоимость должна быть одинаковой (одинаковая длительность и ставка)
        assertEquals(moscowResult.getTotalLaborCost(), nyResult.getTotalLaborCost());
    }
}
