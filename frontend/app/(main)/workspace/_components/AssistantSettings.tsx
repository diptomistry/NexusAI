"use client";
import { AssistantContext } from "@/context/AssistantContext";
import Image from "next/image";
import React, { useContext, useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import AiModelOptions from "@/services/AiModelOptions";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import { Loader2Icon, Save, Trash, Settings, FileText } from "lucide-react";
import { toast } from "sonner";
import ConfirmationAlert from "../ConfirmationAlert";
import { BlurFade } from "@/components/magicui/blur-fade";
import { updateUserAiAssistant, deleteAssistant } from "@/services/database";
import DocumentManager from "./DocumentManager";

function AssistantSettings() {
  const { assistant, setAssistant } = useContext(AssistantContext);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<"settings" | "documents">(
    "settings"
  );

  const onHandleInputChange = (field: string, value: string) => {
    setAssistant((prev: any) => ({
      ...prev,
      [field]: value,
    }));
  };

  const OnSave = async () => {
    setLoading(true);
    try {
      await updateUserAiAssistant(
        assistant?._id,
        assistant?.userInstruction,
        assistant?.aiModelId
      );
      toast("Saved!");
    } catch (error) {
      console.error("Error saving assistant:", error);
      toast("Error saving assistant");
    }
    setLoading(false);
  };

  const OnDelete = async () => {
    console.log("OnDelete");
    setLoading(true);
    try {
      await deleteAssistant(assistant?._id);
      setAssistant(null);
    } catch (error) {
      console.error("Error deleting assistant:", error);
      toast("Error deleting assistant");
    }
    setLoading(false);
  };

  return (
    assistant && (
      <div className="h-screen bg-gray-50 border-l p-5 relative overflow-y-auto">
        {/* Tab Navigation */}
        <div className="flex space-x-1 mb-6 bg-gray-100 p-1 rounded-lg">
          <button
            onClick={() => setActiveTab("settings")}
            className={`flex items-center px-3 py-2 rounded-md text-xs font-medium transition-colors ${
              activeTab === "settings"
                ? "bg-white shadow-sm text-gray-900"
                : "text-gray-500 hover:text-gray-700"
            }`}
          >
            <Settings className="h-4 w-4 mr-2" />
            Settings
          </button>
          <button
            onClick={() => setActiveTab("documents")}
            className={`flex items-center px-3 py-2 rounded-md text-xs font-medium transition-colors ${
              activeTab === "documents"
                ? "bg-white shadow-sm text-gray-900"
                : "text-gray-500 hover:text-gray-700"
            }`}
          >
            <FileText className="h-4 w-4 mr-2" />
            Documents
          </button>
        </div>

        {/* Tab Content */}
        {activeTab === "settings" && (
          <div>
            <BlurFade delay={0.25}>
              <div className="flex gap-3 items-center">
                <Image
                  src={assistant?.image}
                  alt="assistant"
                  width={100}
                  height={100}
                  className="w-[60px] h-[60px] rounded-full object-cover"
                />
                <div>
                  <h2 className="font-bold text-lg">{assistant?.name}</h2>
                  <h2 className="text-gray-500">{assistant?.title}</h2>
                </div>
              </div>
            </BlurFade>
            <BlurFade delay={0.25 * 2}>
              <div className="mt-8">
                <h2 className="text-gray-500">AI Modal:</h2>
                <Select
                  value={assistant?.aiModelId}
                  onValueChange={(value) =>
                    onHandleInputChange("aiModelId", value)
                  }
                >
                  <SelectTrigger className="w-full bg-white">
                    <SelectValue placeholder="Select AI Model" />
                  </SelectTrigger>
                  <SelectContent>
                    {AiModelOptions.map((model, index) => (
                      <SelectItem key={index} value={model.name}>
                        <div className="flex gap-2 items-center">
                          <Image
                            src={model.logo}
                            alt="model"
                            width={20}
                            height={20}
                            className="rounded-md"
                          />
                          <h2>{model.name}</h2>
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </BlurFade>
            <BlurFade delay={0.25 * 3}>
              <div className="mt-4">
                <h2 className="text-gray-500">Instruction:</h2>
                <Textarea
                  placeholder="Add Instruction"
                  className="h-[180px] bg-white"
                  value={assistant?.userInstruction}
                  onChange={(e) =>
                    onHandleInputChange("userInstruction", e.target.value)
                  }
                />
              </div>
            </BlurFade>
            <div className="absolute bottom-10 right-5 flex gap-5">
              <ConfirmationAlert OnDelete={OnDelete}>
                <Button disabled={loading} variant="ghost">
                  <Trash /> Delete
                </Button>
              </ConfirmationAlert>
              <Button onClick={OnSave} disabled={loading}>
                {loading ? <Loader2Icon className="animate-spin" /> : <Save />}{" "}
                Save
              </Button>
            </div>
          </div>
        )}

        {activeTab === "documents" && (
          <div className="pb-20">
            <DocumentManager />
          </div>
        )}
      </div>
    )
  );
}

export default AssistantSettings;
