-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-2

CREATE TABLE "Tracking_Status"
(
  "Id_Tracking_Status" Serial NOT NULL PRIMARY KEY,
  "Name_Tracking_Status" Character varying(20) NOT NULL UNIQUE
)
WITH (autovacuum_enabled=true);
