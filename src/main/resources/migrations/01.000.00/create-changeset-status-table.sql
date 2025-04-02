-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-1

CREATE TABLE "Status"
(
  "Id_Status" Serial NOT NULL PRIMARY KEY,
  "Status_Name" Character varying(20) NOT NULL UNIQUE
)
WITH (autovacuum_enabled=true);

