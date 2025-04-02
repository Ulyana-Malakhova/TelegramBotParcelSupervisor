-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-13
INSERT INTO "Tracking_Status" ("Name_Tracking_Status") VALUES
('Отслеживается'),
('Не отслеживается'),
('Доставлено'),
('Отменено');