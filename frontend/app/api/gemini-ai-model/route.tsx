import { NextRequest, NextResponse } from "next/server";
import { GoogleGenerativeAI } from "@google/generative-ai";

export async function POST(req: NextRequest) {
  try {
    const { userInput, aiResp } = await req.json();

    console.log("Direct Gemini API Request received:");
    console.log("User Input:", userInput);
    console.log("AI Response:", aiResp);

    // Check if Gemini API key exists
    if (!process.env.NEXT_PUBLIC_GEMINI_AI_MODEL) {
      console.error(
        "NEXT_PUBLIC_GEMINI_AI_MODEL not found in environment variables"
      );
      return NextResponse.json(
        {
          role: "assistant",
          content:
            "Gemini AI service is not configured properly. Please check the server configuration.",
        },
        { status: 500 }
      );
    }

    // Initialize Gemini AI
    const genAI = new GoogleGenerativeAI(
      process.env.NEXT_PUBLIC_GEMINI_AI_MODEL
    );
    const model = genAI.getGenerativeModel({
      model: "gemini-2.0-flash-exp",
    });

    // Build chat history if there's a previous AI response
    const chatHistory = [];
    if (aiResp && aiResp !== "Loading...") {
      chatHistory.push({
        role: "user",
        parts: [{ text: "Previous context" }],
      });
      chatHistory.push({
        role: "model",
        parts: [{ text: aiResp }],
      });
    }

    // Start chat with history
    const chat = model.startChat({
      history: chatHistory,
    });

    console.log("Sending request to Gemini AI");

    // Send message to Gemini
    const result = await chat.sendMessage(userInput);
    const response = await result.response;
    const generatedText = response.text();

    console.log("Gemini AI Response received:", generatedText);

    const resp = {
      role: "assistant",
      content:
        generatedText ||
        "Sorry, I could not generate a response. Please try again.",
    };

    return NextResponse.json(resp);
  } catch (error) {
    console.error("Gemini AI API Error:", error);

    // Return error response
    const errorResp = {
      role: "assistant",
      content:
        "Sorry, I encountered an error processing your request. Please try again.",
    };

    return NextResponse.json(errorResp, { status: 500 });
  }
}
