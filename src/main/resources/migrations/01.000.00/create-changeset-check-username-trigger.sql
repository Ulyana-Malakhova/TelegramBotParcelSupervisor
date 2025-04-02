-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-10

CREATE OR REPLACE FUNCTION check_username_format()
RETURNS TRIGGER AS '
BEGIN
    IF NEW."Username" IS NOT NULL AND
       (NEW."Username" = '''' OR LEFT(NEW."Username", 1) <> ''@'') THEN
        RAISE EXCEPTION ''Username must start with "@" or be empty'';
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE TRIGGER username_check
BEFORE INSERT OR UPDATE ON "User"
FOR EACH ROW
EXECUTE FUNCTION check_username_format();
