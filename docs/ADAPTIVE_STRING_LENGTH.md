# Adaptive String Length Generation

## Overview

Joot automatically adapts generated string values to fit column length constraints. This prevents database errors when creating test data and ensures that generated values never exceed the defined column width.

## Problem

Without adaptive generation, a library might generate strings like `"generated_a3f9x7k2"` (17 characters) for a `VARCHAR(10)` column, causing database errors like:

```
ERROR: value too long for type character varying(10)
```

## Solution

Joot analyzes the column's `DataType.length()` metadata and generates strings that fit within the constraint:

```java
// For VARCHAR(5)
ctx.create(TABLE, Pojo.class).build();
// Generates: "a1", "b2", "c3"

// For VARCHAR(20)
ctx.create(TABLE, Pojo.class).build();
// Generates: "unq_1", "gen_a3f9"
```

## Generation Strategy

Joot uses different string formats based on column length:

| Column Length | Format (Regular)           | Format (UNIQUE)            | Example Regular | Example UNIQUE |
|---------------|----------------------------|----------------------------|-----------------|----------------|
| ≤ 5 chars     | `<prefix><random>`         | `<prefix><counter>`        | `"a42"`, `"g17"` | `"a1"`, `"b2"` |
| ≤ 10 chars    | `"g<random>"`              | `"u<counter>"`             | `"g1234"`       | `"u1"`, `"u2"` |
| ≤ 20 chars    | `"gen_<uuid>"`             | `"unq_<counter>"`          | `"gen_a3f9"`    | `"unq_1"`      |
| ≤ 100 chars   | `"generated_<uuid>"`       | `"unq_<counter>_<uuid>"`   | `"generated_a3f9x7k2"` | `"unq_1_a3f9"` |
| Unlimited     | `"generated_<uuid>"`       | `"unq_<counter>_<uuid>"`   | `"generated_a3f9x7k2"` | `"unq_1_a3f9"` |

**Important:**
- All generated strings are **truncated** to fit the exact column length if needed
- For UNIQUE fields, a counter ensures guaranteed uniqueness
- For very short fields, rotating prefixes (`a`-`z`) provide variety

## Usage Examples

### Very Short Fields (1-5 chars)

```java
// Schema: tiny_field VARCHAR(3)
StringLengthTest entity = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

// Generated: "a42" (fits in 3 chars, truncated if needed)
entity.getTinyField(); // → "a42"
```

### Short Unique Fields (6-10 chars)

```java
// Schema: short_unique VARCHAR(10) UNIQUE
StringLengthTest entity1 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();
StringLengthTest entity2 = ctx.create(STRING_LENGTH_TEST, StringLengthTest.class).build();

entity1.getShortUnique(); // → "u1"
entity2.getShortUnique(); // → "u2"
```

### Medium Fields (11-20 chars)

```java
// Schema: isbn VARCHAR(20) UNIQUE
Book book1 = ctx.create(BOOK, Book.class).build();
Book book2 = ctx.create(BOOK, Book.class).build();

book1.getIsbn(); // → "unq_1"
book2.getIsbn(); // → "unq_2"
```

### Long Fields (21-100 chars)

```java
// Schema: name VARCHAR(255)
Author author = ctx.create(AUTHOR, Author.class).build();

author.getName(); // → "generated_a3f9x7k2"
```

### Unlimited Fields (TEXT, CLOB)

```java
// Schema: description TEXT
Book book = ctx.create(BOOK, Book.class).build();

book.getDescription(); // → "generated_a3f9x7k2"
```

## Foreign Key Auto-Creation

Adaptive length handling also applies to **auto-created parent entities**:

```java
// Schema: book.author_id → author(id)
// author.email VARCHAR(255) UNIQUE ← Adaptive generation here!

Book book1 = ctx.create(BOOK, Book.class).build();
Book book2 = ctx.create(BOOK, Book.class).build();

// Each book auto-creates a unique author with unique email
// No database errors, even if email has UNIQUE constraint!
```

