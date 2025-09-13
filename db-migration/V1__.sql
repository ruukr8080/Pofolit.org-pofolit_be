CREATE TABLE users
(
    id                BINARY(16) NOT NULL,
    email             VARCHAR(255),
    nickname          VARCHAR(255),
    profile_image_url VARCHAR(355),
    birth_day         date,
    job               VARCHAR(255),
    domain            VARCHAR(255),
    provider_id       VARCHAR(255),
    registration_id   VARCHAR(255),
    role              VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uk_user_provider UNIQUE ();