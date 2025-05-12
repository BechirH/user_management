-- Default role
INSERT INTO roles (name) VALUES
    ('ROLE_USER')
    ON CONFLICT (name) DO NOTHING;

-- Assign permissions to ROLE_USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' AND p.name IN ('USER_READ', 'USER_UPDATE')
    ON CONFLICT (role_id, permission_id) DO NOTHING;