## Explicit Values

You can always override automatic generation with explicit values:

```java
ctx.create(STRING_LENGTH_TEST, StringLengthTest.class)
    .set(STRING_LENGTH_TEST.TINY_FIELD, "xyz")  // Custom value
    .set(STRING_LENGTH_TEST.SHORT_UNIQUE, "custom")  // Custom unique value
    .build();
```

**Warning:** Joot does **not** validate explicit values. If you provide a value longer than the column length, the database will reject it.

## Implementation Details

### Prefix Rotation (Very Short Fields)

For fields ≤5 characters, Joot uses a rotating prefix to provide variety:

```java
private static final char[] PREFIX_CHARS = {
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w',
    'x', 'y', 'z'
}; // 23 chars (excluded 'l', 'o', 'u' to avoid confusion with numbers)
```

This ensures:
- **Variety**: `"a1"`, `"b2"`, `"c3"`, ..., `"a11"`, `"b12"`, ...
- **Collision avoidance**: Different prefixes reduce the chance of accidental duplicates in non-unique fields

### Truncation

All generated strings are truncated to fit:

```java
String result = "generated_" + uuid;
return result.length() > maxLength ? result.substring(0, maxLength) : result;
```

This ensures **no database errors**, even for edge cases.

### Counter for UNIQUE Fields

UNIQUE fields use an `AtomicLong` counter to guarantee uniqueness across concurrent test execution:

```java
private static final AtomicLong UNIQUE_COUNTER = new AtomicLong(1);

// For UNIQUE field
long counter = UNIQUE_COUNTER.getAndIncrement();
String result = "u" + counter; // "u1", "u2", "u3", ...
```

## Limitations

1. **H2 Limitation**: H2 database does **not** support UNIQUE constraints on `TEXT` or `CLOB` columns. Use `VARCHAR` with explicit length for unique text fields in tests.

2. **Counter Overflow**: For very short UNIQUE fields (e.g., `VARCHAR(3) UNIQUE`), the counter will eventually overflow (e.g., `"a999"` → 4 chars). In practice, this is rarely an issue for test data.

3. **No Semantic Meaning**: Generated strings are random and have no semantic meaning. Use explicit values via `.set()` if you need readable test data.

## Testing

Comprehensive tests in `StringLengthAdaptiveTest.java` verify:

- ✅ Very short fields (3, 5 chars)
- ✅ Short fields (8, 10 chars)
- ✅ Medium fields (15, 20 chars)
- ✅ Long fields (50, 100 chars)
- ✅ Unlimited fields (TEXT)
- ✅ UNIQUE constraint handling
- ✅ Multiple entity creation without collisions
- ✅ Explicit value override

All tests ensure generated values **never exceed column length** and **no database errors occur**.

## Benefits

1. **No Configuration Required**: Works automatically based on schema metadata
2. **No Database Errors**: Generated values always fit column constraints
3. **UNIQUE Support**: Counter-based generation ensures no collisions
4. **Performance**: Minimal overhead, no database queries for length detection
5. **Predictable**: Consistent format for each length category
6. **Safe**: Truncation ensures edge cases are handled gracefully

## Comparison with Other Approaches

| Approach | Pros | Cons |
|----------|------|------|
| **Fixed-length strings** | Simple | Fails on short columns |
| **Random strings** | Unique | May exceed column length |
| **Database queries** | Accurate | Slow, requires DB roundtrip |
| **Joot (Adaptive)** | ✅ Fast, ✅ Safe, ✅ No errors | Requires schema metadata |

## Related Documentation

- [Foreign Key Auto-Creation](FK_AUTO_CREATION.md)
- [UNIQUE Constraints](UNIQUE_CONSTRAINTS.md)
- [Value Generation](VALUE_GENERATION.md) _(coming in Phase 5)_

