-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-5

CREATE TABLE "Package"
(
  "Id_Package" Serial NOT NULL PRIMARY KEY,
  "Track_Number" Character varying(20) NOT NULL,
  "Name_Package" Character varying(50),
  "Departure_Date" Date,
  "Receipt_Date" Date,
  "Id" Bigint NOT NULL,
  "Id_Role" Integer NOT NULL,
  "Id_Tracking_Status" Integer NOT NULL,
  "Latest_Status" Character varying(200)
)
WITH (autovacuum_enabled=true);

ALTER TABLE "Package"
  ADD CONSTRAINT "Relationship3"
    FOREIGN KEY ("Id")
    REFERENCES "User" ("Id_User")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;

ALTER TABLE "Package"
  ADD CONSTRAINT "Relationship4"
    FOREIGN KEY ("Id_Role")
    REFERENCES "Role" ("Id_Role")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;

ALTER TABLE "Package"
  ADD CONSTRAINT "Relationship5"
    FOREIGN KEY ("Id_Tracking_Status")
    REFERENCES "Tracking_Status" ("Id_Tracking_Status")
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
;