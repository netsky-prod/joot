-- Test schema for joot framework
-- Simple library domain with author and book tables

-- Table with SERIAL (sequence-based ID) for testing
CREATE TABLE article (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for utility generators testing
CREATE TABLE contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    birth_date DATE,
    registered_at TIMESTAMP NOT NULL
);

CREATE TABLE author (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,  -- UNIQUE constraint for testing
    country VARCHAR(100)
);

CREATE TABLE book (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    author_id UUID NOT NULL REFERENCES author(id),
    isbn VARCHAR(20) UNIQUE,
    pages INTEGER,
    description TEXT
);

-- Tables with circular dependencies for testing
-- Note: Both FKs are NOT NULL to create a true circular dependency
-- In real scenarios, at least one should be nullable or use deferred constraints
CREATE TABLE company (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    ceo_id UUID NOT NULL  -- NOT NULL to create circular dependency
);

CREATE TABLE person (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    company_id UUID NOT NULL REFERENCES company(id)  -- NOT NULL FK
);

-- Add FK from company to person (creates circular dependency)
ALTER TABLE company ADD CONSTRAINT fk_company_ceo 
    FOREIGN KEY (ceo_id) REFERENCES person(id);

-- Table with nullable FK for testing generateNullables behavior
CREATE TABLE product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) NOT NULL,
    product_id UUID REFERENCES product(id),  -- NULLABLE FK
    quantity INTEGER NOT NULL,
    notes TEXT
);

-- Table with nullable self-reference for testing
CREATE TABLE category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    parent_id UUID REFERENCES category(id),  -- NULLABLE self-reference
    description TEXT
);

-- Tables with resolvable circular dependency (has nullable FK)
-- users.default_team_id → team (nullable)
-- team.owner_user_id → users (NOT NULL)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    default_team_id UUID  -- NULLABLE FK to team (will be added after team table)
);

CREATE TABLE team (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    owner_user_id UUID NOT NULL REFERENCES users(id)  -- NOT NULL FK to users
);

-- Add FK from users to team (creates resolvable cycle)
ALTER TABLE users ADD CONSTRAINT fk_users_default_team
    FOREIGN KEY (default_team_id) REFERENCES team(id);

-- Publisher table with UNIQUE constraint for testing
CREATE TABLE publisher (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,  -- UNIQUE constraint
    country VARCHAR(100)
);

-- Test table for multiple FKs to same parent table
CREATE TABLE message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content VARCHAR(500) NOT NULL,
    sender_id UUID NOT NULL REFERENCES users(id),
    receiver_id UUID NOT NULL REFERENCES users(id)
);

-- Test table for column length handling
CREATE TABLE string_length_test (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Very short fields
    tiny_field VARCHAR(3),
    tiny_unique VARCHAR(5) UNIQUE,
    -- Short fields
    short_field VARCHAR(8),
    short_unique VARCHAR(10) UNIQUE,
    -- Medium fields
    medium_field VARCHAR(15),
    medium_unique VARCHAR(20) UNIQUE,
    -- Long fields
    long_field VARCHAR(50),
    long_unique VARCHAR(100) UNIQUE,
    -- Unlimited (H2 doesn't support UNIQUE on TEXT/CLOB)
    text_field TEXT
);

-- Table for testing enum support
CREATE TABLE task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,  -- Will be mapped to enum via jOOQ converter
    priority VARCHAR(10)          -- Will be mapped to enum via jOOQ converter (nullable)
);
