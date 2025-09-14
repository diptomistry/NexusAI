-- Enable pgvector extension for vector operations
CREATE EXTENSION IF NOT EXISTS vector;

-- Create document_chunks table with advanced indexing
CREATE TABLE document_chunks (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_text TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_tokens INTEGER NOT NULL,
    embedding vector(1536), -- OpenAI Ada-002 embedding dimension
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assistant_id TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

-- Constraints
CONSTRAINT chunk_index_positive CHECK (chunk_index >= 0),
    CONSTRAINT chunk_tokens_positive CHECK (chunk_tokens > 0),
    CONSTRAINT chunk_text_not_empty CHECK (char_length(chunk_text) > 0)
);

-- Create optimized indexes for vector similarity search
CREATE INDEX idx_document_chunks_embedding ON document_chunks USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- Create composite indexes for efficient filtering
CREATE INDEX idx_document_chunks_user_assistant ON document_chunks (user_id, assistant_id);

CREATE INDEX idx_document_chunks_document_id ON document_chunks (document_id);

CREATE INDEX idx_document_chunks_created_at ON document_chunks (created_at DESC);

-- Create function for similarity search with advanced filtering
CREATE OR REPLACE FUNCTION match_document_chunks(
    query_embedding vector(1536),
    match_threshold float DEFAULT 0.7,
    match_count int DEFAULT 10,
    filter_user_id uuid DEFAULT NULL,
    filter_assistant_id text DEFAULT NULL
)
RETURNS TABLE (
    id uuid,
    document_id bigint,
    chunk_text text,
    chunk_index integer,
    chunk_tokens integer,
    similarity float
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        dc.id,
        dc.document_id,
        dc.chunk_text,
        dc.chunk_index,
        dc.chunk_tokens,
        (dc.embedding <=> query_embedding) * -1 + 1 AS similarity
    FROM document_chunks dc
    WHERE 
        (filter_user_id IS NULL OR dc.user_id = filter_user_id)
        AND (filter_assistant_id IS NULL OR dc.assistant_id = filter_assistant_id)
        AND (dc.embedding <=> query_embedding) < (1 - match_threshold)
    ORDER BY dc.embedding <=> query_embedding
    LIMIT match_count;
END;
$$;

-- Create performance monitoring view
CREATE VIEW chunk_statistics AS
SELECT
    COUNT(*) as total_chunks,
    AVG(chunk_tokens) as avg_tokens_per_chunk,
    MIN(chunk_tokens) as min_tokens,
    MAX(chunk_tokens) as max_tokens,
    COUNT(DISTINCT document_id) as documents_with_chunks,
    COUNT(DISTINCT user_id) as users_with_chunks
FROM document_chunks;

-- Add vector embedding column to existing documents table for document-level embeddings
ALTER TABLE documents
ADD COLUMN IF NOT EXISTS summary_embedding vector (1536);

-- Create index for document-level embeddings
CREATE INDEX idx_documents_summary_embedding ON documents USING ivfflat (
    summary_embedding vector_cosine_ops
)
WITH (lists = 50);

-- Create trigger to update document updated_at when chunks are modified
CREATE OR REPLACE FUNCTION update_document_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE documents 
    SET updated_at = NOW() 
    WHERE id = COALESCE(NEW.document_id, OLD.document_id);
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_document_timestamp
    AFTER INSERT OR UPDATE OR DELETE ON document_chunks
    FOR EACH ROW
    EXECUTE FUNCTION update_document_timestamp();