INSERT INTO users
(created_at, email, identification_number, last_update, name, password, permissions, role)
VALUES
    (CURRENT_TIMESTAMP, 'yo@example.com', '123456789', CURRENT_TIMESTAMP, 'yo', '$2a$10$NDBI0ByDVhcYX77afxnbrOmlpPGb9bdM5C1g6ZPdpsGGWEfVHwvIK', 'MANAGE_USERS', 'ADMIN');
