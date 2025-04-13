-- liquibase formatted sql

-- changeset pozdnyakova-ea:1-11

CREATE OR REPLACE FUNCTION check_admin_status()
RETURNS TRIGGER AS '
BEGIN
    IF NEW."Id_Status" = 1 THEN
        IF NEW."Email" IS NULL OR NEW."Email" = '''' OR
           NEW."Password" IS NULL OR NEW."Password" = '''' THEN
            RAISE EXCEPTION ''Email and Password must be filled for Admin status'';
        END IF;
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE TRIGGER admin_status_check
BEFORE INSERT OR UPDATE ON "User"
FOR EACH ROW
EXECUTE FUNCTION check_admin_status();
