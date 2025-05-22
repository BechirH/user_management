-- Insert basic, role, and permission management permissions
INSERT INTO permissions (name, description) VALUES
                                                -- Basic user permissions
                                                ('USER_READ', 'Read user data'),
                                                ('USER_CREATE', 'Create users'),
                                                ('USER_UPDATE', 'Update users'),
                                                ('USER_DELETE', 'Delete users'),
                                                ('ADMIN_ROOT', 'Root admin'),

                                                -- Role management permissions
                                                ('ROLE_CREATE', 'Create roles'),
                                                ('ROLE_READ', 'Read roles'),
                                                ('ROLE_UPDATE', 'Update roles'),
                                                ('ROLE_DELETE', 'Delete roles'),

                                                -- Permission management permissions
                                                ('PERMISSION_CREATE', 'Create permissions'),
                                                ('PERMISSION_READ', 'Read permissions'),
                                                ('PERMISSION_UPDATE', 'Update permissions'),
                                                ('PERMISSION_DELETE', 'Delete permissions')
    ON CONFLICT (name) DO NOTHING;