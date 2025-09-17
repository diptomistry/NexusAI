import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  try {
    const { prompt, userId, duration, resolution, aspectRatio } = await req.json();

    // Call your Spring Boot backend
    const response = await fetch(`${process.env.NEXT_PUBLIC_SPRING_BOOT_URL || 'http://localhost:8080'}/api/video/generate/text`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        prompt,
        userId,
        duration: duration?.toString() || '5',
        resolution: resolution || '1080p',
        aspectRatio: aspectRatio || '16:9'
      })
    });

    if (!response.ok) {
      throw new Error(`Backend error: ${response.status}`);
    }

    const data = await response.json();
    return NextResponse.json(data);

  } catch (error) {
    console.error('Video generation error:', error);
    return NextResponse.json(
      { error: 'Failed to generate video' },
      { status: 500 }
    );
  }
}
