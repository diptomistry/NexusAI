import { NextRequest, NextResponse } from "next/server";

export async function GET(
  req: NextRequest,
  { params }: { params: Promise<{ taskId: string }> }
) {
  try {
    const { taskId } = await params;

    // Call your Spring Boot backend
    const response = await fetch(`${process.env.NEXT_PUBLIC_SPRING_BOOT_URL || 'http://localhost:8080'}/api/video/status/${taskId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (!response.ok) {
      throw new Error(`Backend error: ${response.status}`);
    }

    const data = await response.json();
    return NextResponse.json(data);

  } catch (error) {
    console.error('Video status error:', error);
    return NextResponse.json(
      { error: 'Failed to get video status' },
      { status: 500 }
    );
  }
}
