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
    <div className="p-2 sm:p-3 border-b bg-white">
      {conversations.length === 0 ? (
        <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4">
          <h3 className="text-sm font-medium flex items-center gap-2">
            <MessageCircle className="w-4 h-4" />
            Chat History
          </h3>
          <p className="text-gray-500 text-xs">No previous conversations</p>
        </div>
      ) : (
        <div className="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4">
          <h3 className="text-sm font-medium flex items-center gap-2 flex-shrink-0">
            <MessageCircle className="w-4 h-4" />
            Chat History
          </h3>
          <div className="flex gap-2 overflow-x-auto pb-1 flex-1">
            {conversations.slice(0, 5).map((conversation) => (
              <div
                key={conversation.id}
                onClick={() => onSelectConversation(conversation)}
                className="flex-shrink-0 p-2 border rounded-lg cursor-pointer hover:bg-gray-50 transition-colors group min-w-[140px] sm:min-w-[180px] lg:min-w-[200px] max-w-[180px] sm:max-w-[220px] lg:max-w-[250px]"
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1 min-w-0">
                    <h4
                      className="font-medium text-xs truncate"
                      title={conversation.title}
                    >
                      {conversation.title}
                    </h4>
                    <p className="text-xs text-gray-500 mt-1 hidden sm:block">
                      {conversation.updatedAt &&
                        formatDate(conversation.updatedAt)}
                    </p>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) =>
                      handleDeleteConversation(conversation.id!, e)
                    }
                    className="opacity-0 group-hover:opacity-100 transition-opacity p-1 h-auto ml-1"
                  >
                    <Trash2 className="w-3 h-3 text-red-500" />
                  </Button>
                </div>
              </div>
            ))}
            {conversations.length > 5 && (
              <div className="flex-shrink-0 flex items-center justify-center p-2 text-xs text-gray-500 border rounded-lg min-w-[60px] sm:min-w-[80px]">
                <span className="hidden sm:inline">
                  +{conversations.length - 5} more
                </span>
                <span className="sm:hidden">+{conversations.length - 5}</span>
              </div>
            )}
          </div>
          <span className="text-xs text-gray-500 flex-shrink-0 hidden sm:block">
            {conversations.length} conversation
            {conversations.length !== 1 ? "s" : ""}
          </span>
        </div>
      )}
    </div>
  );
}

export default ConversationList;
