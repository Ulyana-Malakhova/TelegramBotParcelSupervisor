-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-4

CREATE TABLE "Role"
(
  "Id_Role" Serial NOT NULL PRIMARY KEY,
  "Name_Role" Character varying(15) NOT NULL UNIQUE
)
WITH (autovacuum_enabled=true);

