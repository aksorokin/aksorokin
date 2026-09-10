package com.shiftcalc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Инициализатор базы данных.
 * Создаёт необходимую схему таблиц при старте приложения.
 */
@Component
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        logger.info("Инициализация схемы базы данных...");
        
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS shifts (
                id BIGSERIAL PRIMARY KEY,
                shift_name VARCHAR(255) NOT NULL,
                time_zone_id VARCHAR(100) NOT NULL,
                start_time TIMESTAMPTZ NOT NULL,
                end_time TIMESTAMPTZ NOT NULL,
                base_rate NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
                employee_count INTEGER NOT NULL DEFAULT 1,
                created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                
                CONSTRAINT chk_shift_times CHECK (end_time > start_time),
                CONSTRAINT chk_employee_count CHECK (employee_count > 0),
                CONSTRAINT chk_base_rate CHECK (base_rate >= 0)
            )
            """;
        
        jdbcTemplate.execute(createTableSql);
        logger.info("Таблица 'shifts' создана или уже существует");
        
        // Создание индексов для оптимизации запросов
        createIndexes();
        
        logger.info("Инициализация схемы базы данных завершена");
    }

    private void createIndexes() {
        String[] indexes = {
            "CREATE INDEX IF NOT EXISTS idx_shifts_time_zone ON shifts(time_zone_id)",
            "CREATE INDEX IF NOT EXISTS idx_shifts_start_time ON shifts(start_time)",
            "CREATE INDEX IF NOT EXISTS idx_shifts_end_time ON shifts(end_time)",
            "CREATE INDEX IF NOT EXISTS idx_shifts_created_at ON shifts(created_at)"
        };
        
        for (String indexSql : indexes) {
            jdbcTemplate.execute(indexSql);
            logger.debug("Индекс создан: {}", indexSql);
        }
    }
}
