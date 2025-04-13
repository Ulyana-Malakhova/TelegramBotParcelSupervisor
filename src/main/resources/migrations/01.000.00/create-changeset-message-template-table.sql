-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-6

CREATE TABLE "Message_Template"
(
  "Id_Template" Serial NOT NULL PRIMARY KEY,
  "Text_Message_Template" Character varying(200) NOT NULL UNIQUE,
  "Last_Edit_Date" Date NOT NULL,
  "Event" Character varying(100) NOT NULL UNIQUE,
  "Author_Last_Edit" Bigint
)
WITH (autovacuum_enabled=true);

ALTER TABLE "Message_Template"
  ADD CONSTRAINT "Relationship2"
    FOREIGN KEY ("Author_Last_Edit")
    REFERENCES "User" ("Id_User")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;