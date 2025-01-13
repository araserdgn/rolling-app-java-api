-- Rolleri ekle
INSERT INTO roles(name) VALUES('ROLE_USER');
INSERT INTO roles(name) VALUES('ROLE_ADMIN');

-- Test kullanıcısı ekle (şifre: 123123)
INSERT INTO users(name, username, email, password) 
VALUES('Test User', 'admin', 'admin@example.com', '$2a$10$ay/vf0Gkr.8BJ2dG0TMTVOlr4cb9.1i2mRxjDmVUEL0c9bCRAvgSu');

-- Test kullanıcısına USER rolü ver
INSERT INTO user_roles(user_id, role_id) 
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_USER'; 