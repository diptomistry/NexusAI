"use client";
import React, { useContext, useEffect, useRef, useState } from "react";
import EmptyChatState from "./EmptyChatState";
import { AssistantContext } from "@/context/AssistantContext";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Loader2Icon, Send, Upload, Download } from "lucide-react";
import axios from "axios";
import AiModelOptions from "@/services/AiModelOptions";
import Image from "next/image";
import Markdown from "react-markdown";
import { AuthContext } from "@/context/AuthContext";
import { ASSISTANT } from "../../ai-assistants/page";
import { updateUserTokens } from "@/services/database";
import { ConversationService } from "@/services/ConversationService";
import { Conversation, ConversationMessage } from "@/types/conversation";
import { useUploadedImages } from "@/context/UploadedImagesContext";
import { supabase } from "@/lib/supabase";
import { calculateTokenCost } from "@/lib/tokenCosts";
import VideoGenerator from "./VideoGenerator";
import { toast } from "sonner";

type MESSAGE = {
  role: string;
  content: string;
  images?: string[];
};

interface ChatUiProps {
  deletedConversationId?: number | null;
}

function ChatUi({ deletedConversationId }: ChatUiProps) {
  const [input, setInput] = useState<string>("");
  const { assistant, setAssistant } = useContext(AssistantContext);
  const [messages, setMessages] = useState<MESSAGE[]>([]);
  const [loading, setLoading] = useState(false);
  const chatRef = useRef<any>(null);
  const { user, setUser } = useContext(AuthContext);
  const inputRef = useRef<HTMLInputElement>(null);
  const [currentConversation, setCurrentConversation] =
    useState<Conversation | null>(null);
  const { uploadedUrl, setUploadedUrl, setLatestGeneratedImages } =
    useUploadedImages();
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (chatRef.current) {
      chatRef.current.scrollTop = chatRef.current.scrollHeight;
    }
  }, [messages]);

  useEffect(() => {
    setMessages([]);
    setCurrentConversation(null);
    // When assistant changes, create a new conversation or load existing ones
    if (assistant && user) {
      loadOrCreateConversation();
    }
  }, [assistant?.id]);

  // Handle external conversation deletion
  useEffect(() => {
    if (
      deletedConversationId &&
      currentConversation?.id === deletedConversationId
    ) {
      // The current conversation was deleted, clear the UI
      setCurrentConversation(null);
      setMessages([]);

      // Reload conversations to show the next available one (if any)
      setTimeout(() => {
        loadOrCreateConversation();
      }, 100);
    }
  }, [deletedConversationId, currentConversation?.id]);

  // Listen for logout cleanup event
  useEffect(() => {
    const handleLogoutCleanup = () => {
      // Clear all chat-related state on logout
      setMessages([]);
      setCurrentConversation(null);
      setLoading(false);
    };

    window.addEventListener("logout-cleanup", handleLogoutCleanup);
    return () =>
      window.removeEventListener("logout-cleanup", handleLogoutCleanup);
  }, []);

  const loadOrCreateConversation = async () => {
    if (!assistant || !user) return;

    try {
      // Try to get existing conversations for this assistant
      const conversations =
        await ConversationService.getUserConversationsByAssistant(
          user.id,
          assistant.id.toString()
        );

      if (conversations.length > 0) {
        // Load the most recent conversation
        const latestConversation = conversations[0];
        const conversationWithMessages =
          await ConversationService.getConversationWithMessages(
            latestConversation.id!
          );
        setCurrentConversation(conversationWithMessages);

        // Convert conversation messages to MESSAGE format
        if (conversationWithMessages.messages) {
          console.log(
            "Loading conversation messages:",
            conversationWithMessages.messages
          );
          const convertedMessages = conversationWithMessages.messages.map(
            (msg) => {
              // Extract images from content if they exist
              let images: string[] | undefined = undefined;
              if (msg.images) {
                try {
                  images = JSON.parse(msg.images);
                  console.log(`Parsed images for ${msg.role} message:`, images);
                } catch (e) {
                  console.error("Error parsing images from conversation:", e);
                }
              }

              // Fallback: extract images from content if not stored separately
              // This applies to both user and assistant messages
              if (!images && msg.content) {
                const imageUrlRegex =
                  /https?:\/\/[^\s]+\.(jpg|jpeg|png|gif|webp|bmp|tiff|svg)(\?[^\s]*)?/gi;
                const matches = msg.content.match(imageUrlRegex);
                if (matches) {
                  images = matches;
                  console.log(
                    `Extracted images from content for ${msg.role} message:`,
                    images
                  );
                }
              }

              return {
                role: msg.role,
                content: msg.content,
                images: images,
              };
            }
          );
          setMessages(convertedMessages);
          console.log("Converted messages with images:", convertedMessages);

          // Set latest generated images and uploaded image for Image editor
          if (assistant?.name === "Image editor") {
            // Set latest generated images from the last assistant message
            const lastAssistantMessage = convertedMessages
              .filter((msg) => msg.role === "assistant" && msg.images)
              .pop();
            if (lastAssistantMessage?.images) {
              setLatestGeneratedImages(lastAssistantMessage.images);
            }

            // Set uploaded image from the last user message
            const lastUserMessage = convertedMessages
              .filter((msg) => msg.role === "user" && msg.images)
              .pop();
            console.log("Last user message with images:", lastUserMessage);
            if (lastUserMessage?.images && lastUserMessage.images.length > 0) {
              console.log(
                "Restoring uploaded image from conversation:",
                lastUserMessage.images[0]
              );
              setUploadedUrl(lastUserMessage.images[0]);
            } else {
              console.log("No user message with images found");
            }
          }
        }
      }
    } catch (error) {
      console.error("Error loading conversation:", error);
      // If there's an error, just continue without loading previous messages
    }
  };

  const createNewConversation = async (): Promise<Conversation | null> => {
    if (!assistant || !user) return null;

    try {
      const conversation = await ConversationService.createConversation({
        userId: user.id,
        assistantId: assistant.id.toString(),
        title: `Chat with ${assistant.name}`,
      });
      setCurrentConversation(conversation);
      return conversation;
    } catch (error) {
      console.error("Error creating conversation:", error);
      return null;
    }
  };

  const onSendMessage = async (inputSuggestion?: string) => {
    // Check if user has enough credits
    if (user?.credits <= 0) {
      toast.error("Insufficient tokens! Please buy more tokens to continue.");
      return;
    }

    setLoading(true);
    let userInput = inputSuggestion ?? input;

    // For Image editor, append the uploaded image URL to the input if available
    if (assistant?.name === "Image editor" && uploadedUrl) {
      userInput = `${userInput} ${uploadedUrl}`;
    }

    setMessages((prev) => [
      ...prev,
      {
        role: "user",
        content: userInput,
        images: uploadedUrl ? [uploadedUrl] : undefined,
      },
      {
        role: "assistant",
        content: "Loading...",
      },
    ]);

    inputRef.current?.focus(); // Keep focus on input
    setInput("");

    // Ensure we have a conversation before saving messages
    let conversationToUse = currentConversation;
    if (!conversationToUse) {
      conversationToUse = await createNewConversation();
      if (!conversationToUse) {
        setLoading(false);
        return;
      }
    }

    // Save user message to conversation
    try {
      await ConversationService.addMessage({
        conversationId: conversationToUse.id!,
        role: "user",
        content: userInput,
        images: uploadedUrl ? JSON.stringify([uploadedUrl]) : undefined,
      });
    } catch (error) {
      console.error("Error saving user message:", error);
    }

    try {
      const AIModel = AiModelOptions.find(
        (item) => item.name == assistant.aiModelId
      );

      let result;

      // Use the new Spring Boot AI endpoint for all models
      console.log(
        "Using Spring Boot AI Service for provider:",
        AIModel?.replicateModel
      );
      result = await axios.post("http://localhost:8080/api/ai/chat", {
        provider: AIModel?.replicateModel,
        userInput: userInput,
        aiResp: messages[messages?.length - 1]?.content,
        assistantInstruction: assistant?.userInstruction,
        userId: user?.id,
        assistantId: assistant?.id?.toString(),
      });

      setLoading(false);
      setMessages((prev) => prev.slice(0, -1));

      // Check if we got a valid response
      if (result.data) {
        // Extract image URLs from the response for Image editor
        let generatedImages: string[] = [];
        if (assistant?.name === "Image editor" && result.data.content) {
          const imageUrlRegex =
            /https?:\/\/[^\s]+\.(jpg|jpeg|png|gif|webp|bmp|tiff|svg)(\?[^\s]*)?/gi;
          const matches = result.data.content.match(imageUrlRegex);
          if (matches) {
            generatedImages = matches;
            setLatestGeneratedImages(generatedImages);
          }
        }

        setMessages((prev) => [
          ...prev,
          {
            ...result.data,
            images: generatedImages.length > 0 ? generatedImages : undefined,
          },
        ]);
        updateUserToken(result.data?.content);

        // Save assistant response to conversation
        if (conversationToUse?.id) {
          try {
            await ConversationService.addMessage({
              conversationId: conversationToUse.id,
              role: "assistant",
              content: result.data.content,
              images:
                generatedImages.length > 0
                  ? JSON.stringify(generatedImages)
                  : undefined,
            });
          } catch (error) {
            console.error("Error saving assistant message:", error);
          }
        }
      } else {
        // Handle case where API didn't return expected data
        const errorMessage =
          "Sorry, I encountered an error processing your request.";
        setMessages((prev) => [
          ...prev,
          {
            role: "assistant",
            content: errorMessage,
          },
        ]);
        // Save error message to conversation
        if (conversationToUse?.id) {
          try {
            await ConversationService.addMessage({
              conversationId: conversationToUse.id,
              role: "assistant",
              content: errorMessage,
            });
          } catch (error) {
            console.error("Error saving error message:", error);
          }
        }
      }
    } catch (error) {
      console.error("Error calling AI API:", error);
      setLoading(false);
      setMessages((prev) => prev.slice(0, -1));
      const errorMessage =
        "Sorry, I encountered an error processing your request. Please try again.";
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: errorMessage,
        },
      ]);
      // Save error message to conversation
      if (conversationToUse?.id) {
        try {
          await ConversationService.addMessage({
            conversationId: conversationToUse.id,
            role: "assistant",
            content: errorMessage,
          });
        } catch (error) {
          console.error("Error saving error message:", error);
        }
      }
    }
  };

  const uploadFileToSupabase = async (file: File): Promise<string | null> => {
    try {
      const fileExt = file.name.split(".").pop();
      const fileName = `${Date.now()}-${Math.random()
        .toString(36)
        .substring(2)}.${fileExt}`;
      const filePath = `${user?.id}/inputs/${fileName}`;

      console.log("[Upload] Uploading file:", {
        bucket: "images",
        path: filePath,
        name: file.name,
        type: file.type,
        size: file.size,
      });

      const { data, error } = await supabase.storage
        .from("images")
        .upload(filePath, file);

      if (error) {
        console.error("[Upload] Upload failed:", error);
        throw error;
      }

      console.log("[Upload] Upload successful:", data);

      const {
        data: { publicUrl },
      } = supabase.storage.from("images").getPublicUrl(data.path);

      console.log("[Upload] Public URL:", publicUrl);
      return publicUrl;
    } catch (error) {
      console.error("[Upload] Error details:", error);
      return null;
    }
  };

  const downloadImage = async (imageUrl: string, filename: string) => {
    try {
      const response = await fetch(imageUrl);
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error("Download failed:", error);
    }
  };

  const updateUserToken = async (resp: string | undefined) => {
    // Handle undefined or null response
    if (!resp || typeof resp !== "string") {
      console.log("No response content to count tokens for");
      return;
    }

    // Use the sophisticated token costing system
    const tokenCount = calculateTokenCost(assistant?.name || "default", resp);
    const isImageGeneration = assistant?.name === "Image editor";
    const isVideoGeneration = assistant?.name === "Video generator";

    console.log(
      `${
        isImageGeneration
          ? "Image generation"
          : isVideoGeneration
          ? "Video generation"
          : "Text response"
      } - charging ${tokenCount} tokens`
    );

    // Only update if user exists and has credits
    if (!user?.id || !user?.credits) {
      console.log("No user or credits available");
      return;
    }

    //Update User Token
    try {
      await updateUserTokens(user.id, user.credits - tokenCount);

      setUser((prev: any) => ({
        ...prev,
        credits: user.credits - tokenCount,
      }));
    } catch (error) {
      console.error("Error updating tokens:", error);
    }
  };
  return (
    <div
      className={`mt-2 p-6 relative ${
        assistant?.name === "Image editor" ? "h-[85vh]" : "h-[88vh]"
      }`}
    >
      {messages?.length == 0 && (
        <EmptyChatState
          sendMessage={(input: string) => {
            onSendMessage(input);
          }}
        />
      )}

      <div
        ref={chatRef}
        className={`overflow-scroll scrollbar-hide ${
          assistant?.name === "Image editor" ? "h-[65vh]" : "h-[74vh]"
        }`}
      >
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`flex mb-2 ${
              msg.role == "user" ? "justify-end" : "justify-start"
            }`}
          >
            <div className="flex gap-3">
              {msg.role == "assistant" && (
                <Image
                  src={assistant?.image}
                  alt="assistant"
                  width={100}
                  height={100}
                  className="w-[30px] h-[30px] rounded-full object-cover"
                />
              )}
              <div
                className={`p-3 rounded-lg gap-2
                                ${
                                  msg.role == "user"
                                    ? "user-message rounded-lg"
                                    : "assistant-message"
                                }
                                `}
              >
                {loading && messages?.length - 1 == index && (
                  <Loader2Icon className="animate-spin" />
                )}

                {/* Display images if they exist */}
                {msg.images && msg.images.length > 0 && (
                  <div className="mb-2 flex flex-wrap gap-2">
                    {msg.images.map((imageUrl, imgIndex) => (
                      <div key={imgIndex} className="relative group">
                        <Image
                          src={imageUrl}
                          alt={`${
                            msg.role === "user" ? "Uploaded" : "Generated"
                          } image ${imgIndex + 1}`}
                          width={400}
                          height={400}
                          className="rounded-lg object-cover max-w-[400px] max-h-[400px] w-full"
                        />
                        <button
                          onClick={() =>
                            downloadImage(
                              imageUrl,
                              `${
                                msg.role === "user" ? "uploaded" : "generated"
                              }-image-${Date.now()}-${imgIndex}.jpg`
                            )
                          }
                          className="absolute top-2 right-2 bg-black bg-opacity-50 text-white p-1 rounded opacity-0 group-hover:opacity-100 transition-opacity"
                        >
                          <Download size={16} />
                        </button>
                      </div>
                    ))}
                  </div>
                )}

                {/* Show content only if it's not just image URLs */}
                {(() => {
                  // Filter out image URLs from the content
                  const imageUrlRegex =
                    /https?:\/\/[^\s]+\.(jpg|jpeg|png|gif|webp|bmp|tiff|svg)(\?[^\s]*)?/gi;
                  let cleanContent = msg.content;

                  // Remove image URLs from assistant messages
                  if (msg.role === "assistant") {
                    cleanContent = msg.content
                      .replace(imageUrlRegex, "")
                      .trim();
                  }

                  // For Image editor, also remove image URLs from user messages
                  if (
                    assistant?.name === "Image editor" &&
                    msg.role === "user"
                  ) {
                    cleanContent = msg.content
                      .replace(imageUrlRegex, "")
                      .trim();
                  }

                  // Only show content if there's something meaningful after removing URLs
                  if (cleanContent && cleanContent.length > 0) {
                    return <Markdown>{cleanContent}</Markdown>;
                  }
                  return null;
                })()}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Specialized UI for Image editor */}
      {assistant?.name === "Image editor" ? (
        <div className="absolute bottom-5 w-[94%] space-y-3">
          {/* File upload area */}
          <div className="flex gap-3">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={async (e) => {
                const file = e.target.files?.[0];
                if (file) {
                  const url = await uploadFileToSupabase(file);
                  if (url) {
                    setUploadedUrl(url);
                  }
                }
              }}
              className="hidden"
            />
            <Button
              onClick={() => fileInputRef.current?.click()}
              variant="outline"
              className="flex items-center gap-2"
            >
              <Upload size={16} />
              Upload Image (Optional)
            </Button>
            {uploadedUrl && (
              <div className="flex items-center gap-2 text-sm text-secondary">
                <Image
                  src={uploadedUrl}
                  alt="Uploaded"
                  width={24}
                  height={24}
                  className="rounded"
                />
                Image uploaded
              </div>
            )}
          </div>

          {/* Input area */}
          <div className="flex justify-between gap-5">
            <Input
              ref={inputRef}
              placeholder="Describe the image you want to generate or edit..."
              value={input}
              disabled={loading}
              onChange={(event) => setInput(event.target.value)}
              onKeyPress={(e) => e.key == "Enter" && onSendMessage()}
            />
            <Button disabled={loading} onClick={() => onSendMessage()}>
              <Send />
            </Button>
          </div>

          {!uploadedUrl && (
            <p className="text-sm text-muted text-center">
              💡 Generate new images from text or upload an image to edit it
            </p>
          )}

          {/* Token Cost Indicator for Image Editor */}
          <div className="text-xs text-gray-600 text-center">
            Cost: 1000 tokens per image generation
          </div>
        </div>
      ) : assistant?.name === "Video generator" ? (
        <div className="absolute top-5 bottom-5 w-[94%] overflow-y-auto">
          <VideoGenerator />
        </div>
      ) : (
        <div
          className="flex justify-between p-5 gap-5 
              absolute bottom-5 w-[94%]"
        >
          <Input
            ref={inputRef}
            placeholder="Start Typing here..."
            value={input}
            disabled={loading}
            onChange={(event) => setInput(event.target.value)}
            onKeyPress={(e) => e.key == "Enter" && onSendMessage()}
          />
          <Button disabled={loading} onClick={() => onSendMessage()}>
            <Send />
          </Button>
        </div>
      )}
    </div>
  );
}

export default ChatUi;
