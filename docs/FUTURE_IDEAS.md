# Joot: Идеи для будущих версий (v2.0+)

> **Статус:** Brainstorming / Parking Lot  
> **Дата:** 2026-01-03  
> **Приоритет:** Не определён (вернёмся после релиза v1.0.0)

---

## 🎯 Направления развития

### 1. Test Assertions / Matchers

**Проблема:** Нужны удобные проверки для jOOQ сущностей и БД.

#### Вариант A: С кодогенерацией (type-safe)
```java
// Генерируем AuthorAssert, BookAssert для каждого POJO
assertThat(author)
    .hasName("John")
    .hasEmail("john@test.com")
    .hasRating(5.0);
```

**Плюсы:** Type-safe, IDE автодополнение, красивый API  
**Минусы:** Нужна кодогенерация, сложность

#### Вариант B: Без кодогенерации (generic)
```java
// Используем jOOQ metadata
assertThat(author, AUTHOR)
    .field(AUTHOR.NAME).isEqualTo("John")
    .field(AUTHOR.EMAIL).matches(".*@test.com");
```

**Плюсы:** Нет кодогенерации, работает с любыми таблицами  
**Минусы:** Чуть более verbose

#### Вариант C: Гибридный (database-focused)
```java
// Database assertions
assertThat(AUTHOR)
    .hasRecordCount(5)
    .hasUniqueValues(AUTHOR.EMAIL)
    .hasNoNullValues(AUTHOR.NAME);

// POJO assertions
assertThat(author, AUTHOR)
    .field(AUTHOR.NAME).isEqualTo("John")
    .exists(dsl);

// Query assertions
assertThat(dsl.selectFrom(AUTHOR))
    .hasSize(5)
    .extracting(AUTHOR.NAME)
    .containsExactly("John", "Jane", "Bob");
```

**Плюсы:** Нет кодогенерации, покрывает разные use cases, уникальная ценность  
**Минусы:** Не такой красивый как Вариант A

---

### 2. Database Change Tracking / Diff

**Проблема:** Как проверить, какие ИМЕННО изменения внёс метод в БД?

```java
@Test
void shouldCreateBook() {
    Author author = ctx.create(AUTHOR, Author.class).build();
    
    // ACT
    bookService.create(request);  // ← Что изменилось?
    
    // Нужно проверить:
    // - Создалась запись BOOK?
    // - Обновился AUTHOR.books_count?
    // - Создался audit log?
    // - Триггеры создали что-то?
}
```

#### Вариант 1: Snapshot + Diff
```java
DatabaseSnapshot before = ctx.snapshot();

bookService.create(request);

DatabaseDiff diff = ctx.diff(before);

assertThat(diff)
    .hasInserted(BOOK, 1)
    .hasNoChanges(AUTHOR)
    .hasInserted(AUDIT_LOG, 1);

Book createdBook = diff.getInserted(BOOK).first();
assertThat(createdBook.getTitle()).isEqualTo("Title");
```

**Преимущества:** Видны ВСЕ изменения (даже неожиданные), работает с триггерами

#### Вариант 2: Change Tracker
```java
ChangeTracker tracker = ctx.trackChanges()
    .watching(BOOK, AUTHOR, AUDIT_LOG);

bookService.create(request);

Changes changes = tracker.getChanges();

assertThat(changes)
    .inserted(BOOK).hasSize(1)
        .first()
        .hasFieldValue(BOOK.TITLE, "Title");

assertThat(changes)
    .updated(AUTHOR).isEmpty()
    .deleted().isEmpty();
```

**Преимущества:** Более детальный API, fluent assertions

#### Вариант 3: Transaction-aware
```java
TransactionChanges changes = ctx.inTransaction(tx -> {
    bookService.create(request);
    return tx.getChanges();
});

assertThat(changes)
    .hasInserted(BOOK, 1)
    .hasExecutedQueries(3)
    .hasAffectedRows(1);
```

#### Вариант 4: Query Spy
```java
QuerySpy spy = ctx.spyQueries();

bookService.create(request);

assertThat(spy)
    .executedInsert(BOOK)
        .withValues(BOOK.TITLE, "Title");

assertThat(spy)
    .didNotExecute("UPDATE")
    .executedExactly(1, "INSERT");
```

**Use Cases:**
- Проверка side effects
- Проверка "ничего лишнего не создалось"
- Проверка cascades (cascade delete)
- Performance assertions (N+1 queries)

---

### 3. Test Fixtures / Templates

**Проблема:** Переиспользуемые шаблоны данных для стандартных сценариев.

