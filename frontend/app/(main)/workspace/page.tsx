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
      <div className="grid grid-cols-1 md:grid-cols-5 lg:grid-cols-5">
        <div className="hidden lg:block col-span-1">
          {/* Assistant List  */}
          <AssistantList />
        </div>
        <div className="col-span-1 md:col-span-4 lg:col-span-3 flex flex-col">
          {/* Conversation History at top */}
          <div className="border-b bg-white">
            <ConversationList onSelectConversation={handleSelectConversation} />
          </div>
          {/* Chat Ui below */}
          <div className="flex-1">
            <ChatUi />
          </div>
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
