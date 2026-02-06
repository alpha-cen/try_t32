-- Database initialization script
-- This will be run automatically when the PostgreSQL container starts

-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for users table
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create user_addresses table if it doesn't exist
CREATE TABLE IF NOT EXISTS user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    address_type VARCHAR(50) DEFAULT 'BOTH',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for user_addresses table
CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id ON user_addresses(user_id);
CREATE INDEX IF NOT EXISTS idx_user_addresses_is_default ON user_addresses(is_default);
CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id_default ON user_addresses(user_id, is_default);

-- Create trigger for user_addresses table
DROP TRIGGER IF EXISTS update_user_addresses_updated_at ON user_addresses;
CREATE TRIGGER update_user_addresses_updated_at 
    BEFORE UPDATE ON user_addresses 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data (optional - for testing)
INSERT INTO users (username, email, password_hash, first_name, last_name, phone, role)
VALUES 
    ('admin', 'admin@example.com', '$2a$10$rJZ7dZQHPZsHGIcHm.kQJ.UF6mWwL2xH8J9fT5jS6QG3Pq1Xt7Kqu', 'Admin', 'User', '+1234567890', 'ADMIN'),
    ('testuser', 'test@example.com', '$2a$10$rJZ7dZQHPZsHGIcHm.kQJ.UF6mWwL2xH8J9fT5jS6QG3Pq1Xt7Kqu', 'Test', 'User', '+1234567891', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Insert sample address data (optional - for testing)
INSERT INTO user_addresses (user_id, address_line1, address_line2, city, state, postal_code, country, is_default, address_type)
SELECT 
    u.id,
    '123 Main Street',
    'Apt 4B',
    'San Francisco',
    'California',
    '94102',
    'USA',
    TRUE,
    'BOTH'
FROM users u
WHERE u.username = 'testuser'
AND NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = u.id);

-- Note: The password_hash above is BCrypt hash of 'admin123'
-- In production, you should create users through the API
