"use client";

import React, { useState, useContext } from "react";
import { AuthContext } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { toast } from "sonner";
import axios from "axios";
import { Play, Download, Loader2 } from "lucide-react";
import { calculateTokenCost } from "@/lib/tokenCosts";
import { updateUserTokens } from "@/services/database";

interface GenerationStatus {
  taskId: string;
  status: string;
  videoUrl?: string;
  error?: string;
}

const VideoGenerator = () => {
  const { user, setUser } = useContext(AuthContext);
  const [prompt, setPrompt] = useState("");
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [duration, setDuration] = useState(5);
  const [resolution, setResolution] = useState("1080p");
  const [aspectRatio, setAspectRatio] = useState("16:9");
  const [isGenerating, setIsGenerating] = useState(false);
  const [generationStatus, setGenerationStatus] =
    useState<GenerationStatus | null>(null);
  const [progress, setProgress] = useState(0);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        // 10MB limit
        toast.error("Image file size exceeds 10MB limit.");
        return;
      }
      if (!file.type.startsWith("image/")) {
        toast.error("Please select an image file");
        return;
      }
      setImageFile(file);
      toast.success("Image uploaded successfully");
    }
  };

  const removeImage = () => {
    setImageFile(null);
    const fileInput = document.getElementById(
      "image-upload"
    ) as HTMLInputElement;
    if (fileInput) {
      fileInput.value = "";
    }
  };

  const deductTokens = async (videoDuration: number) => {
    if (!user?.id || !user?.credits) {
      console.log("No user or credits available for token deduction");
      return;
    }

    // Calculate tokens based on video duration ($0.15 per second = 1500 tokens per second)
    const tokenCount = calculateTokenCost(
      "Video generator",
      "",
      false,
      videoDuration
    );

    console.log(
      `Video generation - charging ${tokenCount} tokens for ${videoDuration}s video`
    );

    try {
      await updateUserTokens(user.id, user.credits - tokenCount);

      setUser((prev: any) => ({
        ...prev,
        credits: user.credits - tokenCount,
      }));

      toast.success(`Video generated! ${tokenCount} tokens deducted.`);
    } catch (error) {
      console.error("Error updating tokens:", error);
      toast.error("Error updating token balance");
    }
  };

  const handleGenerateVideo = async () => {
    if (!prompt.trim()) {
      toast.error("Please enter a prompt");
      return;
    }

    if (!user?.id) {
      toast.error("User not logged in.");
      return;
    }

    // Check if user has enough credits for video generation
    const requiredTokens = calculateTokenCost(
      "Video generator",
      "",
      false,
      duration
    );
    if (user?.credits < requiredTokens) {
      toast.error(
        `Insufficient tokens! You need ${requiredTokens} tokens for a ${duration}-second video. Please buy more tokens.`
      );
      return;
    }

    setIsGenerating(true);
    setProgress(0);
    setGenerationStatus(null);

    try {
      let response;
      if (imageFile) {
        // Image-to-video generation
        const formData = new FormData();
        formData.append("image", imageFile);
        formData.append("prompt", prompt);
        formData.append("userId", user.id);
        formData.append("duration", duration.toString());
        formData.append("resolution", resolution);
        formData.append("aspectRatio", aspectRatio);

        response = await axios.post("/api/video/generate/image", formData);
      } else {
        // Text-to-video generation
        response = await axios.post("/api/video/generate/text", {
          prompt,
          userId: user.id,
          duration,
          resolution,
          aspectRatio,
        });
      }

      const { taskId } = response.data;
      setGenerationStatus({ taskId, status: "processing" });

      // Poll for status
      const pollStatus = setInterval(async () => {
        try {
          const statusResponse = await axios.get(`/api/video/status/${taskId}`);
          const status = statusResponse.data;

          setGenerationStatus(status);
          setProgress(Math.min(progress + 10, 90)); // Simulate progress

          if (status.status === "completed") {
            clearInterval(pollStatus);
            setIsGenerating(false);
            setProgress(100);
            // Deduct tokens based on video duration
            await deductTokens(duration);
            toast.success(
              "Video generated successfully! Please download it before refreshing the page."
            );
          } else if (status.status === "failed") {
            clearInterval(pollStatus);
            setIsGenerating(false);
            toast.error(
              "Video generation failed: " + (status.error || "Unknown error")
            );
          }
        } catch (error) {
          console.error("Error polling status:", error);
          clearInterval(pollStatus);
          setIsGenerating(false);
          toast.error("Failed to get video generation status.");
        }
      }, 2000);
    } catch (error: any) {
      console.error("Error generating video:", error);
      setIsGenerating(false);
      toast.error(
        "Failed to generate video: " +
          (error.response?.data?.error || error.message)
      );
    }
  };

  const handleDownload = () => {
    if (generationStatus?.videoUrl) {
      window.open(generationStatus.videoUrl, "_blank");
    }
  };

  return (
    <div className="w-full h-full overflow-y-auto">
      <div className="max-w-4xl mx-auto p-1 space-y-1">
        <Card>
          <CardHeader className="pb-1">
            <CardTitle className="flex items-center gap-2 text-sm">
              <Play className="h-3 w-3" />
              Video Generator
            </CardTitle>
            <CardDescription className="text-xs">
              Generate videos from text prompts or images using AI.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-1">
            {/* Prompt Input */}
            <div className="space-y-1">
              <label className="text-xs font-medium">Video Prompt</label>
              <Textarea
                placeholder="Describe the video you want to generate..."
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                className="min-h-[60px] text-sm"
                disabled={isGenerating}
              />
            </div>

            {/* Image Upload */}
            <div className="space-y-1">
              <label className="text-xs font-medium">
                Reference Image (Optional)
              </label>
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-2 text-center">
                {imageFile ? (
                  <div className="space-y-1">
                    <img
                      src={URL.createObjectURL(imageFile)}
                      alt="Uploaded image"
                      className="mx-auto max-h-16 rounded object-cover"
                    />
                    <p className="text-xs text-gray-600 truncate">
                      {imageFile.name}
                    </p>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={removeImage}
                      disabled={isGenerating}
                      className="text-xs h-6 px-2"
                    >
                      Remove
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-1">
                    <input
                      id="image-upload"
                      type="file"
                      accept="image/*"
                      onChange={handleImageChange}
                      className="hidden"
                      disabled={isGenerating}
                    />
                    <label
                      htmlFor="image-upload"
                      className="cursor-pointer text-blue-600 hover:text-blue-800 text-xs"
                    >
                      Click to upload an image
                    </label>
                    <p className="text-xs text-gray-500">
                      PNG, JPG, GIF up to 10MB
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Video Settings */}
            <div className="grid grid-cols-3 gap-2">
              <div className="space-y-1">
                <label className="text-xs font-medium text-gray-700">
                  Duration
                </label>
                <Select
                  value={duration.toString()}
                  onValueChange={(value) => setDuration(parseInt(value))}
                  disabled={isGenerating}
                >
                  <SelectTrigger className="h-7 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="3">3s</SelectItem>
                    <SelectItem value="5">5s</SelectItem>
                    <SelectItem value="8">8s</SelectItem>
                    <SelectItem value="10">10s</SelectItem>
                    <SelectItem value="12">12s</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-medium text-gray-700">
                  Resolution
                </label>
                <Select
                  value={resolution}
                  onValueChange={setResolution}
                  disabled={isGenerating}
                >
                  <SelectTrigger className="h-7 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="720p">720p</SelectItem>
                    <SelectItem value="1080p">1080p</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-medium text-gray-700">
                  Aspect
                </label>
                <Select
                  value={aspectRatio}
                  onValueChange={setAspectRatio}
                  disabled={isGenerating}
                >
                  <SelectTrigger className="h-7 text-xs">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="16:9">16:9</SelectItem>
                    <SelectItem value="9:16">9:16</SelectItem>
                    <SelectItem value="1:1">1:1</SelectItem>
                    <SelectItem value="4:3">4:3</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Token Cost Indicator */}
            <div className="text-xs text-gray-600 text-center">
              Cost: {calculateTokenCost("Video generator", "", false, duration)}{" "}
              tokens ({duration}s × 1500 tokens/s)
            </div>

            {/* Generate Button */}
            <Button
              onClick={handleGenerateVideo}
              disabled={isGenerating || !prompt.trim()}
              className="w-full h-8 text-sm"
              size="sm"
            >
              {isGenerating ? (
                <>
                  <Loader2 className="mr-1 h-3 w-3 animate-spin" />
                  Generating...
                </>
              ) : (
                <>
                  <Play className="mr-1 h-3 w-3" />
                  {imageFile ? "Generate from Image" : "Generate from Text"}
                </>
              )}
            </Button>

            {/* Progress Bar */}
            {isGenerating && (
              <div className="space-y-1">
                <div className="flex justify-between text-xs">
                  <span>Generating video...</span>
                  <span>{progress}%</span>
                </div>
                <Progress value={progress} className="w-full h-1" />
              </div>
            )}

            {/* Status Display */}
            {generationStatus && (
              <div className="p-2 border rounded text-xs">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="font-medium">
                      Status:{" "}
                      <span className="capitalize">
                        {generationStatus.status}
                      </span>
                    </p>
                    {generationStatus.taskId && (
                      <p className="text-xs text-gray-500 truncate">
                        ID: {generationStatus.taskId}
                      </p>
                    )}
                  </div>
                  {generationStatus.status === "completed" &&
                    generationStatus.videoUrl && (
                      <Button
                        onClick={handleDownload}
                        size="sm"
                        className="h-6 px-2 text-xs"
                      >
                        <Download className="mr-1 h-3 w-3" />
                        Download
                      </Button>
                    )}
                </div>
              </div>
            )}

            {/* Video Preview */}
            {generationStatus?.status === "completed" &&
              generationStatus.videoUrl && (
                <div className="space-y-1">
                  <label className="text-xs font-medium">Generated Video</label>
                  <video
                    src={generationStatus.videoUrl}
                    controls
                    className="w-full rounded border max-h-40"
                  >
                    Your browser does not support the video tag.
                  </video>

                  {/* Download Warning */}
                  <div className="bg-yellow-50 border border-yellow-200 rounded p-1 text-xs text-yellow-800">
                    <div className="flex items-center gap-1">
                      <span className="font-medium">⚠️ Important:</span>
                      <span>
                        Download your video now! Videos are not stored
                        permanently and will be lost if you refresh the page.
                      </span>
                    </div>
                  </div>
                </div>
              )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default VideoGenerator;
