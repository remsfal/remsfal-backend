-- Add timezone column to appointments table (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'appointments' AND column_name = 'timezone'
    ) THEN
        ALTER TABLE appointments 
        ADD COLUMN timezone VARCHAR(50) NOT NULL DEFAULT 'UTC';
    END IF;
END $$;

-- Add comment for documentation
COMMENT ON COLUMN appointments.timezone IS 'Timezone identifier for all appointment times (e.g., Europe/Berlin, America/New_York, UTC)';

-- Create index for timezone queries (optional, for future analytics)
CREATE INDEX IF NOT EXISTS idx_appointments_timezone ON appointments(timezone);
