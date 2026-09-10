package com.shiftcalc.service;

import com.shiftcalc.calculator.ShiftMetricsCalculator;
import com.shiftcalc.model.Shift;
import com.shiftcalc.model.ShiftCalculationResult;
import com.shiftcalc.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления сменами и расчёта показателей.
 * Оркестрирует взаимодействие между репозиторием и калькулятором.
 */
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

    /**
     * Получить все смены и рассчитать показатели для каждой
     */
    public List<ShiftCalculationResult> calculateAllShifts() {
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream()
                .map(calculator::calculate)
                .collect(Collectors.toList());
    }

    /**
     * Рассчитать показатели для конкретной смены по ID
     */
    public Optional<ShiftCalculationResult> calculateShiftById(Long shiftId) {
        return shiftRepository.findById(shiftId)
                .map(calculator::calculate);
    }

    /**
     * Рассчитать показатели для смен в указанном часовом поясе
     */
    public List<ShiftCalculationResult> calculateByTimeZone(String timeZoneId) {
        List<Shift> shifts = shiftRepository.findByTimeZone(timeZoneId);
        return shifts.stream()
                .map(calculator::calculate)
                .collect(Collectors.toList());
    }

    /**
     * Рассчитать показатели для смен с учётом ночных часов
     */
    public List<ShiftCalculationResult> calculateAllWithNightHours(BigDecimal nightRateMultiplier) {
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream()
                .map(shift -> calculator.calculateWithNightHours(shift, nightRateMultiplier))
                .collect(Collectors.toList());
    }

    /**
     * Конвертировать все смены в указанный часовой пояс
     */
    public List<Shift> convertAllShiftsToTimeZone(String targetTimeZoneId) {
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream()
                .map(shift -> calculator.convertToTimeZone(shift, targetTimeZoneId))
                .collect(Collectors.toList());
    }

    /**
     * Создать новую смену
     */
    @Transactional
    public Shift createShift(String shiftName, String timeZoneId, 
                             ZonedDateTime startTime, ZonedDateTime endTime,
                             BigDecimal baseRate, Integer employeeCount) {
        validateTimeZone(timeZoneId);
        validateShiftTimes(startTime, endTime);
        
        return shiftRepository.save(shiftName, timeZoneId, startTime, endTime, baseRate, employeeCount);
    }

    /**
     * Обновить существующую смену
     */
    @Transactional
    public boolean updateShift(Long shiftId, String shiftName, String timeZoneId,
                               ZonedDateTime startTime, ZonedDateTime endTime,
                               BigDecimal baseRate, Integer employeeCount) {
        validateTimeZone(timeZoneId);
        validateShiftTimes(startTime, endTime);
        
        return shiftRepository.update(shiftId, shiftName, timeZoneId, startTime, 
                endTime, baseRate, employeeCount) > 0;
    }

    /**
     * Удалить смену по ID
     */
    @Transactional
    public boolean deleteShift(Long shiftId) {
        return shiftRepository.deleteById(shiftId) > 0;
    }

    /**
     * Валидация часового пояса
     */
    private void validateTimeZone(String timeZoneId) {
        try {
            java.time.ZoneId.of(timeZoneId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный часовой пояс: " + timeZoneId, e);
        }
    }

    /**
     * Валидация временных интервалов смены
     */
    private void validateShiftTimes(ZonedDateTime startTime, ZonedDateTime endTime) {
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
        }
        
        // Проверка, что времена в одном часовом поясе или совместимы
        if (!startTime.getZone().equals(endTime.getZone())) {
            throw new IllegalArgumentException("Время начала и окончания должны быть в одном часовом поясе");
        }
    }
}
