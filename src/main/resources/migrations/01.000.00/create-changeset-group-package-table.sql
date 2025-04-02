-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-9

CREATE TABLE "Group_Package"
(
  "Id_Package" Integer NOT NULL,
  "Id_Group" Integer NOT NULL
)
WITH (autovacuum_enabled=true);

ALTER TABLE "Group_Package"
  ADD CONSTRAINT "Relationship8"
    FOREIGN KEY ("Id_Package")
    REFERENCES "Package" ("Id_Package")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;

ALTER TABLE "Group_Package"
  ADD CONSTRAINT "Relationship9"
    FOREIGN KEY ("Id_Group")
    REFERENCES "Group" ("Id_Group")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;

