DROP SCHEMA PUBLIC CASCADE;
CREATE SCHEMA PUBLIC;

CREATE TABLE account (
  id BIGSERIAL PRIMARY KEY,
  license CHAR(36) UNIQUE NOT NULL,
  email_address VARCHAR NOT NULL,
  pin CHAR(7) NOT NULL,
  activated BIGINT NOT NULL,
  deactivated BIGINT NOT NULL
);

CREATE TABLE walker (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT REFERENCES account(id),
  name VARCHAR(24) NOT NULL
);

CREATE TABLE session (
  id BIGSERIAL PRIMARY KEY,
  walker_id BIGINT REFERENCES walker(id),
  weight INT NOT NULL,
  weight_unit CHAR(2) NOT NULL,
  distance INT NOT NULL,
  distance_unit CHAR(2) NOT NULL,
  hours INT NOT NULL,
  minutes INT NOT NULL,
  calories INT NOT NULL,
  datetime BIGINT NOT NULL
);

CREATE TABLE fault (
  cause VARCHAR NOT NULL,
  occurred VARCHAR NOT NULL
);