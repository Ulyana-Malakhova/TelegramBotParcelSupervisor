-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-8

CREATE TABLE "Group"
(
  "Id_Group" Serial NOT NULL PRIMARY KEY,
  "Name_Group" Character varying(100) NOT NULL,
  "Id_User" Bigint NOT NULL
)
WITH (autovacuum_enabled=true);

ALTER TABLE "Group"
  ADD CONSTRAINT "Relationship7"
    FOREIGN KEY ("Id_User")
    REFERENCES "User" ("Id_User")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;
