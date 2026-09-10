package com.shiftcalc.repository;

import com.shiftcalc.model.Shift;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с данными смен.
 * Использует Spring JDBC для доступа к PostgreSQL.
 */
@Repository
public class ShiftRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Shift> shiftRowMapper = (rs, rowNum) -> new Shift(
            rs.getLong("id"),
            rs.getString("shift_name"),
            rs.getString("time_zone_id"),
            rs.getObject("start_time", ZonedDateTime.class),
            rs.getObject("end_time", ZonedDateTime.class),
            rs.getBigDecimal("base_rate"),
            rs.getInt("employee_count")
    );

    public ShiftRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Получить все смены
     */
    public List<Shift> findAll() {
        String sql = "SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count " +
                     "FROM shifts ORDER BY start_time";
        return jdbcTemplate.query(sql, shiftRowMapper);
    }

    /**
     * Получить смену по ID
     */
    public Optional<Shift> findById(Long id) {
        String sql = "SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count " +
                     "FROM shifts WHERE id = ?";
        try {
            Shift shift = jdbcTemplate.queryForObject(sql, shiftRowMapper, id);
            return Optional.ofNullable(shift);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Найти смены по часовому поясу
     */
    public List<Shift> findByTimeZone(String timeZoneId) {
        String sql = "SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count " +
                     "FROM shifts WHERE time_zone_id = ? ORDER BY start_time";
        return jdbcTemplate.query(sql, shiftRowMapper, timeZoneId);
    }

    /**
     * Найти смены в диапазоне дат (в UTC)
     */
    public List<Shift> findByDateRange(ZonedDateTime startUtc, ZonedDateTime endUtc) {
        String sql = "SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count " +
                     "FROM shifts WHERE start_time >= ? AND end_time <= ? ORDER BY start_time";
        return jdbcTemplate.query(sql, shiftRowMapper, startUtc, endUtc);
    }

    /**
     * Сохранить новую смену
     */
    public Shift save(String shiftName, String timeZoneId, ZonedDateTime startTime, 
                      ZonedDateTime endTime, BigDecimal baseRate, Integer employeeCount) {
        String sql = "INSERT INTO shifts (shift_name, time_zone_id, start_time, end_time, base_rate, employee_count) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        
        Long id = jdbcTemplate.queryForObject(sql, Long.class, 
                shiftName, timeZoneId, startTime, endTime, baseRate, employeeCount);
        
        return new Shift(id, shiftName, timeZoneId, startTime, endTime, baseRate, employeeCount);
    }

    /**
     * Обновить смену
     */
    public int update(Long id, String shiftName, String timeZoneId, ZonedDateTime startTime,
                      ZonedDateTime endTime, BigDecimal baseRate, Integer employeeCount) {
        String sql = "UPDATE shifts SET shift_name = ?, time_zone_id = ?, start_time = ?, " +
                     "end_time = ?, base_rate = ?, employee_count = ? WHERE id = ?";
        return jdbcTemplate.update(sql, shiftName, timeZoneId, startTime, endTime, baseRate, employeeCount, id);
    }

    /**
     * Удалить смену
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM shifts WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
