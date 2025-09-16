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
import { X } from "lucide-react";
import {
  Loader2Icon,
  Save,
  Trash,
  Settings,
  FileText,
  Download,
} from "lucide-react";
import { toast } from "sonner";
import ConfirmationAlert from "../ConfirmationAlert";
import { BlurFade } from "@/components/magicui/blur-fade";
import { updateUserAiAssistant, deleteAssistant } from "@/services/database";
import DocumentManager from "./DocumentManager";
import { useUploadedImages } from "@/context/UploadedImagesContext";

function AssistantSettings() {
  const { assistant, setAssistant } = useContext(AssistantContext);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<"settings" | "documents">(
    "settings"
  );
  const {
    uploadedUrl,
    latestGeneratedImages,
    setUploadedUrl,
    clearUploadedUrl,
  } = useUploadedImages();

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
        {/* Tab Navigation - Hide for Image editor */}
        {assistant?.name !== "Image editor" && (
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
        )}

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
                  value={
                    assistant?.aiModelId ||
                    (assistant?.name === "Image editor"
                      ? "Flux Kontext Max (Text Effects)"
                      : "Google: Gemini 2.0 Flash")
                  }
                  onValueChange={(value) =>
                    onHandleInputChange("aiModelId", value)
                  }
                >
                  <SelectTrigger className="w-full bg-white">
                    <SelectValue placeholder="Select AI Model" />
                  </SelectTrigger>
                  <SelectContent>
                    {AiModelOptions.filter((model) =>
                      assistant?.name === "Image editor"
                        ? model.imageOnly
                        : !model.imageOnly
                    ).map((model, index) => (
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

            {/* Image editor specific UI */}
            {assistant?.name === "Image editor" ? (
              <>
                {/* Reference Image Section */}
                {uploadedUrl && (
                  <BlurFade delay={0.25 * 3}>
                    <div className="mt-6">
                      <div className="flex justify-between items-center mb-2">
                        <h2 className="text-gray-500">Reference Image:</h2>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            console.log("Clearing uploaded URL");
                            clearUploadedUrl();
                          }}
                          className="h-6 w-6 p-0 hover:bg-red-100 hover:text-red-600"
                        >
                          <X size={16} />
                        </Button>
                      </div>
                      <div className="w-full">
                        <Image
                          src={uploadedUrl}
                          alt="Reference"
                          width={300}
                          height={300}
                          className="rounded-lg object-cover w-full h-48"
                        />
                      </div>
                    </div>
                  </BlurFade>
                )}

                {/* Latest Generated Images Section */}
                {latestGeneratedImages.length > 0 && (
                  <BlurFade delay={0.25 * 4}>
                    <div className="mt-6">
                      <h2 className="text-gray-500 mb-2">
                        Latest Generated Images (click to use as reference):
                      </h2>
                      <div className="space-y-3">
                        {latestGeneratedImages.map((imageUrl, index) => (
                          <div
                            key={index}
                            className="relative group cursor-pointer w-full"
                            onClick={() => {
                              console.log("Setting uploaded URL to:", imageUrl);
                              setUploadedUrl(imageUrl);
                            }}
                          >
                            <Image
                              src={imageUrl}
                              alt={`Generated ${index + 1}`}
                              width={300}
                              height={300}
                              className="rounded-lg object-cover w-full h-48 hover:opacity-80 transition-opacity"
                            />
                            <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 transition-all rounded-lg flex items-center justify-center">
                              <span className="text-white text-sm font-medium opacity-0 group-hover:opacity-100 transition-opacity bg-black bg-opacity-50 px-3 py-2 rounded">
                                Use as Ref
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </BlurFade>
                )}
              </>
            ) : (
              /* Regular instruction editor for other assistants */
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
            )}

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

        {activeTab === "documents" && assistant?.name !== "Image editor" && (
          <div className="pb-20">
            <DocumentManager />
          </div>
        )}
      </div>
    )
  );
}

export default AssistantSettings;