```java
// Определяем фикстуры
ctx.defineFixture("bestseller-author", AUTHOR, Author.class)
    .set(AUTHOR.NAME, "Stephen King")
    .set(AUTHOR.RATING, 5.0);

// Переиспользуем
Author author1 = ctx.fromFixture("bestseller-author").build();
Author author2 = ctx.fromFixture("bestseller-author")
    .set(AUTHOR.NAME, "J.K. Rowling")
    .build();
```

**Use case:** Admin user, premium customer, standard order scenarios

---

### 4. Database State Management

**Проблема:** Изоляция тестов, быстрое восстановление состояния.

```java
// Сохраняем состояние
String snapshot = ctx.snapshot();

// Делаем что-то в тесте
ctx.create(AUTHOR, Author.class).build();

// Откатываемся
ctx.restore(snapshot);
```

---

### 5. Query Testing Utilities

**Проблема:** Проверка производительности и оптимизации запросов.

```java
Query query = dsl.selectFrom(AUTHOR).where(AUTHOR.RATING.gt(4.0));

assertThat(query)
    .returnsExactly(3)
    .hasExecutionTimeLessThan(100, TimeUnit.MILLISECONDS)
    .usesIndex("idx_author_rating");
```

**Use case:** Performance testing, проверка execution plans

---

### 6. Test Data Seeding для Dev/Staging

**Проблема:** Быстрое заполнение окружений тестовыми данными.

```java
Joot.seed(dsl)
    .create(AUTHOR, 100)  // 100 авторов
    .create(BOOK, 1000)   // 1000 книг
    .withRelationships()  // Автоматические FK
    .run();
```

**Use case:** Заполнение dev/staging окружений реалистичными данными

---

### 7. Data Anonymization

**Проблема:** Безопасная работа с production данными.

```java
ctx.anonymize(productionData)
    .maskField(USER.EMAIL)      // test-1@example.com
    .maskField(USER.PHONE)      // +1-555-0001
    .preserveRelationships()    // FK остаются валидными
    .export();
```

**Use case:** Тестирование с production-like данными без утечки PII

---

### 8. Schema Evolution Testing

**Проблема:** Тестирование миграций схемы.

```java
ctx.testMigration()
    .from("v1.0_schema.sql")
    .to("v2.0_schema.sql")
    .withData(() -> {
        // Создаём данные в старой схеме
    })
    .assertDataIntegrity()
    .assertNoDataLoss();
```

**Use case:** Проверка Flyway/Liquibase миграций с реальными данными

---

### 9. Multi-tenancy Support

**Проблема:** Тестирование multi-tenant схем.

```java
ctx.forTenant("tenant-1")
    .create(AUTHOR, Author.class).build();

ctx.forTenant("tenant-2")
    .create(AUTHOR, Author.class).build();

assertThat(ctx.forTenant("tenant-1").getAll(AUTHOR, Author.class))
    .hasSize(1); // Изоляция данных
```

**Use case:** SaaS приложения с multi-tenancy

---

### 10. Test Data Versioning

**Проблема:** Git-like управление тестовыми данными.

```java
ctx.commit("initial-state");
// ... изменения ...
ctx.commit("after-update");
// ... ещё изменения ...
ctx.rollback("initial-state");
```

**Use case:** Сложные тестовые сценарии с историей изменений

---

### 11. Visual Test Data Inspector

**Проблема:** Отладка сложных тестов, визуализация данных.

```java
ctx.inspect(); // Открывает web UI с визуализацией
// - Граф зависимостей FK
// - Timeline создания объектов
// - JSON export
```

**Use case:** Документирование тестовых данных, отладка

---

## 💭 Обсуждение

### Наиболее перспективные направления:

1. **Database Change Tracking** - решает реальную проблему, уникально
2. **Test Assertions** (Вариант C) - естественное дополнение к генерации
3. **Test Fixtures** - очень востребовано

### Вопросы для проработки (после v1.0.0):

- Какие направления наиболее востребованы в реальных проектах?
- Что можно реализовать без кодогенерации?
- Какие фичи дадут максимальную ценность пользователям?
- Что отличает Joot от других test data библиотек?

---

## 📝 Примечания

- Все идеи требуют дополнительной проработки
- Приоритеты будут определены после релиза v1.0.0
- Фокус на уникальной ценности и простоте использования
- Избегаем кодогенерации, где возможно (используем jOOQ metadata)

---

**Следующий шаг:** Релиз v1.0.0 (README.md) 🚀

