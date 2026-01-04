# UNIQUE Constraints Support

**Фаза 4** - Реализовано в рамках Joot Framework  
**Дата:** 2026-01-02

## Описание

Автоматическая генерация уникальных значений для полей с UNIQUE constraints. Предотвращает ошибки дублирования при создании тестовых данных.

## Проблема

До реализации:
```java
// Создаём 2 автора с UNIQUE email
Author author1 = ctx.create(AUTHOR, Author.class).build();
Author author2 = ctx.create(AUTHOR, Author.class).build();

// ❌ Ошибка: duplicate key value violates unique constraint "author_email_key"
```

Оба автора получали одинаковый или коллизионный email, что приводило к ошибке БД.

## Решение

### Атомарный счётчик для гарантии уникальности

Для UNIQUE полей используется **thread-safe AtomicLong счётчик**:

```java
private static final AtomicLong UNIQUE_COUNTER = new AtomicLong(1);
```

### Генерация уникальных значений

**String с UNIQUE:**
```
Формат: "unq_<counter>_<random>"
Примеры: "unq_1_a3f9", "unq_2_b7k2", "unq_3_c8m1"
```

**Integer с UNIQUE:**
```
Формат: <counter>
Примеры: 1, 2, 3, 4, 5, ...
```

**Long с UNIQUE:**
```
Формат: <counter>
Примеры: 1L, 2L, 3L, ...
```

**UUID:**
```
UUID всегда уникален по природе (используется UUID.randomUUID())
```

## API

### Автоматическая генерация

```java
// Создаём несколько авторов - emails автоматически уникальны
Author author1 = ctx.create(AUTHOR, Author.class).build();
Author author2 = ctx.create(AUTHOR, Author.class).build();
Author author3 = ctx.create(AUTHOR, Author.class).build();

// Emails: "unq_1_a3f9", "unq_2_b7k2", "unq_3_c8m1" - все разные! ✅
```

### Явная установка значения

```java
// Можно явно задать значение для UNIQUE поля
Author author = ctx.create(AUTHOR, Author.class)
    .set(AUTHOR.EMAIL, "explicit@example.com")
    .build();
```

### FK Auto-creation с UNIQUE полями

```java
// Создаём несколько книг - каждая автоматически создаёт автора
Book book1 = ctx.create(BOOK, Book.class).build();
Book book2 = ctx.create(BOOK, Book.class).build();
Book book3 = ctx.create(BOOK, Book.class).build();

// 3 разных автора с уникальными emails ✅
// Никаких коллизий UNIQUE constraint!
```

## Реализация

### MetadataAnalyzer

Добавлены методы для анализа UNIQUE constraints:

```java
// Получить все UNIQUE поля таблицы
Set<Field<?>> uniqueFields = metadataAnalyzer.getUniqueFields(table);

// Проверить конкретное поле
boolean isUnique = metadataAnalyzer.isUniqueField(field, table);
```

**Поддерживаются:**
- Single-column UNIQUE constraints
- Composite UNIQUE keys (все поля из ключа)
- PRIMARY KEY автоматически исключается (обрабатывается отдельно)

### RecordBuilderImpl

Обновлён метод `generateDefaultValue()`:

```java
private Object generateDefaultValue(Field<?> field) {
    boolean isUnique = metadataAnalyzer.isUniqueField(field, table);
    
    if (type == String.class && isUnique) {
        long counter = UNIQUE_COUNTER.getAndIncrement();
        return "unq_" + counter + "_" + UUID.randomUUID().toString().substring(0, 4);
    }
    // ... остальные типы
}
```

## Схема для тестов

```sql
CREATE TABLE author (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,  -- UNIQUE constraint
    country VARCHAR(100)
);

CREATE TABLE book (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id UUID NOT NULL REFERENCES author(id),
    isbn VARCHAR(20) UNIQUE,    -- UNIQUE constraint
    pages INTEGER,
    description TEXT
);

CREATE TABLE publisher (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,  -- UNIQUE constraint
    country VARCHAR(100)
);
```

## Тесты

**UniqueConstraintTest** - 5 тестов (все проходят ✅):

1. `shouldGenerateUniqueValuesForUniqueField()` - прямое создание с UNIQUE
2. `shouldGenerateUniqueISBNForBooks()` - UNIQUE для ISBNs
3. `shouldGenerateUniquePublisherNames()` - UNIQUE для publisher names
4. `shouldGenerateUniqueValuesForFKAutoCreation()` - FK auto-creation с UNIQUE полями
5. `shouldAllowExplicitValueForUniqueField()` - явная установка UNIQUE значения

## Технические детали

### Thread-Safety

`AtomicLong UNIQUE_COUNTER` гарантирует уникальность в многопоточной среде.

### Короткий формат

Формат `"unq_<counter>_<random>"` компактный и помещается в короткие VARCHAR поля (например, VARCHAR(20)).

### Производительность

- O(1) для генерации уникального значения
- Нет обращений к БД для проверки уникальности
- Нет retry логики (уникальность гарантирована счётчиком)

## Ограничения

1. **Composite UNIQUE keys**: Если несколько полей образуют composite UNIQUE key, каждое поле генерируется независимо. Это гарантирует уникальность композита в большинстве случаев.

2. **Существующие данные**: Счётчик не учитывает существующие записи в БД. Начинает с 1 при каждом запуске тестов.

3. **Типы данных**: Поддерживаются String, Integer, Long, UUID. Для других типов с UNIQUE - исключение.

## Файлы

- `src/main/java/io/github/jtestkit/joot/MetadataAnalyzer.java` - анализ UNIQUE constraints
- `src/main/java/io/github/jtestkit/joot/RecordBuilderImpl.java` - генерация уникальных значений
- `src/test/java/integration/UniqueConstraintTest.java` - 5 тестов
- `src/test/resources/test-schema.sql` - схема с UNIQUE constraints

## Примеры значений

| Тип | UNIQUE | Пример значения |
|-----|--------|-----------------|
| String | ❌ | `"generated_a3f9x7k2"` |
| String | ✅ | `"unq_1_a3f9"`, `"unq_2_b7k2"` |
| Integer | ❌ | `42`, `157`, `983` (random) |
| Integer | ✅ | `1`, `2`, `3`, ... (counter) |
| Long | ❌ | `42L`, `157L` (random) |
| Long | ✅ | `1L`, `2L`, `3L`, ... (counter) |
| UUID | ⚠️ | Всегда уникален | 
| Boolean | N/A | `true`/`false` (random) |

