"use client";
import React, { useState } from "react";
import AssistantList from "./_components/AssistantList";
import AssistantSettings from "./_components/AssistantSettings";
import { AuroraText } from "@/components/magicui/aurora-text";
import { SparklesText } from "@/components/magicui/sparkles-text";
import ChatUi from "./_components/ChatUi";
import ConversationList from "./_components/ConversationList";
import { Conversation } from "@/types/conversation";
import { UploadedImagesProvider } from "@/context/UploadedImagesContext";

function Workspace() {
  const [selectedConversation, setSelectedConversation] =
    useState<Conversation | null>(null);
  const [deletedConversationId, setDeletedConversationId] = useState<
    number | null
  >(null);

  const handleSelectConversation = (conversation: Conversation) => {
    setSelectedConversation(conversation);
    // TODO: Load this conversation in ChatUi
  };

  const handleDeleteConversation = (conversationId: number) => {
    // If the deleted conversation is currently selected, clear it
    if (selectedConversation?.id === conversationId) {
      setSelectedConversation(null);
    }
    // Notify ChatUi about the deletion
    setDeletedConversationId(conversationId);
    // Reset the deletion ID after a short delay to allow ChatUi to process it
    setTimeout(() => setDeletedConversationId(null), 100);
  };

  return (
    <UploadedImagesProvider>
      <div className="h-screen fixed w-full">
        <div className="grid grid-cols-1 md:grid-cols-5 lg:grid-cols-5">
          <div className="hidden lg:block col-span-1">
            {/* Assistant List  */}
            <AssistantList />
          </div>
          <div className="col-span-1 md:col-span-4 lg:col-span-3 flex flex-col">
            {/* Conversation History at top */}
            <div className="border-b bg-card">
              <ConversationList
                onSelectConversation={handleSelectConversation}
                onDeleteConversation={handleDeleteConversation}
              />
            </div>
            {/* Chat Ui below */}
            <div className="flex-1">
              <ChatUi deletedConversationId={deletedConversationId} />
            </div>
          </div>
          <div className="hidden lg:block col-span-1">
            {/* Settings  */}
            <AssistantSettings />
          </div>
        </div>
      </div>
    </UploadedImagesProvider>
  );
}

export default Workspace;
