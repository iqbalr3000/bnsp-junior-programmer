-- password asli untuk akun dev/demo ini: admin123 (hash BCrypt di bawah, jangan pakai di production)
INSERT INTO staff (username, password_hash, nama, role)
VALUES ('admin', '$2a$10$45HkTUc4z9yZNHaNK2NDZ.SYWIKgyWAZBs0p5o7QNjcEu2nZ1KRJC', 'Administrator', 'ADMIN');
