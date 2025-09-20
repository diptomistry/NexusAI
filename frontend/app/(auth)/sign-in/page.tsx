"use client";
import { ShineBorder } from "@/components/magicui/shine-border";
import { Button } from "@/components/ui/button";
import { AuthContext } from "@/context/AuthContext";
import { GetAuthUserData } from "@/services/GlobalApi";
import { useGoogleLogin } from "@react-oauth/google";
import axios from "axios";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useContext, useState } from "react";
import { createUser, getAllUserAssistants } from "@/services/database";
import { supabase } from "@/lib/supabase";

function SignIn() {
  const { user, setUser } = useContext(AuthContext);
  const router = useRouter();
  const [isSigningIn, setIsSigningIn] = useState(false);
  const googleLogin = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      setIsSigningIn(true);
      if (typeof window !== undefined) {
        localStorage.setItem("user_token", tokenResponse.access_token);
      }
      const userData = await GetAuthUserData(tokenResponse.access_token);

      try {
        // Save User Info
        const result = await createUser({
          name: userData?.name,
          email: userData?.email,
          picture: userData.picture,
        });

        setUser(result);

        // Check if user has pending assistants from pre-sign-in selection
        const pendingAssistants = localStorage.getItem("pendingAssistants");
        if (pendingAssistants) {
          // User had pre-selected assistants, go back to assistant selection page
          // The assistant selection page will handle auto-continuing
          router.replace("/ai-assistants");
        } else {
          // Check if user has existing assistants
          try {
            const existingAssistants = await getAllUserAssistants(result.id);
            if (existingAssistants.length > 0) {
              // User has assistants, go directly to workspace
              router.replace("/workspace");
            } else {
              // New user with no assistants, go to assistant selection
              router.replace("/ai-assistants");
            }
          } catch (assistantError) {
            console.error(
              "Error checking existing assistants:",
              assistantError
            );
            // If there's an error checking assistants, default to assistant selection
            router.replace("/ai-assistants");
          }
        }
      } catch (error) {
        console.error("Error creating user:", error);
      } finally {
        setIsSigningIn(false);
      }
    },
    onError: (errorResponse: any) => console.log(errorResponse),
  });
  return (
    <div className="flex items-center flex-col justify-center h-screen">
      <ShineBorder shineColor={["#A07CFE", "#FE8FB5", "#FFBE7B"]} />
      <div
        className="flex flex-col items-center
        gap-5 border rounded-2xl p-10 shadow-md"
      >
        <Image src={"/NesusAI.png"} alt="Nexus AI" width={50} height={50} />
        <h2 className="text-2xl">Sign In To AI Personal Assitant & Agent</h2>

        <Button onClick={() => googleLogin()} disabled={isSigningIn}>
          {isSigningIn ? "Signing in..." : "Sign in With Gmail"}
        </Button>
      </div>
    </div>
  );
}

export default SignIn;
