import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  try {
    const formData = await req.formData();
    const imageFile = formData.get("image") as File;
    const prompt = formData.get("prompt") as string;
    const userId = formData.get("userId") as string;
    const duration = formData.get("duration") as string;
    const resolution = formData.get("resolution") as string;
    const aspectRatio = formData.get("aspectRatio") as string;

    // Call your Spring Boot backend
    const backendFormData = new FormData();
    backendFormData.append("image", imageFile);
    backendFormData.append("prompt", prompt);
    backendFormData.append("userId", userId);
    backendFormData.append("duration", duration || "5");
    backendFormData.append("resolution", resolution || "1080p");
    backendFormData.append("aspectRatio", aspectRatio || "16:9");

    const response = await fetch(`${process.env.NEXT_PUBLIC_SPRING_BOOT_URL || 'http://localhost:8080'}/api/video/generate/image`, {
      method: 'POST',
      body: backendFormData
    });

    if (!response.ok) {
      throw new Error(`Backend error: ${response.status}`);
    }

    const data = await response.json();
    return NextResponse.json(data);

  } catch (error) {
    console.error('Image-to-video generation error:', error);
    return NextResponse.json(
      { error: 'Failed to generate video from image' },
      { status: 500 }
    );
  }
}
