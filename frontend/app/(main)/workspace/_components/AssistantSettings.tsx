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
import { Loader2Icon, Save, Trash } from "lucide-react";
import { toast } from "sonner";
import ConfirmationAlert from "../ConfirmationAlert";
import { BlurFade } from "@/components/magicui/blur-fade";
import { updateUserAiAssistant, deleteAssistant } from "@/services/database";

function AssistantSettings() {
  const { assistant, setAssistant } = useContext(AssistantContext);
  const [loading, setLoading] = useState(false);

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
      <div className="p-5 bg-secondary border-l-[1px] h-screen">
        <h2 className="font-bold text-xl">Settings</h2>
        <BlurFade delay={0.25}>
          <div className="mt-4 flex gap-3">
            <Image
              src={assistant?.image}
              alt="assistant"
              width={100}
              height={100}
              className="rounded-xl h-[80px] w-[80px] object-cover"
            />
            <div>
              <h2 className="font-bold">{assistant?.name}</h2>
              <p className="text-gray-700 dark:text-gray-300">
                {assistant?.title}
              </p>
            </div>
          </div>
        </BlurFade>
        <BlurFade delay={0.25 * 2}>
          <div className="mt-4">
            <h2 className="text-gray-500">Model:</h2>
            <Select
              defaultValue={assistant.aiModelId}
              onValueChange={(value) => onHandleInputChange("aiModelId", value)}
            >
              <SelectTrigger className="w-full bg-white">
                <SelectValue placeholder="Select Model" />
              </SelectTrigger>
              <SelectContent>
                {AiModelOptions.map((model, index) => (
                  <SelectItem value={model.name} key={index}>
                    <div className="flex gap-2 items-center m-1">
                      <Image
                        src={model.logo}
                        alt={model.name}
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
        <div className="absolute bottom-10 right-5 flex  gap-5">
          <ConfirmationAlert OnDelete={OnDelete}>
            <Button disabled={loading} variant="ghost">
              {" "}
              <Trash /> Delete
            </Button>
          </ConfirmationAlert>
          <Button onClick={OnSave} disabled={loading}>
            {" "}
            {loading ? <Loader2Icon className="animate-spin" /> : <Save />} Save
          </Button>
        </div>
      </div>
    )
  );
}

export default AssistantSettings;
