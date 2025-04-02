-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-7

CREATE TABLE "Message"
(
  "Id_Message" Serial NOT NULL PRIMARY KEY,
  "Text_Message" Character varying(4096) NOT NULL,
  "Date_Message" Timestamp NOT NULL,
  "Id_User" Bigint NOT NULL
)
WITH (
  autovacuum_enabled=true)
;

ALTER TABLE "Message"
  ADD CONSTRAINT "Relationship6"
    FOREIGN KEY ("Id_User")
    REFERENCES "User" ("Id_User")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;
