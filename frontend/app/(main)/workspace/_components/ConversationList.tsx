"use client";
import React, { useContext, useEffect, useState } from "react";
import { ConversationService } from "@/services/ConversationService";
import { Conversation } from "@/types/conversation";
import { AuthContext } from "@/context/AuthContext";
import { AssistantContext } from "@/context/AssistantContext";
import { MessageCircle, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";

interface ConversationListProps {
  onSelectConversation: (conversation: Conversation) => void;
}

function ConversationList({ onSelectConversation }: ConversationListProps) {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [loading, setLoading] = useState(false);
  const { user } = useContext(AuthContext);
  const { assistant } = useContext(AssistantContext);

  useEffect(() => {
    if (user && assistant) {
      loadConversations();
    }
  }, [user, assistant]);

  const loadConversations = async () => {
    if (!user || !assistant) return;

    setLoading(true);
    try {
      const userConversations =
        await ConversationService.getUserConversationsByAssistant(
          user.id,
          assistant.id.toString()
        );
      setConversations(userConversations);
    } catch (error) {
      console.error("Error loading conversations:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConversation = async (
    conversationId: number,
    event: React.MouseEvent
  ) => {
    event.stopPropagation(); // Prevent selecting the conversation

    try {
      await ConversationService.deleteConversation(conversationId);
      setConversations((prev) =>
        prev.filter((conv) => conv.id !== conversationId)
      );
    } catch (error) {
      console.error("Error deleting conversation:", error);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (loading) {
    return (
      <div className="p-4 text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto"></div>
        <p className="mt-2 text-sm text-gray-600">Loading conversations...</p>
      </div>
    );
  }

  return (
    <div className="p-4">
      <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
        <MessageCircle className="w-5 h-5" />
        Chat History
      </h3>

      {conversations.length === 0 ? (
        <p className="text-gray-500 text-sm">No previous conversations</p>
      ) : (
        <div className="space-y-2">
          {conversations.map((conversation) => (
            <div
              key={conversation.id}
              onClick={() => onSelectConversation(conversation)}
              className="p-3 border rounded-lg cursor-pointer hover:bg-gray-50 transition-colors group"
            >
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <h4 className="font-medium text-sm truncate">
                    {conversation.title}
                  </h4>
                  <p className="text-xs text-gray-500 mt-1">
                    {conversation.updatedAt &&
                      formatDate(conversation.updatedAt)}
                  </p>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={(e) => handleDeleteConversation(conversation.id!, e)}
                  className="opacity-0 group-hover:opacity-100 transition-opacity p-1 h-auto"
                >
                  <Trash2 className="w-4 h-4 text-red-500" />
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default ConversationList;
