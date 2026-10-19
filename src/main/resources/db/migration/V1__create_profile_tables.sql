CREATE TABLE profile_profiles (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    status      VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE profile_publishers (
     id          BIGINT PRIMARY KEY,
     first_name  VARCHAR(255) NOT NULL,
     last_name   VARCHAR(255) NOT NULL,
     niche       VARCHAR(255) NOT NULL,
     bio         TEXT NOT NULL,
     country     VARCHAR(255) NOT NULL,
     CONSTRAINT fk_profile_profiles FOREIGN KEY (id) REFERENCES profile_profiles (id)
);

CREATE TABLE profile_advertisers (
    id            BIGINT PRIMARY KEY,
    company_name  VARCHAR(255) NOT NULL UNIQUE,
    sector        VARCHAR(255) NOT NULL,
    website       VARCHAR(255) NOT NULL,
    CONSTRAINT fk_profile_advertisers FOREIGN KEY (id) REFERENCES profile_profiles (id)
);