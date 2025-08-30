CREATE TABLE users
(
    id                BINARY(16)   NOT NULL,
    email             VARCHAR(255) NULL,
    nickname          VARCHAR(255) NULL,
    profile_image_url VARCHAR(355) NULL,
    provider_id       VARCHAR(255) NULL,
    registration_id   VARCHAR(255) NULL,
    birth_day         date NULL,
    job               VARCHAR(255) NULL,
    domain            VARCHAR(255) NULL,
    `role`            VARCHAR(255) NULL,
    refresh_token     VARCHAR(255) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uk_user_provider UNIQUE (provider_id);