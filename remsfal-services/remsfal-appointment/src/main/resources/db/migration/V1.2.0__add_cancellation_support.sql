-- Add cancellation reason support to appointments table
-- Version 1.2.0

-- Add cancellation_reason column to store optional reason for cancellation (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'appointments' AND column_name = 'cancellation_reason'
    ) THEN
        ALTER TABLE appointments
        ADD COLUMN cancellation_reason VARCHAR(500);
    END IF;
END $$;

COMMENT ON COLUMN appointments.cancellation_reason IS 'Optional reason provided when cancelling an appointment';
