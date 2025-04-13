-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-12
INSERT INTO "Status" ("Status_Name") VALUES
('Admin'),
('User'),
('Blocked');