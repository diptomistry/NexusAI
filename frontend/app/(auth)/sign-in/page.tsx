"use client";
import { ShineBorder } from "@/components/magicui/shine-border";
import { Button } from "@/components/ui/button";
import { AuthContext } from "@/context/AuthContext";
import { GetAuthUserData } from "@/services/GlobalApi";
import { useGoogleLogin } from "@react-oauth/google";
import axios from "axios";
import Image from "next/image";
import { useRouter } from "next/navigation";
import React, { useContext } from "react";
import { createUser } from "@/services/database";

function SignIn() {
  const { user, setUser } = useContext(AuthContext);
  const router = useRouter();
  const googleLogin = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
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
        router.replace("/ai-assistants");
      } catch (error) {
        console.error("Error creating user:", error);
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
        <Image src={"/logo.svg"} alt="logo" width={50} height={50} />
        <h2 className="text-2xl">Sign In To AI Personal Assitant & Agent</h2>

        <Button onClick={() => googleLogin()}>Sign in With Gmail</Button>
      </div>
    </div>
  );
}

export default SignIn;
