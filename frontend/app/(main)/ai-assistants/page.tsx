"use client";
import { BlurFade } from "@/components/magicui/blur-fade";
import { RainbowButton } from "@/components/magicui/rainbow-button";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { AuthContext } from "@/context/AuthContext";
import { AssistantContext } from "@/context/AssistantContext";
import AiAssistantsList from "@/services/AiAssistantsList";
import { Loader, Loader2Icon } from "lucide-react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useContext, useEffect, useState } from "react";
import {
  getAllUserAssistants,
  insertSelectedAssistants,
} from "@/services/database";
import AddNewAssistant from "../workspace/_components/AddNewAssistant";

export type ASSISTANT = {
  id: number;
  name: string;
  title: string;
  image: string;
  instruction: string;
  userInstruction: string;
  sampleQuestions: string[];
  aiModelId?: string;
  _id?: string; // Supabase UUID for unique identification
};
function AIAssistants() {
  const [selectedAssistan, setSelectedAssistant] = useState<ASSISTANT[]>([]);
  const { user } = useContext(AuthContext);
  const { setAssistant } = useContext(AssistantContext);
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  useEffect(() => {
    // Only check for existing assistants if user is logged in
    if (user) {
      GetUserAssistants();

      // Check for pending assistant data after sign-in
      const pendingAssistant = localStorage.getItem("pendingAssistant");
      if (pendingAssistant) {
        // If there's pending data, the AddNewAssistant component will handle it
        // We don't need to do anything here as the modal will open automatically
      }
    }
  }, [user]);

  const GetUserAssistants = async () => {
    try {
      const result = await getAllUserAssistants(user.id);
      console.log(result);
      if (result.length > 0) {
        // Navigate to New Screen
        router.replace("/workspace");
        return;
      }
    } catch (error) {
      console.error("Error fetching assistants:", error);
    }
  };

  const onSelect = (assistant: ASSISTANT) => {
    const item = selectedAssistan.find(
      (item: ASSISTANT) => item.id == assistant.id
    );
    if (item) {
      setSelectedAssistant(
        selectedAssistan.filter((item: ASSISTANT) => item.id !== assistant.id)
      );
      return;
    }
    setSelectedAssistant((prev) => [...prev, assistant]);
  };

  const IsAssistantSelected = (assistant: ASSISTANT) => {
    const item = selectedAssistan.find(
      (item: ASSISTANT) => item.id == assistant.id
    );
    return item ? true : false;
  };

  const OnClickContinue = async () => {
    // Check if user is authenticated
    if (!user) {
      router.push("/sign-in");
      return;
    }

    setLoading(true);
    try {
      const result = await insertSelectedAssistants(selectedAssistan, user.id);
      setLoading(false);
      router.replace("/workspace");
      console.log(result);
    } catch (error) {
      console.error("Error inserting assistants:", error);
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-slate-900 dark:via-slate-800 dark:to-indigo-900">
      {/* Hero Section */}
      <div className="relative overflow-hidden">
        <div className="absolute inset-0">
          <div className="absolute top-20 left-10 w-72 h-72 bg-gradient-to-r from-blue-400/20 to-purple-400/20 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute bottom-20 right-10 w-96 h-96 bg-gradient-to-r from-pink-400/20 to-orange-400/20 rounded-full blur-3xl animate-pulse delay-1000"></div>
        </div>

        <div className="relative container mx-auto px-4 sm:px-6 lg:px-8 pt-20 pb-16">
          <div className="text-center max-w-4xl mx-auto">
            <BlurFade delay={0.25} inView>
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold tracking-tight mb-6">
                <span className="bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 bg-clip-text text-transparent">
                  Choose Your AI Companions
                </span>
              </h1>
            </BlurFade>
            <BlurFade delay={0.25 * 2} inView>
              <p className="text-xl md:text-2xl text-slate-600 dark:text-slate-300 mb-8 max-w-3xl mx-auto leading-relaxed">
                Select the AI assistants that will help you accomplish your
                goals. Each one is specialized in different areas to make your
                life easier.
              </p>
            </BlurFade>

            {/* Selection Counter & Action Buttons */}
            <BlurFade delay={0.25 * 3} inView>
              <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-12">
                <div className="bg-white/80 backdrop-blur-sm rounded-full px-6 py-3 border border-blue-200/50 shadow-lg">
                  <span className="text-lg font-semibold text-slate-700">
                    {selectedAssistan.length} selected
                  </span>
                </div>
                <AddNewAssistant>
                  <Button
                    variant="outline"
                    className="px-8 py-4 text-lg font-semibold border-2 border-purple-500 text-purple-600 hover:bg-purple-50 hover:border-purple-600 transition-all duration-300 transform hover:scale-105 shadow-lg"
                  >
                    <svg
                      className="w-5 h-5 mr-2"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M12 6v6m0 0v6m0-6h6m-6 0H6"
                      />
                    </svg>
                    Create Your Own AI Assistant
                  </Button>
                </AddNewAssistant>
                <RainbowButton
                  disabled={selectedAssistan?.length == 0 || loading}
                  onClick={OnClickContinue}
                  className="px-8 py-4 text-lg font-semibold shadow-2xl hover:shadow-3xl transition-all duration-300 transform hover:scale-105"
                >
                  {loading && <Loader2Icon className="animate-spin mr-2" />}
                  {user ? "Continue" : "Sign In to Continue"}
                </RainbowButton>
              </div>
            </BlurFade>
          </div>
        </div>
      </div>

      {/* AI Assistants Grid */}
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 pb-20">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 max-w-7xl mx-auto">
          {AiAssistantsList.map((assistant, index) => (
            <BlurFade
              key={`assistant-${assistant.id}-${index}`}
              delay={0.25 + index * 0.05}
              inView
            >
              <div
                className={`group relative bg-white/80 dark:bg-slate-800/80 backdrop-blur-sm rounded-2xl p-6 border-2 transition-all duration-300 cursor-pointer hover:scale-105 hover:shadow-2xl ${
                  IsAssistantSelected(assistant)
                    ? "border-blue-500 shadow-blue-200/50 shadow-xl bg-blue-50/50 dark:bg-blue-900/20"
                    : "border-slate-200 dark:border-slate-700 hover:border-blue-300 dark:hover:border-blue-600"
                }`}
                onClick={() => onSelect(assistant)}
              >
                {/* Selection Indicator */}
                <div className="absolute top-4 right-4 z-10">
                  <div
                    className={`w-7 h-7 rounded-full border-2 flex items-center justify-center transition-all duration-300 shadow-lg ${
                      IsAssistantSelected(assistant)
                        ? "bg-gradient-to-r from-blue-500 to-purple-500 border-blue-500 scale-110 shadow-blue-500/30"
                        : "border-slate-300 dark:border-slate-600 group-hover:border-blue-400 group-hover:scale-105 bg-white/90 dark:bg-slate-800/90"
                    }`}
                  >
                    {IsAssistantSelected(assistant) ? (
                      <svg
                        className="w-4 h-4 text-white drop-shadow-sm"
                        fill="currentColor"
                        viewBox="0 0 20 20"
                      >
                        <path
                          fillRule="evenodd"
                          d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                          clipRule="evenodd"
                        />
                      </svg>
                    ) : (
                      <div className="w-2 h-2 rounded-full bg-slate-400 dark:bg-slate-500 opacity-60 group-hover:opacity-100 transition-opacity duration-200"></div>
                    )}
                  </div>
                </div>

                {/* Assistant Image */}
                <div className="relative mb-4 overflow-hidden rounded-xl">
                  <Image
                    src={assistant.image}
                    alt={assistant.title}
                    width={300}
                    height={200}
                    className="w-full h-48 object-cover transition-transform duration-300 group-hover:scale-110"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                </div>

                {/* Assistant Info */}
                <div className="text-center">
                  <h3 className="text-xl font-bold text-slate-900 dark:text-white mb-2 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                    {assistant.name}
                  </h3>
                  <p className="text-slate-600 dark:text-slate-300 text-sm font-medium mb-3">
                    {assistant.title}
                  </p>

                  {/* Sample Question Preview */}
                  {assistant.sampleQuestions &&
                    assistant.sampleQuestions.length > 0 && (
                      <div className="text-xs text-slate-500 dark:text-slate-400 italic">
                        "{assistant.sampleQuestions[0].substring(0, 50)}..."
                      </div>
                    )}
                </div>

                {/* Hover Effect Overlay */}
                <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-blue-500/10 to-purple-500/10 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none"></div>
              </div>
            </BlurFade>
          ))}
        </div>

        {/* Bottom CTA */}
        {selectedAssistan.length > 0 && (
          <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-50">
            <div className="bg-white/90 dark:bg-slate-800/90 backdrop-blur-sm rounded-full px-8 py-4 shadow-2xl border border-blue-200/50">
              <div className="flex items-center gap-4">
                <span className="text-slate-700 dark:text-slate-300 font-medium">
                  {selectedAssistan.length} assistant
                  {selectedAssistan.length > 1 ? "s" : ""} selected
                </span>
                <RainbowButton
                  disabled={loading}
                  onClick={OnClickContinue}
                  className="px-6 py-2 text-sm font-semibold"
                >
                  {loading && <Loader2Icon className="animate-spin mr-2" />}
                  {user ? "Continue" : "Sign In to Continue"}
                </RainbowButton>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default AIAssistants;
