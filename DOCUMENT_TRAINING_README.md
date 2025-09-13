# Document-Based AI Training Feature

## Overview

The NexusAI platform now supports training AI assistants with uploaded documents in addition to instruction-based training. This feature allows users to upload various document types (PDF, Word, Excel, Text, etc.) and have the AI assistant use the content of these documents to provide more contextual and informed responses.

## Features

### Document Support

- **File Types**: PDF, Word (.doc, .docx), Excel (.xls, .xlsx), Text (.txt), Markdown (.md), RTF, HTML
- **File Size Limit**: 10MB per document
- **Text Extraction**: Automatic text extraction using Apache Tika
- **Storage**: Documents are stored securely with extracted text for quick retrieval

### AI Integration

- **Contextual Responses**: AI assistants now include relevant document content in their responses
- **Multi-Provider Support**: Works with all supported AI providers (OpenAI, Anthropic, Gemini, DeepSeek, Mistral)
- **Intelligent Context**: The system automatically includes relevant document context based on user queries

### User Interface

- **Tabbed Interface**: Settings panel now includes both "Settings" and "Documents" tabs
- **Drag & Drop Upload**: Easy document upload with visual feedback
- **Document Management**: View, list, and delete uploaded documents
- **File Information**: See file size, type, and upload date for each document

## Technical Implementation

### Backend Components

#### 1. Document Entity (`Document.java`)

- Stores document metadata and extracted text
- Links documents to users and specific assistants
- Tracks file information and timestamps

#### 2. Document Service (`DocumentService.java`)

- Handles file upload and text extraction
- Manages document storage and retrieval
- Provides relevant context for AI queries

#### 3. Document Controller (`DocumentController.java`)

- REST API endpoints for document operations
- File upload, listing, and deletion endpoints
- Context retrieval for AI integration

#### 4. Enhanced AI Service (`AiChatService.java`)

- Modified to include document context in AI prompts
- Works with all AI providers (Replicate, Gemini)
- Intelligent context building based on uploaded documents

### Frontend Components

#### 1. Document Manager (`DocumentManager.tsx`)

- React component for document upload and management
- File validation and upload progress
- Document listing with delete functionality

#### 2. Enhanced Assistant Settings (`AssistantSettings.tsx`)

- Tabbed interface for settings and documents
- Integrated document management
- Responsive design with proper error handling

#### 3. Updated Chat UI (`ChatUi.tsx`)

- Modified to send user and assistant IDs with requests
- Automatic document context inclusion
- Seamless integration with existing chat flow

### Database Schema

```sql
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
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

## Usage Guide

### For Users

1. **Upload Documents**:

   - Navigate to any assistant in your workspace
   - Click on the "Documents" tab in the settings panel
   - Drag and drop files or click "Choose File"
   - Wait for upload confirmation

2. **Chat with Document Context**:

   - Start chatting with your assistant as usual
   - The AI will automatically reference uploaded documents when relevant
   - Ask specific questions about your documents for detailed responses

3. **Manage Documents**:
   - View all uploaded documents in the Documents tab
   - See file information (name, size, upload date)
   - Delete documents that are no longer needed

### For Developers

1. **API Endpoints**:

   ```
   POST /api/documents/upload - Upload a new document
   GET /api/documents/user/{userId} - Get all user documents
   GET /api/documents/assistant/{userId}/{assistantId} - Get assistant-specific documents
   DELETE /api/documents/{documentId} - Delete a document
   GET /api/documents/context - Get document context for AI queries
   ```

2. **AI Integration**:
   - The AiChatService automatically retrieves document context
   - Context is included in prompts for all AI providers
   - No additional configuration needed for basic functionality

## Configuration

### Backend Configuration (`application.properties`)

```properties
# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
```

### Dependencies Added

```xml
<!-- Apache Tika for text extraction -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-parsers-standard-package</artifactId>
    <version>2.9.1</version>
</dependency>

<!-- File upload support -->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.5</version>
</dependency>

<!-- Text processing utilities -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>1.10.0</version>
</dependency>
```

## Security Considerations

- **File Validation**: Only supported file types are accepted
- **Size Limits**: 10MB maximum file size to prevent abuse
- **User Isolation**: Documents are linked to specific users and assistants
- **Text Length Limits**: Extracted text is truncated to prevent memory issues
- **Secure Storage**: Files are stored outside the web root

## Limitations

- **File Size**: Maximum 10MB per document
- **Text Length**: Extracted text is limited to 50,000 characters
- **File Types**: Only common document formats are supported
- **Context Window**: Document context is included based on AI model limits

## Future Enhancements

- **Semantic Search**: Implement vector embeddings for better document relevance
- **Document Chunking**: Split large documents into smaller, more manageable chunks
- **File Versioning**: Support for document updates and version history
- **Collaborative Documents**: Share documents across multiple assistants
- **Advanced Search**: Full-text search within uploaded documents

## Troubleshooting

### Common Issues

1. **Upload Fails**:

   - Check file size (must be < 10MB)
   - Verify file type is supported
   - Ensure backend is running and accessible

2. **Document Context Not Working**:

   - Verify documents are uploaded successfully
   - Check that user and assistant IDs are being sent correctly
   - Review backend logs for any errors

3. **Performance Issues**:
   - Large documents may slow down AI responses
   - Consider the total size of uploaded documents per assistant
   - Monitor memory usage on the backend

### Error Messages

- `"File size too large"`: File exceeds 10MB limit
- `"Unsupported file type"`: File type not in supported list
- `"Upload failed"`: General upload error, check backend logs
- `"Document not found"`: Document may have been deleted or doesn't exist

## Support

For technical support or feature requests, please:

1. Check the troubleshooting section above
2. Review backend logs for detailed error messages
3. Ensure all dependencies are properly installed
4. Verify database schema is up to date

This feature significantly enhances the AI assistant capabilities by providing document-based context, making responses more accurate and relevant to user-specific information.
