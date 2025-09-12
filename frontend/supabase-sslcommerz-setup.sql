-- Create payments table for SSLCommerz payment tracking
CREATE TABLE IF NOT EXISTS payments (
    id UUID DEFAULT gen_random_uuid () PRIMARY KEY,
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    transaction_id TEXT UNIQUE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency TEXT DEFAULT 'BDT',
    status TEXT NOT NULL, -- 'pending', 'success', 'failed', 'cancelled'
    payment_method TEXT,
    gateway_response JSONB,
    plan_type TEXT DEFAULT 'pro_plan',
    credits_added INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments (user_id);

CREATE INDEX IF NOT EXISTS idx_payments_transaction_id ON payments (transaction_id);

CREATE INDEX IF NOT EXISTS idx_payments_status ON payments (status);

-- Enable Row Level Security (RLS)
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;

-- Create policy to allow users to view their own payments
CREATE POLICY IF NOT EXISTS "Users can view their own payments" ON payments FOR
SELECT USING (auth.uid () = user_id);

-- Create policy to allow payment service to insert/update payments
CREATE POLICY IF NOT EXISTS "Service can manage payments" ON payments FOR ALL USING (true);

-- Add credits and order_id columns to users table if they don't exist
DO $$ 
BEGIN 
    -- Add credits column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='credits') THEN
        ALTER TABLE users ADD COLUMN credits INTEGER DEFAULT 10000;

END IF;

-- Add order_id column if it doesn't exist
IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE
        table_name = 'users'
        AND column_name = 'order_id'
) THEN
ALTER TABLE users
ADD COLUMN order_id TEXT;

END IF;

END $$;