"use client";
import React, { useState, useContext } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Upload, File, Trash2, Loader2 } from "lucide-react";
import { AuthContext } from "@/context/AuthContext";
import { AssistantContext } from "@/context/AssistantContext";
import axios from "axios";

interface Document {
  id: number;
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  assistantId: string;
  createdAt: string;
  updatedAt: string;
}

interface DocumentUploadResponse {
  id?: number;
  fileName?: string;
  originalFileName?: string;
  fileType?: string;
  fileSize?: number;
  message: string;
  success: boolean;
}

const DocumentManager: React.FC = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(false);
  const { user } = useContext(AuthContext);
  const { assistant } = useContext(AssistantContext);

  React.useEffect(() => {
    if (user && assistant) {
      loadDocuments();
    }
  }, [user, assistant]);

  const loadDocuments = async () => {
    if (!user || !assistant) return;

    setLoading(true);
    try {
      const response = await axios.get(
        `http://localhost:8080/api/documents/assistant/${user.id}/${assistant.id}`
      );
      setDocuments(response.data);
    } catch (error) {
      console.error("Error loading documents:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      // Check file size (10MB limit)
      if (file.size > 10 * 1024 * 1024) {
        alert("File size must be less than 10MB");
        return;
      }

      // Check file type
      const supportedTypes = [
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain",
        "text/markdown",
        "application/rtf",
        "text/html",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      ];

      if (!supportedTypes.includes(file.type)) {
        alert(
          "Unsupported file type. Please upload PDF, Word, Text, Markdown, RTF, HTML, or Excel files."
        );
        return;
      }

      setSelectedFile(file);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile || !user || !assistant) return;

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", selectedFile);
      formData.append("userId", user.id);
      formData.append("assistantId", assistant.id.toString());

      const response = await axios.post<DocumentUploadResponse>(
        "http://localhost:8080/api/documents/upload",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      if (response.data.success) {
        alert("Document uploaded successfully!");
        setSelectedFile(null);
        // Reset file input
        const fileInput = document.getElementById(
          "fileInput"
        ) as HTMLInputElement;
        if (fileInput) fileInput.value = "";
        // Reload documents
        loadDocuments();
      } else {
        alert("Upload failed: " + response.data.message);
      }
    } catch (error) {
      console.error("Error uploading document:", error);
      alert("Failed to upload document. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  const handleDeleteDocument = async (documentId: number) => {
    if (!user) return;

    if (!confirm("Are you sure you want to delete this document?")) return;

    try {
      await axios.delete(
        `http://localhost:8080/api/documents/${documentId}?userId=${user.id}`
      );
      alert("Document deleted successfully!");
      loadDocuments();
    } catch (error) {
      console.error("Error deleting document:", error);
      alert("Failed to delete document. Please try again.");
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  const getFileIcon = (fileType: string) => {
    if (fileType.includes("pdf")) return "📄";
    if (fileType.includes("word") || fileType.includes("document")) return "📝";
    if (fileType.includes("excel") || fileType.includes("spreadsheet"))
      return "📊";
    if (fileType.includes("text") || fileType.includes("markdown")) return "📄";
    return "📁";
  };

  if (!user || !assistant) {
    return (
      <div className="p-4 text-center text-gray-500">
        Please select an assistant to manage documents.
      </div>
    );
  }

  return (
    <div className="p-6 bg-white rounded-lg shadow-sm border">
      <h3 className="text-lg font-semibold mb-4">Document Manager</h3>
      <p className="text-sm text-gray-600 mb-4">
        Upload documents to provide context to {assistant.name}. Supported
        formats: PDF, Word, Text, Markdown, RTF, HTML, Excel.
      </p>

      {/* Upload Section */}
      <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 mb-6">
        <div className="text-center">
          <Upload className="mx-auto h-12 w-12 text-gray-400 mb-4" />
          <div className="space-y-2">
            <Input
              id="fileInput"
              type="file"
              onChange={handleFileSelect}
              accept=".pdf,.doc,.docx,.txt,.md,.rtf,.html,.xls,.xlsx"
              className="hidden"
            />
            <label
              htmlFor="fileInput"
              className="cursor-pointer inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
            >
              Choose File
            </label>
            {selectedFile && (
              <div className="mt-2">
                <p className="text-sm text-gray-600 break-words">
                  Selected:{" "}
                  <span className="font-medium" title={selectedFile.name}>
                    {selectedFile.name.length > 50
                      ? `${selectedFile.name.substring(0, 50)}...`
                      : selectedFile.name}
                  </span>{" "}
                  ({formatFileSize(selectedFile.size)})
                </p>
                <Button
                  onClick={handleUpload}
                  disabled={uploading}
                  className="mt-2"
                >
                  {uploading ? (
                    <>
                      <Loader2 className="animate-spin h-4 w-4 mr-2" />
                      Uploading...
                    </>
                  ) : (
                    <>
                      <Upload className="h-4 w-4 mr-2" />
                      Upload Document
                    </>
                  )}
                </Button>
              </div>
            )}
          </div>
          <p className="text-xs text-gray-500 mt-2">Maximum file size: 10MB</p>
        </div>
      </div>

      {/* Documents List */}
      <div>
        <h4 className="font-medium mb-3">Uploaded Documents</h4>
        {loading ? (
          <div className="text-center py-4">
            <Loader2 className="animate-spin h-6 w-6 mx-auto" />
            <p className="text-sm text-gray-500 mt-2">Loading documents...</p>
          </div>
        ) : documents.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <File className="mx-auto h-12 w-12 mb-2 opacity-50" />
            <p>No documents uploaded yet.</p>
            <p className="text-sm">
              Upload documents to provide context to your AI assistant.
            </p>
          </div>
        ) : (
          <div className="space-y-2">
            {documents.map((doc) => (
              <div
                key={doc.id}
                className="flex items-center justify-between p-3 bg-gray-50 rounded-lg gap-3"
              >
                <div className="flex items-center space-x-3 min-w-0 flex-1">
                  <span className="text-2xl flex-shrink-0">
                    {getFileIcon(doc.fileType)}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p
                      className="font-medium text-sm truncate"
                      title={doc.originalFileName}
                    >
                      {doc.originalFileName}
                    </p>
                    <p className="text-xs text-gray-500">
                      {formatFileSize(doc.fileSize)} •{" "}
                      {new Date(doc.createdAt).toLocaleDateString()}
                    </p>
                  </div>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDeleteDocument(doc.id)}
                  className="text-red-600 hover:text-red-700 hover:bg-red-50 flex-shrink-0"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default DocumentManager;
