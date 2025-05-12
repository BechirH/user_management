-- Basic permissions
INSERT INTO permissions (name, description) VALUES
                                                ('USER_READ', 'Read user data'),
                                                ('USER_CREATE', 'Create users'),
                                                ('USER_UPDATE', 'Update users'),
                                                ('USER_DELETE', 'Delete users')
    ON CONFLICT (name) DO NOTHING;