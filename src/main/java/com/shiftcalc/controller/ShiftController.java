package com.shiftcalc.controller;

import com.shiftcalc.dto.ShiftDto;
import com.shiftcalc.model.ShiftCalculationResult;
import com.shiftcalc.service.ShiftCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/shifts")
@Tag(name = "Shift API", description = "API для управления сменами и расчёта показателей")
public class ShiftController {

    private final ShiftCalculationService shiftCalculationService;

    public ShiftController(ShiftCalculationService shiftCalculationService) {
        this.shiftCalculationService = shiftCalculationService;
    }

    @GetMapping
    @Operation(summary = "Получить все смены с расчётом показателей")
    public ResponseEntity<List<ShiftCalculationResult>> getAllShifts() {
        log.info("Запрос на получение всех смен");
        List<ShiftCalculationResult> results = shiftCalculationService.calculateAllShifts();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить смену по ID с расчётом показателей")
    public ResponseEntity<ShiftCalculationResult> getShiftById(
            @Parameter(description = "ID смены") @PathVariable Long id) {
        log.info("Запрос на получение смены с ID={}", id);
        return shiftCalculationService.calculateShiftById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/timezone/{timeZoneId}")
    @Operation(summary = "Получить смены по часовому поясу")
    public ResponseEntity<List<ShiftCalculationResult>> getShiftsByTimeZone(
            @Parameter(description = "Идентификатор часового пояса") @PathVariable String timeZoneId) {
        log.info("Запрос на получение смен в часовом поясе: {}", timeZoneId);
        List<ShiftCalculationResult> results = shiftCalculationService.calculateByTimeZone(timeZoneId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/with-night-hours")
    @Operation(summary = "Получить все смены с учётом ночных часов")
    public ResponseEntity<List<ShiftCalculationResult>> getShiftsWithNightHours(
            @Parameter(description = "Коэффициент доплаты за ночные часы") 
            @RequestParam(defaultValue = "1.5") BigDecimal nightRateMultiplier) {
        log.info("Запрос на получение смен с ночными часами, коэффициент={}", nightRateMultiplier);
        List<ShiftCalculationResult> results = shiftCalculationService.calculateAllWithNightHours(nightRateMultiplier);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    @Operation(summary = "Создать новую смену")
    public ResponseEntity<ShiftCalculationResult> createShift(
            @Valid @RequestBody ShiftDto shiftDto) {
        log.info("Запрос на создание смены: {}", shiftDto.getShiftName());
        var shift = shiftCalculationService.createShift(
                shiftDto.getShiftName(),
                shiftDto.getTimeZoneId(),
                shiftDto.getStartTime(),
                shiftDto.getEndTime(),
                shiftDto.getBaseRate(),
                shiftDto.getEmployeeCount()
        );
        var result = shiftCalculationService.calculateShiftById(shift.getId()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить существующую смену")
    public ResponseEntity<ShiftCalculationResult> updateShift(
            @Parameter(description = "ID смены") @PathVariable Long id,
            @Valid @RequestBody ShiftDto shiftDto) {
        log.info("Запрос на обновление смены с ID={}", id);
        boolean updated = shiftCalculationService.updateShift(
                id,
                shiftDto.getShiftName(),
                shiftDto.getTimeZoneId(),
                shiftDto.getStartTime(),
                shiftDto.getEndTime(),
                shiftDto.getBaseRate(),
                shiftDto.getEmployeeCount()
        );
        if (updated) {
            var result = shiftCalculationService.calculateShiftById(id).orElseThrow();
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить смену")
    public ResponseEntity<Void> deleteShift(
            @Parameter(description = "ID смены") @PathVariable Long id) {
        log.info("Запрос на удаление смены с ID={}", id);
        if (shiftCalculationService.deleteShift(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
