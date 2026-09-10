package com.shiftcalc.repository;

import com.shiftcalc.model.Shift;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ShiftRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Shift> shiftRowMapper = (rs, rowNum) -> Shift.builder()
            .id(rs.getLong("id"))
            .shiftName(rs.getString("shift_name"))
            .timeZoneId(rs.getString("time_zone_id"))
            .startTime(rs.getObject("start_time", ZonedDateTime.class))
            .endTime(rs.getObject("end_time", ZonedDateTime.class))
            .baseRate(rs.getBigDecimal("base_rate"))
            .employeeCount(rs.getInt("employee_count"))
            .build();

    public ShiftRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Shift> findAll() {
        String sql = """
            SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count 
            FROM shifts ORDER BY start_time""";
        log.debug("Выполнение запроса: {}", sql);
        return jdbcTemplate.query(sql, shiftRowMapper);
    }

    public Optional<Shift> findById(Long id) {
        String sql = """
            SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count 
            FROM shifts WHERE id = ?""";
        try {
            Shift shift = jdbcTemplate.queryForObject(sql, shiftRowMapper, id);
            return Optional.ofNullable(shift);
        } catch (Exception e) {
            log.debug("Смена с ID={} не найдена: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    public List<Shift> findByTimeZone(String timeZoneId) {
        String sql = """
            SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count 
            FROM shifts WHERE time_zone_id = ? ORDER BY start_time""";
        return jdbcTemplate.query(sql, shiftRowMapper, timeZoneId);
    }

    public List<Shift> findByDateRange(ZonedDateTime startUtc, ZonedDateTime endUtc) {
        String sql = """
            SELECT id, shift_name, time_zone_id, start_time, end_time, base_rate, employee_count 
            FROM shifts WHERE start_time >= ? AND end_time <= ? ORDER BY start_time""";
        return jdbcTemplate.query(sql, shiftRowMapper, startUtc, endUtc);
    }

    @Transactional
    public Shift save(String shiftName, String timeZoneId, ZonedDateTime startTime,
                      ZonedDateTime endTime, BigDecimal baseRate, Integer employeeCount) {
        String sql = """
            INSERT INTO shifts (shift_name, time_zone_id, start_time, end_time, base_rate, employee_count) 
            VALUES (?, ?, ?, ?, ?, ?) RETURNING id""";

        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                shiftName, timeZoneId, startTime, endTime, baseRate, employeeCount);

        log.info("Сохранена новая смена: ID={}, name={}", id, shiftName);
        return Shift.builder()
                .id(id)
                .shiftName(shiftName)
                .timeZoneId(timeZoneId)
                .startTime(startTime)
                .endTime(endTime)
                .baseRate(baseRate)
                .employeeCount(employeeCount)
                .build();
    }

    @Transactional
    public int update(Long id, String shiftName, String timeZoneId, ZonedDateTime startTime,
                      ZonedDateTime endTime, BigDecimal baseRate, Integer employeeCount) {
        String sql = """
            UPDATE shifts SET shift_name = ?, time_zone_id = ?, start_time = ?, 
            end_time = ?, base_rate = ?, employee_count = ? WHERE id = ?""";
        int rowsAffected = jdbcTemplate.update(sql, shiftName, timeZoneId, startTime, endTime, baseRate, employeeCount, id);
        log.info("Обновлена смена: ID={}, rows={}", id, rowsAffected);
        return rowsAffected;
    }

    @Transactional
    public int deleteById(Long id) {
        String sql = "DELETE FROM shifts WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        log.info("Удалена смена: ID={}, rows={}", id, rowsAffected);
        return rowsAffected;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM shifts";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
