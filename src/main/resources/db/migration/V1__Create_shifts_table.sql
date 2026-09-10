-- Создание таблицы смен
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

-- Создание индексов для оптимизации запросов
CREATE INDEX IF NOT EXISTS idx_shifts_time_zone ON shifts(time_zone_id);
CREATE INDEX IF NOT EXISTS idx_shifts_start_time ON shifts(start_time);
CREATE INDEX IF NOT EXISTS idx_shifts_end_time ON shifts(end_time);
CREATE INDEX IF NOT EXISTS idx_shifts_created_at ON shifts(created_at);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE shifts IS 'Таблица для хранения данных о сменах сотрудников';
COMMENT ON COLUMN shifts.id IS 'Уникальный идентификатор смены';
COMMENT ON COLUMN shifts.shift_name IS 'Название смены';
COMMENT ON COLUMN shifts.time_zone_id IS 'Идентификатор часового пояса (например, Europe/Moscow)';
COMMENT ON COLUMN shifts.start_time IS 'Время начала смены с учётом часового пояса';
COMMENT ON COLUMN shifts.end_time IS 'Время окончания смены с учётом часового пояса';
COMMENT ON COLUMN shifts.base_rate IS 'Базовая почасовая ставка оплаты труда';
COMMENT ON COLUMN shifts.employee_count IS 'Количество сотрудников в смене';
COMMENT ON COLUMN shifts.created_at IS 'Время создания записи';
COMMENT ON COLUMN shifts.updated_at IS 'Время последнего обновления записи';
