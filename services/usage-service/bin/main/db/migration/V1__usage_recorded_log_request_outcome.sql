-- Adds outcome columns when upgrading an existing usage_recorded_log table created before
-- request_successful / upstream_status_code existed. Hibernate ddl-auto=update cannot add
-- NOT NULL columns in one step when rows already exist; this migration adds nullable columns,
-- backfills, then enforces NOT NULL on request_successful.
--
-- When the table does not exist yet (empty database), this script is a no-op; Hibernate
-- will create the full schema on startup.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'usage_recorded_log'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'usage_recorded_log'
              AND column_name = 'request_successful'
        ) THEN
            ALTER TABLE usage_recorded_log
                ADD COLUMN request_successful BOOLEAN;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'usage_recorded_log'
              AND column_name = 'upstream_status_code'
        ) THEN
            ALTER TABLE usage_recorded_log
                ADD COLUMN upstream_status_code INTEGER;
        END IF;

        UPDATE usage_recorded_log
        SET request_successful = TRUE
        WHERE request_successful IS NULL;

        ALTER TABLE usage_recorded_log
            ALTER COLUMN request_successful SET NOT NULL;

        ALTER TABLE usage_recorded_log
            ALTER COLUMN request_successful SET DEFAULT TRUE;
    END IF;
END $$;
