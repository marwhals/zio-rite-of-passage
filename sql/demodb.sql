CREATE DATABASE demodb;
\c demodb;

CREATE TABLE IF NOT EXISTS JOBS (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    url TEXT NOT NULL,
    company TEXT NOT NULL
);