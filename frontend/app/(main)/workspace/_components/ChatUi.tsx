"use client";
import React, { useContext, useEffect, useRef, useState } from "react";
import EmptyChatState from "./EmptyChatState";
import { AssistantContext } from "@/context/AssistantContext";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Loader2Icon, Send } from "lucide-react";
import axios from "axios";
import AiModelOptions from "@/services/AiModelOptions";
import Image from "next/image";
import Markdown from "react-markdown";
import { AuthContext } from "@/context/AuthContext";
import { ASSISTANT } from "../../ai-assistants/page";
import { updateUserTokens } from "@/services/database";
import { ConversationService } from "@/services/ConversationService";
import { Conversation, ConversationMessage } from "@/types/conversation";
type MESSAGE = {
  role: string;
  content: string;
};
function ChatUi() {
  const [input, setInput] = useState<string>("");
  const { assistant, setAssistant } = useContext(AssistantContext);
  const [messages, setMessages] = useState<MESSAGE[]>([]);
  const [loading, setLoading] = useState(false);
  const chatRef = useRef<any>(null);
  const { user, setUser } = useContext(AuthContext);
  const inputRef = useRef<HTMLInputElement>(null);
  const [currentConversation, setCurrentConversation] =
    useState<Conversation | null>(null);

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
          const convertedMessages = conversationWithMessages.messages.map(
            (msg) => ({
              role: msg.role,
              content: msg.content,
            })
          );
          setMessages(convertedMessages);
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

  const saveMessageToConversation = async (
    role: "user" | "assistant",
    content: string
  ) => {
    if (!currentConversation && role === "user") {
      // Create new conversation for the first user message
      const newConversation = await createNewConversation();
      if (!newConversation) return;
    }

    if (currentConversation) {
      try {
        await ConversationService.addMessage({
          conversationId: currentConversation.id!,
          role,
          content,
        });
      } catch (error) {
        console.error("Error saving message:", error);
        // Continue without saving to avoid blocking the chat
      }
    }
  };

  const onSendMessage = async (inputSuggestion?: string) => {
    setLoading(true);
    const userInput = inputSuggestion ?? input;

    setMessages((prev) => [
      ...prev,
      {
        role: "user",
        content: userInput,
      },
      {
        role: "assistant",
        content: "Loading...",
      },
    ]);

    inputRef.current?.focus(); // Keep focus on input
    setInput("");

    // Save user message to conversation
    await saveMessageToConversation("user", userInput);

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
      });

      setLoading(false);
      setMessages((prev) => prev.slice(0, -1));

      // Check if we got a valid response
      if (result.data) {
        setMessages((prev) => [...prev, result.data]);
        updateUserToken(result.data?.content);

        // Save assistant response to conversation
        await saveMessageToConversation("assistant", result.data.content);
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
        await saveMessageToConversation("assistant", errorMessage);
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
      await saveMessageToConversation("assistant", errorMessage);
    }
  };

  const updateUserToken = async (resp: string | undefined) => {
    // Handle undefined or null response
    if (!resp || typeof resp !== "string") {
      console.log("No response content to count tokens for");
      return;
    }

    const tokenCount = resp.trim() ? resp.trim().split(/\s+/).length : 0;
    console.log(tokenCount);

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
    <div className="mt-20 p-6 relative h-[88vh]">
      {messages?.length == 0 && (
        <EmptyChatState
          sendMessage={(input: string) => {
            onSendMessage(input);
          }}
        />
      )}

      <div ref={chatRef} className="h-[74vh] overflow-scroll scrollbar-hide">
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
                                    ? "bg-gray-200 text-black rounded-lg"
                                    : "bg-gray-50 text-black"
                                }
                                `}
              >
                {loading && messages?.length - 1 == index && (
                  <Loader2Icon className="animate-spin" />
                )}
                {/* <h2>{msg.content}</h2> */}
                <Markdown>{msg.content}</Markdown>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div
        className="flex justify-between p-5 gap-5 
            absolute bottom-5 w-[94%]"
      >
        <Input
          ref={inputRef}
          placeholder="Start Typing here..."
          value={input}
          disabled={loading || user?.credits <= 0}
          onChange={(event) => setInput(event.target.value)}
          onKeyPress={(e) => e.key == "Enter" && onSendMessage()}
        />
        <Button
          disabled={loading || user?.credits <= 0}
          onClick={() => onSendMessage()}
        >
          <Send />
        </Button>
      </div>
    </div>
  );
}

export default ChatUi;
