-- Users table
CREATE TABLE users (
    id UUID DEFAULT gen_random_uuid () PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    picture TEXT NOT NULL,
    credits INTEGER DEFAULT 2500,
    order_id TEXT,
    created_at TIMESTAMP
    WITH
        TIME ZONE DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE DEFAULT NOW()
);

-- User AI Assistants table
CREATE TABLE user_ai_assistants (
    id UUID DEFAULT gen_random_uuid () PRIMARY KEY,
    assistant_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    title TEXT NOT NULL,
    image TEXT NOT NULL,
    instruction TEXT NOT NULL,
    user_instruction TEXT NOT NULL,
    sample_questions JSONB,
    ai_model_id TEXT DEFAULT 'Google: Gemini 2.0 Flash',
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP
    WITH
        TIME ZONE DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE DEFAULT NOW()
);

-- Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    file_name TEXT NOT NULL,
    original_file_name TEXT NOT NULL,
    file_type TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    extracted_text TEXT,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    assistant_id TEXT,
    file_path TEXT,
    created_at TIMESTAMP
    WITH
        TIME ZONE DEFAULT NOW(),
        updated_at TIMESTAMP
    WITH
        TIME ZONE DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_user_ai_assistants_user_id ON user_ai_assistants (user_id);

CREATE INDEX idx_documents_user_id ON documents (user_id);

CREATE INDEX idx_documents_assistant_id ON documents (assistant_id);

CREATE INDEX idx_documents_user_assistant ON documents (user_id, assistant_id);

-- Enable Row Level Security (RLS)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

ALTER TABLE user_ai_assistants ENABLE ROW LEVEL SECURITY;

ALTER TABLE documents ENABLE ROW LEVEL SECURITY;

-- Create RLS policies (Optional - you can disable these if you handle auth differently)
-- CREATE POLICY "Users can view their own data" ON users FOR ALL USING (auth.uid()::text = id::text);
-- CREATE POLICY "Users can manage their own assistants" ON user_ai_assistants FOR ALL USING (auth.uid()::text = user_id::text);

ALTER TABLE users DISABLE ROW LEVEL SECURITY;

ALTER TABLE user_ai_assistants DISABLE ROW LEVEL SECURITY;

ALTER TABLE documents DISABLE ROW LEVEL SECURITY;