package com.shiftcalc;

import com.shiftcalc.calculator.ShiftMetricsCalculator;
import com.shiftcalc.model.Shift;
import com.shiftcalc.model.ShiftCalculationResult;
import com.shiftcalc.repository.ShiftRepository;
import com.shiftcalc.service.ShiftCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Главное приложение для расчёта показателей смен.
 * Запускается как standalone application без web интерфейса.
 */
@SpringBootApplication
public class ShiftCalculatorApplication {

    private static final Logger logger = LoggerFactory.getLogger(ShiftCalculatorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShiftCalculatorApplication.class, args);
    }

    /**
     * Демонстрационный runner для тестирования функциональности.
     * Выполняется после запуска приложения.
     */
    @Bean
    public CommandLineRunner demo(ShiftCalculationService service, 
                                   ShiftRepository repository,
                                   ShiftMetricsCalculator calculator) {
        return args -> {
            logger.info("=== Запуск демонстрации расчёта показателей смен ===\n");

            // Создание тестовых данных с разными часовыми поясами
            logger.info("Создание тестовых смен в разных часовых поясах...");
            
            ZoneId moscowZone = ZoneId.of("Europe/Moscow");
            ZoneId newYorkZone = ZoneId.of("America/New_York");
            ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
            ZoneId londonZone = ZoneId.of("Europe/London");

            ZonedDateTime now = ZonedDateTime.now();

            // Смена 1: Москва (UTC+3)
            Shift shiftMoscow = service.createShift(
                    "Московская смена",
                    "Europe/Moscow",
                    now.withZoneSameInstant(moscowZone).withHour(9).withMinute(0).withSecond(0),
                    now.withZoneSameInstant(moscowZone).withHour(18).withMinute(0).withSecond(0),
                    new BigDecimal("1500.00"),
                    10
            );
            logger.info("Создана смена: {}", shiftMoscow.getShiftName());

            // Смена 2: Нью-Йорк (UTC-5/-4)
            Shift shiftNY = service.createShift(
                    "Нью-Йорк смена",
                    "America/New_York",
                    now.withZoneSameInstant(newYorkZone).withHour(9).withMinute(0).withSecond(0),
                    now.withZoneSameInstant(newYorkZone).plusDays(1).withHour(0).withMinute(0).withSecond(0),
                    new BigDecimal("25.00"),
                    15
            );
            logger.info("Создана смена: {}", shiftNY.getShiftName());

            // Смена 3: Токио (UTC+9)
            Shift shiftTokyo = service.createShift(
                    "Токио смена",
                    "Asia/Tokyo",
                    now.withZoneSameInstant(tokyoZone).withHour(22).withMinute(0).withSecond(0),
                    now.withZoneSameInstant(tokyoZone).plusDays(1).withHour(6).withMinute(0).withSecond(0),
                    new BigDecimal("3000.00"),
                    8
            );
            logger.info("Создана смена: {}", shiftTokyo.getShiftName());

            // Смена 4: Лондон (UTC+0/+1)
            Shift shiftLondon = service.createShift(
                    "Лондон смена",
                    "Europe/London",
                    now.withZoneSameInstant(londonZone).withHour(6).withMinute(0).withSecond(0),
                    now.withZoneSameInstant(londonZone).withHour(14).withMinute(0).withSecond(0),
                    new BigDecimal("20.00"),
                    12
            );
            logger.info("Создана смена: {}", shiftLondon.getShiftName());

            logger.info("\n=== Расчёт базовых показателей для всех смен ===\n");
            
            List<ShiftCalculationResult> results = service.calculateAllShifts();
            
            for (ShiftCalculationResult result : results) {
                logger.info("{}", result);
                logger.info("  Часовой пояс: {}", result.getTimeZoneId());
                logger.info("  Начало (локальное): {} {}", 
                        result.getStartTime().toLocalDateTime(), 
                        result.getStartTime().getOffset());
                logger.info("  Окончание (локальное): {} {}", 
                        result.getEndTime().toLocalDateTime(), 
                        result.getEndTime().getOffset());
                logger.info("  Продолжительность: {} ч.", result.getDurationHours());
                logger.info("  Сотрудников: {}", result.getEmployeeCount());
                logger.info("  Человеко-часов: {}", result.getTotalEmployeeHours());
                logger.info("  Общий фонд оплаты: {}", result.getTotalLaborCost());
                logger.info("  Средняя почасовая ставка: {}", result.getAverageHourlyRate());
                logger.info("---");
            }

            logger.info("\n=== Расчёт с учётом ночных часов (коэффициент 1.5) ===\n");
            
            List<ShiftCalculationResult> nightResults = service.calculateAllWithNightHours(new BigDecimal("1.5"));
            
            for (ShiftCalculationResult result : nightResults) {
                logger.info("{} (с ночными)", result.getShiftName());
                logger.info("  Общий фонд оплаты (с ночными): {}", result.getTotalLaborCost());
                logger.info("---");
            }

            logger.info("\n=== Конвертация всех смен в UTC для отчётности ===\n");
            
            List<Shift> utcShifts = service.convertAllShiftsToTimeZone("UTC");
            
            for (Shift shift : utcShifts) {
                logger.info("{} в UTC:", shift.getShiftName());
                logger.info("  Начало: {}", shift.getStartTime());
                logger.info("  Окончание: {}", shift.getEndTime());
                logger.info("---");
            }

            logger.info("\n=== Фильтрация по часовому поясу (Europe/Moscow) ===\n");
            
            List<ShiftCalculationResult> moscowResults = service.calculateByTimeZone("Europe/Moscow");
            logger.info("Найдено смен в Moscow: {}", moscowResults.size());
            
            for (ShiftCalculationResult result : moscowResults) {
                logger.info("  - {} ({} ч., стоимость: {})", 
                        result.getShiftName(), 
                        result.getDurationHours(),
                        result.getTotalLaborCost());
            }

            logger.info("\n=== Демонстрация завершена ===");
        };
    }
}
