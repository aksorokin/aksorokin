package com.shiftcalc.calculator;

import com.shiftcalc.model.Shift;
import com.shiftcalc.model.ShiftCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты калькулятора показателей смен")
class ShiftMetricsCalculatorTest {

    private ShiftMetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShiftMetricsCalculator();
    }

    @Test
    @DisplayName("Расчёт базовых показателей смены")
    void testCalculateBasicMetrics() {
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 9, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 15, 17, 0, 0, 0, ZoneId.of("Europe/Moscow"));

        Shift shift = Shift.builder()
                .id(1L)
                .shiftName("Test Shift")
                .timeZoneId("Europe/Moscow")
                .startTime(start)
                .endTime(end)
                .baseRate(new BigDecimal("100.00"))
                .employeeCount(10)
                .build();

        ShiftCalculationResult result = calculator.calculate(shift);

        assertEquals(8L, result.getDurationHours());
        assertEquals(new BigDecimal("80.00"), result.getTotalEmployeeHours());
        assertEquals(new BigDecimal("8000.00"), result.getTotalLaborCost());
        assertEquals(new BigDecimal("100.00"), result.getAverageHourlyRate());
        assertEquals(10, result.getEmployeeCount());
        assertEquals(0, result.getNightHours());
        assertEquals(8, result.getDayHours());
    }

    @Test
    @DisplayName("Расчёт с ночными часами - полностью ночная смена")
    void testCalculateWithNightHours() {
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 22, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 16, 6, 0, 0, 0, ZoneId.of("Europe/Moscow"));

        Shift shift = Shift.builder()
                .id(2L)
                .shiftName("Night Shift")
                .timeZoneId("Europe/Moscow")
                .startTime(start)
                .endTime(end)
                .baseRate(new BigDecimal("100.00"))
                .employeeCount(5)
                .build();

        BigDecimal nightMultiplier = new BigDecimal("1.5");
        ShiftCalculationResult result = calculator.calculateWithNightHours(shift, nightMultiplier);

        assertEquals(new BigDecimal("6000.00"), result.getTotalLaborCost());
        assertEquals(8, result.getNightHours());
    }

    @Test
    @DisplayName("Расчёт смешанной дневной/ночной смены")
    void testCalculateMixedDayNightShift() {
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 14, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 15, 23, 0, 0, 0, ZoneId.of("Europe/Moscow"));

        Shift shift = Shift.builder()
                .id(3L)
                .shiftName("Evening Shift")
                .timeZoneId("Europe/Moscow")
                .startTime(start)
                .endTime(end)
                .baseRate(new BigDecimal("100.00"))
                .employeeCount(1)
                .build();

        BigDecimal nightMultiplier = new BigDecimal("1.5");
        ShiftCalculationResult result = calculator.calculateWithNightHours(shift, nightMultiplier);

        assertEquals(new BigDecimal("950.00"), result.getTotalLaborCost());
        assertEquals(1, result.getNightHours());
        assertEquals(8, result.getDayHours());
    }

    @Test
    @DisplayName("Конвертация в другой часовой пояс")
    void testConvertToTimeZone() {
        ZonedDateTime start = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(2024, 1, 15, 20, 0, 0, 0, ZoneId.of("Europe/Moscow"));

        Shift shift = Shift.builder()
                .id(4L)
                .shiftName("Moscow Shift")
                .timeZoneId("Europe/Moscow")
                .startTime(start)
                .endTime(end)
                .baseRate(new BigDecimal("100.00"))
                .employeeCount(1)
                .build();

        Shift utcShift = calculator.convertToTimeZone(shift, "UTC");

        assertEquals("UTC", utcShift.getTimeZoneId());
        assertEquals(9, utcShift.getStartTime().getHour());
        assertEquals(17, utcShift.getEndTime().getHour());
    }

    @Test
    @DisplayName("Одинаковая длительность в разных часовых поясах")
    void testDifferentTimeZones() {
        ZonedDateTime moscowStart = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime nyStart = moscowStart.withZoneSameInstant(ZoneId.of("America/New_York"));

        Shift moscowShift = Shift.builder()
                .id(5L)
                .shiftName("Moscow")
                .timeZoneId("Europe/Moscow")
                .startTime(moscowStart)
                .endTime(moscowStart.plusHours(8))
                .baseRate(new BigDecimal("100.00"))
                .employeeCount(1)
                .build();

        Shift nyShift = Shift.builder()
                .id(6L)
                .shiftName("NewYork")
                .timeZoneId("America/New_York")
                .startTime(nyStart)
                .endTime(nyStart.plusHours(8))
                .baseRate(new BigDecimal("100.00"))
                .employeeCount(1)
                .build();

        ShiftCalculationResult moscowResult = calculator.calculate(moscowShift);
        ShiftCalculationResult nyResult = calculator.calculate(nyShift);

        assertEquals(moscowResult.getDurationHours(), nyResult.getDurationHours());
        assertEquals(moscowResult.getTotalLaborCost(), nyResult.getTotalLaborCost());
    }

    @ParameterizedTest
    @CsvSource({
        "2024, 1, 15, 22, 2024, 1, 16, 6, 8",
        "2024, 1, 15, 23, 2024, 1, 16, 7, 8"
    })
    @DisplayName("Параметризованный тест ночных часов")
    void testNightHoursCalculation(int startYear, int startMonth, int startDay, int startHour,
                                    int endYear, int endMonth, int endDay, int endHour,
                                    long expectedNightHours) {
        ZonedDateTime start = ZonedDateTime.of(startYear, startMonth, startDay, startHour, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime end = ZonedDateTime.of(endYear, endMonth, endDay, endHour, 0, 0, 0, ZoneId.of("Europe/Moscow"));

        Shift shift = Shift.builder()
                .id(1L)
                .shiftName("Parametrized Test")
                .timeZoneId("Europe/Moscow")
                .startTime(start)
                .endTime(end)
                .baseRate(BigDecimal.valueOf(100))
                .employeeCount(1)
                .build();

        ShiftCalculationResult result = calculator.calculateWithNightHours(shift, BigDecimal.valueOf(1.5));
        assertEquals(expectedNightHours, result.getNightHours());
    }
}
