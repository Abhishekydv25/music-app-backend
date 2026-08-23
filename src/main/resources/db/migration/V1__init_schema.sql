-- ========================================
-- V1: Initial schema for Music App
-- ========================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE artists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    genre VARCHAR(80),
    image_url VARCHAR(500)
);

CREATE TABLE albums (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    artist_id BIGINT NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    cover_image_url VARCHAR(500),
    release_year INT
);

CREATE TABLE songs (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    artist_id BIGINT NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    album_id BIGINT REFERENCES albums(id) ON DELETE SET NULL,
    genre VARCHAR(80),
    duration_sec INT NOT NULL,
    audio_url VARCHAR(500) NOT NULL,
    cover_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE playlists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_public BOOLEAN NOT NULL DEFAULT false,
    cover_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE playlist_songs (
    id BIGSERIAL PRIMARY KEY,
    playlist_id BIGINT NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    song_id BIGINT NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    position INT NOT NULL DEFAULT 0,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (playlist_id, song_id)
);

CREATE TABLE favourites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    song_id BIGINT NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, song_id)
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE device_sync (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id VARCHAR(150) NOT NULL,
    last_synced_at TIMESTAMP NOT NULL DEFAULT now(),
    last_sync_version VARCHAR(100),
    UNIQUE (user_id, device_id)
);

-- Helpful indexes
CREATE INDEX idx_songs_title ON songs (title);
CREATE INDEX idx_songs_artist ON songs (artist_id);
CREATE INDEX idx_playlists_user ON playlists (user_id);
CREATE INDEX idx_favourites_user ON favourites (user_id);
