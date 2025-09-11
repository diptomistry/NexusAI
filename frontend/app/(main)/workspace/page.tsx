"use client";
import React, { useState } from "react";
import AssistantList from "./_components/AssistantList";
import AssistantSettings from "./_components/AssistantSettings";
import { AuroraText } from "@/components/magicui/aurora-text";
import { SparklesText } from "@/components/magicui/sparkles-text";
import ChatUi from "./_components/ChatUi";
import ConversationList from "./_components/ConversationList";
import { Conversation } from "@/types/conversation";

function Workspace() {
  const [selectedConversation, setSelectedConversation] =
    useState<Conversation | null>(null);

  const handleSelectConversation = (conversation: Conversation) => {
    setSelectedConversation(conversation);
    // TODO: Load this conversation in ChatUi
  };

  return (
    <div className="h-screen fixed w-full">
      <div className="grid md:grid-cols-6 lg:grid-cols-6">
        <div className="hidden lg:block col-span-1">
          {/* Assistant List  */}
          <AssistantList />
        </div>
        <div className="hidden lg:block col-span-1">
          {/* Conversation History  */}
          <ConversationList onSelectConversation={handleSelectConversation} />
        </div>
        <div className="md:col-span-4 lg:col-span-3">
          {/* Chat Ui  */}
          <ChatUi />
        </div>
        <div className="hidden lg:block col-span-1">
          {/* Settings  */}
          <AssistantSettings />
        </div>
      </div>
    </div>
  );
}

export default Workspace;
