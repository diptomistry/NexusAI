import { NextRequest, NextResponse } from "next/server";

//Eden AI
export async function POST(req: NextRequest) {
  try {
    const { provider, userInput, aiResp } = await req.json();

    // Debug logging
    console.log("API Request received:");
    console.log("Provider:", provider);
    console.log("User Input:", userInput);
    console.log("AI Response:", aiResp);

    // Check if Eden AI API key exists
    if (!process.env.EDEN_AI_API_KEY) {
      console.error("EDEN_AI_API_KEY not found in environment variables");
      return NextResponse.json(
        {
          role: "assistant",
          content:
            "AI service is not configured properly. Please check the server configuration.",
        },
        { status: 500 }
      );
    }

    // Check if provider is valid
    if (!provider) {
      console.error("No provider specified");
      return NextResponse.json(
        {
          role: "assistant",
          content: "AI model not specified. Please select an AI model.",
        },
        { status: 400 }
      );
    }

    const headers = {
      Authorization: "Bearer " + process.env.EDEN_AI_API_KEY,
      "Content-Type": "application/json",
    };

    const url = "https://api.edenai.run/v2/multimodal/chat";
    const body = JSON.stringify({
      providers: [provider],
      messages: [
        {
          role: "user",
          content: [
            {
              type: "text",
              content: {
                text: userInput,
              },
            },
          ],
        },
        ...(aiResp
          ? [
              {
                role: "assistant",
                content: [
                  {
                    type: "text",
                    content: {
                      text: aiResp,
                    },
                  },
                ],
              },
            ]
          : []),
      ],
    });

    console.log("Sending request to Eden AI:", { url, provider });

    const response = await fetch(url, {
      method: "POST",
      headers,
      body,
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`Eden AI API error: ${response.status} - ${errorText}`);
      throw new Error(`Eden AI API error: ${response.status}`);
    }

    const result = await response.json();
    console.log("Eden AI Response:", result);

    // Check if we got a valid response from the provider
    const generatedText = result[provider]?.generated_text;

    if (!generatedText) {
      console.error("No generated text from provider:", provider);
      console.error("Full result:", result);
    }

    const resp = {
      role: "assistant",
      content:
        generatedText ||
        "Sorry, I could not generate a response. Please try again.",
    };

    return NextResponse.json(resp);
  } catch (error) {
    console.error("Eden AI API Error:", error);

    // Return error response
    const errorResp = {
      role: "assistant",
      content:
        "Sorry, I encountered an error processing your request. Please try again.",
    };

    return NextResponse.json(errorResp, { status: 500 });
  }
}
