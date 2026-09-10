package com.shiftcalc.service;

import com.shiftcalc.calculator.ShiftMetricsCalculator;
import com.shiftcalc.model.Shift;
import com.shiftcalc.model.ShiftCalculationResult;
import com.shiftcalc.repository.ShiftRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ShiftCalculationService {

    private final ShiftRepository shiftRepository;
    private final ShiftMetricsCalculator calculator;

    public ShiftCalculationService(ShiftRepository shiftRepository,
                                   ShiftMetricsCalculator calculator) {
        this.shiftRepository = shiftRepository;
        this.calculator = calculator;
    }

    public List<ShiftCalculationResult> calculateAllShifts() {
        log.info("Расчёт показателей для всех смен");
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream()
                .map(calculator::calculate)
                .collect(Collectors.toList());
    }

    public Optional<ShiftCalculationResult> calculateShiftById(Long shiftId) {
        log.debug("Расчёт показателей для смены с ID={}", shiftId);
        return shiftRepository.findById(shiftId)
                .map(calculator::calculate);
    }

    public List<ShiftCalculationResult> calculateByTimeZone(String timeZoneId) {
        log.info("Расчёт показателей для смен в часовом поясе: {}", timeZoneId);
        validateTimeZone(timeZoneId);
        List<Shift> shifts = shiftRepository.findByTimeZone(timeZoneId);
        return shifts.stream()
                .map(calculator::calculate)
                .collect(Collectors.toList());
    }

    public List<ShiftCalculationResult> calculateAllWithNightHours(BigDecimal nightRateMultiplier) {
        log.info("Расчёт показателей с ночными часами, коэффициент={}", nightRateMultiplier);
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream()
                .map(shift -> calculator.calculateWithNightHours(shift, nightRateMultiplier))
                .collect(Collectors.toList());
    }

    public List<Shift> convertAllShiftsToTimeZone(String targetTimeZoneId) {
        log.info("Конвертация всех смен в часовой пояс: {}", targetTimeZoneId);
        validateTimeZone(targetTimeZoneId);
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream()
                .map(shift -> calculator.convertToTimeZone(shift, targetTimeZoneId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Shift createShift(String shiftName, String timeZoneId,
                             ZonedDateTime startTime, ZonedDateTime endTime,
                             BigDecimal baseRate, Integer employeeCount) {
        log.info("Создание новой смены: {} в часовом поясе {}", shiftName, timeZoneId);
        validateTimeZone(timeZoneId);
        validateShiftTimes(startTime, endTime);

        return shiftRepository.save(shiftName, timeZoneId, startTime, endTime, baseRate, employeeCount);
    }

    @Transactional
    public boolean updateShift(Long shiftId, String shiftName, String timeZoneId,
                               ZonedDateTime startTime, ZonedDateTime endTime,
                               BigDecimal baseRate, Integer employeeCount) {
        log.info("Обновление смены с ID={}", shiftId);
        validateTimeZone(timeZoneId);
        validateShiftTimes(startTime, endTime);

        return shiftRepository.update(shiftId, shiftName, timeZoneId, startTime,
                endTime, baseRate, employeeCount) > 0;
    }

    @Transactional
    public boolean deleteShift(Long shiftId) {
        log.info("Удаление смены с ID={}", shiftId);
        return shiftRepository.deleteById(shiftId) > 0;
    }

    private void validateTimeZone(String timeZoneId) {
        try {
            ZoneId.of(timeZoneId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный часовой пояс: " + timeZoneId, e);
        }
    }

    private void validateShiftTimes(ZonedDateTime startTime, ZonedDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Время начала и окончания не могут быть null");
        }
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
        }
        if (!startTime.getZone().equals(endTime.getZone())) {
            throw new IllegalArgumentException("Время начала и окончания должны быть в одном часовом поясе");
        }
    }
}
