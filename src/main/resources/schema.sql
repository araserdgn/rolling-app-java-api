DROP TABLE IF EXISTS votes;
DROP TABLE IF EXISTS choices;
DROP TABLE IF EXISTS polls;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- Roles tablosu
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL
);

-- Users tablosu
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    username VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(40) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Roles tablosu
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Polls tablosu
CREATE TABLE IF NOT EXISTS polls (
    id BIGSERIAL PRIMARY KEY,
    question VARCHAR(140) NOT NULL,
    expiration_date_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    status VARCHAR(15) DEFAULT 'ACTIVE',
    FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE
);

-- Choices tablosu
CREATE TABLE IF NOT EXISTS choices (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(40) NOT NULL,
    poll_id BIGINT NOT NULL,
    FOREIGN KEY (poll_id) REFERENCES polls (id) ON DELETE CASCADE
);

-- Votes tablosu
CREATE TABLE IF NOT EXISTS votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    poll_id BIGINT NOT NULL,
    choice_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (poll_id) REFERENCES polls (id) ON DELETE CASCADE,
    FOREIGN KEY (choice_id) REFERENCES choices (id) ON DELETE CASCADE,
    UNIQUE (user_id, poll_id)
); 