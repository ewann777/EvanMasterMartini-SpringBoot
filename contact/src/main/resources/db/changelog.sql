-- liquibase formatted sql

-- changeset martini:contact-messages
CREATE TABLE contact_messages (
    number BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    subject VARCHAR(120) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(12) NOT NULL,
    created_at VARCHAR(30) NOT NULL,
    INDEX messages_user (user_id, number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- rollback DROP TABLE contact_messages;
