# Data Access API

**Фаза 6.1** - Реализовано в рамках Joot Framework

## Описание

Удобный метод для чтения созданных тестовых данных без необходимости писать jOOQ запросы вручную.

## API

### `ctx.get()` - получить сущность по PK

```java
Author author = ctx.get(authorId, AUTHOR, Author.class);
```

**Параметры:**
- `primaryKey` - значение первичного ключа
- `table` - jOOQ Table объект
- `pojoClass` - класс POJO для маппинга

**Возвращает:** 
- Сущность или `null` если не найдена

## Примеры использования

### Получение FK сущности

```java
// Создаём книгу (автоматически создаётся автор)
Book book = ctx.create(BOOK, Book.class).build();

// Получаем автора по FK
Author author = ctx.get(book.getAuthorId(), AUTHOR, Author.class);

assertThat(author).isNotNull();
assertThat(author.getName()).isNotNull();
```

### Работа с несуществующими сущностями

```java
// Попытка получить несуществующую сущность
UUID nonExistentId = UUID.randomUUID();
Author author = ctx.get(nonExistentId, AUTHOR, Author.class);

// Возвращает null, а не выбрасывает exception
assertThat(author).isNull();
```

## Реализация

- **Файлы:**
  - `JootContext.java` - интерфейс с методом `get()`
  - `JootContextImpl.java` - реализация через jOOQ DSL

- **Тесты:** 
  - `DataAccessTest.java` - тесты для `ctx.get()`

## Технические детали

- Используется jOOQ DSL для выполнения запросов
- Автоматическое определение PK через `table.getPrimaryKey()`
- Поддержка любых таблиц с определённым primary key
- Thread-safe (использует DSLContext который thread-safe)

## Ограничения

- Работает только с таблицами имеющими **единственный PK** (composite keys не поддерживаются в текущей версии)
- Требуется чтобы таблица имела определённый primary key

