-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-3

CREATE TABLE "User"
(
  "Id_User" Bigint NOT NULL PRIMARY KEY,
  "Name" Character varying(64) NOT NULL,
  "Surname" Character varying(64),
  "Username" Character varying(32),
  "Phone_Number" Character varying(20) NOT NULL,
  "Id_Status" Integer NOT NULL,
  "Email" Character varying(30),
  "Password" Character varying(60)
)
WITH (autovacuum_enabled=true);
ALTER TABLE "User"
  ADD CONSTRAINT "Relationship1"
    FOREIGN KEY ("Id_Status")
    REFERENCES "Status" ("Id_Status")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;