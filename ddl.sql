DROP SCHEMA PUBLIC CASCADE;
CREATE SCHEMA PUBLIC;

CREATE TABLE account (
  id BIGSERIAL PRIMARY KEY,
  license CHAR(36) UNIQUE NOT NULL,
  email_address VARCHAR UNIQUE NOT NULL,
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
  laps INT NOT NULL,
  lap_distance INT NOT NULL,
  lap_unit VARCHAR(6) NOT NULL,
  style VARCHAR(9) NOT NULL,
  kickboard BOOLEAN NOT NULL,
  fins BOOLEAN NOT NULL,
  minutes INT NOT NULL,
  seconds INT NOT NULL,
  calories INT NOT NULL,
  datetime BIGINT NOT NULL
);

CREATE TABLE fault (
  cause VARCHAR NOT NULL,
  occurred VARCHAR NOT NULL
);