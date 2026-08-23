-- Seed data for local testing.
-- Place at src/main/resources/db/migration/V2__seed_test_data.sql
-- (adjust the version number if V1 is already your baseline schema migration)

INSERT INTO artists (id, name, genre) VALUES
  (1, 'Arijit Singh', 'Pop'),
  (2, 'The Weeknd', 'R&B'),
  (3, 'Coldplay', 'Rock');

INSERT INTO albums (id, title, artist_id, cover_url) VALUES
  (1, 'Aashiqui Hits', 1, NULL),
  (2, 'After Hours', 2, NULL),
  (3, 'Music of the Spheres', 3, NULL);

INSERT INTO songs (id, title, genre, duration_sec, audio_url, cover_image_url, artist_id, album_id) VALUES
  (1, 'Tum Hi Ho', 'Pop', 262, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', NULL, 1, 1),
  (2, 'Channa Mereya', 'Pop', 285, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3', NULL, 1, 1),
  (3, 'Blinding Lights', 'R&B', 200, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3', NULL, 2, 2),
  (4, 'Save Your Tears', 'R&B', 215, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3', NULL, 2, 2),
  (5, 'Higher Power', 'Rock', 251, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3', NULL, 3, 3),
  (6, 'My Universe', 'Rock', 229, 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3', NULL, 3, 3);

-- Reset sequences (Postgres) so future inserts don't collide with seeded IDs
SELECT setval(pg_get_serial_sequence('artists', 'id'), (SELECT MAX(id) FROM artists));
SELECT setval(pg_get_serial_sequence('albums', 'id'), (SELECT MAX(id) FROM albums));
SELECT setval(pg_get_serial_sequence('songs', 'id'), (SELECT MAX(id) FROM songs));
