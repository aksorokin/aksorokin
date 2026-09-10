# Shift Calculator Backend

Backend-приложение для расчёта показателей смен сотрудников, работающих в разных часовых поясах.

## Технологии

- **Java 17**
- **Spring Boot 3.2**
- **Spring JDBC**
- **PostgreSQL** (TIMESTAMPTZ для работы с часовыми поясами)
- **JUnit 5** для тестирования

## Архитектура решения

### Ключевые компоненты

1. **Модели данных** (`com.shiftcalc.model`)
   - `Shift` - модель смены с поддержкой ZonedDateTime
   - `ShiftCalculationResult` - результат расчёта показателей

2. **Репозиторий** (`com.shiftcalc.repository`)
   - `ShiftRepository` - CRUD операции с использованием Spring JDBC

3. **Сервис** (`com.shiftcalc.service`)
   - `ShiftCalculationService` - бизнес-логика и оркестрация

4. **Калькулятор** (`com.shiftcalc.calculator`)
   - `ShiftMetricsCalculator` - расчёт метрик с учётом часовых поясов

5. **Конфигурация** (`com.shiftcalc.config`)
   - `DatabaseConfig` - настройка DataSource и JdbcTemplate
   - `DatabaseInitializer` - авто-создание схемы БД

## Расчётные показатели

Приложение рассчитывает следующие показатели для каждой смены:

| Показатель | Описание |
|------------|----------|
| `durationHours` | Продолжительность смены в часах |
| `totalEmployeeHours` | Общее количество человеко-часов (часы × сотрудники) |
| `totalLaborCost` | Общий фонд оплаты труда |
| `averageHourlyRate` | Средняя почасовая ставка |
| `nightHours` | Количество ночных часов (22:00-06:00 локального времени) |

## Особенности работы с часовыми поясами

1. **Хранение данных**: PostgreSQL тип `TIMESTAMPTZ` хранит время с информацией о часовом поясе
2. **Локальное время**: Каждая смена хранится в своём часовом поясе (Europe/Moscow, America/New_York, etc.)
3. **Конвертация**: Возможность конвертации всех смен в единый часовой пояс (UTC) для отчётности
4. **Ночные часы**: Расчёт ночных часов производится по локальному времени каждой смены

## Запуск приложения

### Требования

- Java 17+
- PostgreSQL 12+
- Maven 3.8+

### Настройка базы данных

```bash
createdb shift_calculator
```

### Конфигурация

Отредактируйте `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/shift_calculator
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Сборка и запуск

```bash
mvn clean package
java -jar target/shift-calculator-1.0.0-SNAPSHOT.jar
```

Или через Maven:

```bash
mvn spring-boot:run
```

## Пример использования

```java
@Autowired
private ShiftCalculationService service;

// Создание смены в московском часовом поясе
ZoneId moscowZone = ZoneId.of("Europe/Moscow");
ZonedDateTime start = ZonedDateTime.now(moscowZone).withHour(9).withMinute(0);
ZonedDateTime end = start.plusHours(9);

Shift shift = service.createShift(
    "Утренняя смена",
    "Europe/Moscow",
    start,
    end,
    new BigDecimal("1500.00"),  // почасовая ставка
    10                           // количество сотрудников
);

// Расчёт показателей
ShiftCalculationResult result = service.calculateShiftById(shift.getId()).get();
System.out.println("Человеко-часов: " + result.getTotalEmployeeHours());
System.out.println("Фонд оплаты: " + result.getTotalLaborCost());

// Расчёт с учётом ночных часов (коэффициент 1.5)
List<ShiftCalculationResult> results = service.calculateAllWithNightHours(new BigDecimal("1.5"));

// Конвертация в UTC для отчётности
List<Shift> utcShifts = service.convertAllShiftsToTimeZone("UTC");
```

## Тестирование

```bash
mvn test
```

Тесты покрывают:
- Базовый расчёт показателей
- Расчёт с ночными часами
- Конвертацию между часовыми поясами
- Корректность работы с разными временными зонами

## Структура проекта

```
src/
├── main/
│   ├── java/com/shiftcalc/
│   │   ├── config/           # Конфигурация
│   │   ├── model/            # Модели данных
│   │   ├── repository/       # Репозитории
│   │   ├── service/          # Сервисы
│   │   ├── calculator/       # Калькуляторы
│   │   └── ShiftCalculatorApplication.java
│   └── resources/
│       ├── application.properties
│       └── schema.sql
└── test/
    └── java/com/shiftcalc/
        └── calculator/
            └── ShiftMetricsCalculatorTest.java
```

## SQL запросы для отчётности

Примеры полезных запросов доступны в файле `schema.sql`:
- Конвертация времени в UTC
- Группировка по часовым поясам
- Выявление смен с ночными часами

## Расширение функциональности

Для добавления новых показателей:

1. Добавьте поля в `ShiftCalculationResult`
2. Реализуйте логику расчёта в `ShiftMetricsCalculator`
3. Добавьте методы в `ShiftCalculationService`
4. Покройте тестами

## Лицензия

MIT
