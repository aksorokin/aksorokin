-- SQL скрипт для создания схемы базы данных (альтернативный вариант)
-- Используется для ручного развёртывания или миграций

-- Таблица смен с поддержкой часовых поясов
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
);

-- Индексы для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_shifts_time_zone ON shifts(time_zone_id);
CREATE INDEX IF NOT EXISTS idx_shifts_start_time ON shifts(start_time);
CREATE INDEX IF NOT EXISTS idx_shifts_end_time ON shifts(end_time);
CREATE INDEX IF NOT EXISTS idx_shifts_created_at ON shifts(created_at);

-- Комментарии к таблице и полям
COMMENT ON TABLE shifts IS 'Таблица для хранения информации о сменах сотрудников в разных часовых поясах';
COMMENT ON COLUMN shifts.shift_name IS 'Название смены';
COMMENT ON COLUMN shifts.time_zone_id IS 'Идентификатор часового пояса (например, Europe/Moscow, America/New_York)';
COMMENT ON COLUMN shifts.start_time IS 'Время начала смены с учётом часового пояса (TIMESTAMPTZ)';
COMMENT ON COLUMN shifts.end_time IS 'Время окончания смены с учётом часового пояса (TIMESTAMPTZ)';
COMMENT ON COLUMN shifts.base_rate IS 'Базовая почасовая ставка оплаты труда';
COMMENT ON COLUMN shifts.employee_count IS 'Количество сотрудников в смене';

-- Примеры данных для тестирования
INSERT INTO shifts (shift_name, time_zone_id, start_time, end_time, base_rate, employee_count) VALUES
    ('Московская утренняя', 'Europe/Moscow', '2024-01-15 09:00:00+03', '2024-01-15 18:00:00+03', 1500.00, 10),
    ('Нью-Йорк дневная', 'America/New_York', '2024-01-15 09:00:00-05', '2024-01-16 00:00:00-05', 25.00, 15),
    ('Токио ночная', 'Asia/Tokyo', '2024-01-15 22:00:00+09', '2024-01-16 06:00:00+09', 3000.00, 8),
    ('Лондон утренняя', 'Europe/London', '2024-01-15 06:00:00+00', '2024-01-15 14:00:00+00', 20.00, 12);

-- Полезные запросы для отчётности

-- 1. Получить все смены с конвертацией в UTC
SELECT 
    id,
    shift_name,
    time_zone_id,
    start_time AT TIME ZONE 'UTC' AS start_time_utc,
    end_time AT TIME ZONE 'UTC' AS end_time_utc,
    EXTRACT(EPOCH FROM (end_time - start_time)) / 3600 AS duration_hours,
    base_rate,
    employee_count
FROM shifts
ORDER BY start_time;

-- 2. Группировка по часовым поясам
SELECT 
    time_zone_id,
    COUNT(*) AS shift_count,
    SUM(EXTRACT(EPOCH FROM (end_time - start_time)) / 3600 * employee_count) AS total_employee_hours,
    AVG(base_rate) AS avg_hourly_rate
FROM shifts
GROUP BY time_zone_id
ORDER BY time_zone_id;

-- 3. Смены с ночными часами (22:00 - 06:00)
SELECT 
    id,
    shift_name,
    time_zone_id,
    start_time,
    end_time,
    CASE 
        WHEN EXTRACT(HOUR FROM start_time) >= 22 OR EXTRACT(HOUR FROM end_time) <= 6 
        THEN true 
        ELSE false 
    END AS has_night_hours
FROM shifts;
